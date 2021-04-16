package im.cu.router;

/**
 * Created by huangliming on 2021/3/18
 */

import im.cu.api.Plan;
import im.cu.api.grpc.factory.CacheProxyRpcServiceFactory;
import im.cu.api.grpc.factory.CacheStoreRpcServiceFactory;
import im.cu.api.register.ZKConstants;
import im.cu.api.register.ZkClient;
import im.cu.api.utils.DelayRetry;
import im.cu.api.utils.DumpService;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.proxy.CacheProxy;
import im.cu.grpc.api.server.cache.proxy.CacheProxyServiceGrpc;
import im.cu.grpc.api.server.cache.store.CacheStore;
import im.cu.grpc.api.server.cache.store.CacheStoreServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Slf4j
public class SlotService {

    @Autowired
    private CacheStoreRpcServiceFactory storeRpcServiceFactory;

    @Autowired
    private CacheProxyRpcServiceFactory proxyRpcServiceFactory;

    @Autowired
    private DumpService dumpService;

    @Autowired
    private ZkClient zkClient;

    private Map<Integer, Common.HostAndPort> routerTable;

    private static final String SLOTS_FILE_NAME = "slots";

    @PostConstruct
    private void init() {
        Object data = dumpService.read(SLOTS_FILE_NAME);
        if (ObjectUtils.isEmpty(data)) {
            routerTable = new ConcurrentHashMap<>();
        } else {
            routerTable = (Map<Integer, Common.HostAndPort>) data;
        }
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            try {
                dumpService.dump(routerTable, "slots");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }, 1, 1, TimeUnit.MINUTES);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> dumpService.dump(routerTable, "slots")));
    }

    public Map<Integer, Common.HostAndPort> getRouterTable() {
        return this.routerTable;
    }

    /**
     * 单台服务下线 slot被其他结点瓜分
     * 该服务的slot视为空闲状态
     * zk服务列表排除当前服务
     * @param serviceInfo
     */
    public void offline(String serviceInfo) {
        this.autoReShard(serviceInfo);
    }

    public void autoReShard() {
        this.autoReShard(null);
    }

    /**
     * slot分配策略-均分
     */
    private void autoReShard(String deadHuman) {
        //获取村子里所有的人
        List<String> humanList = getServiceList(deadHuman);
        //通过账本对所有人所拥有的财产进行统计
        Map<Common.HostAndPort, List<Integer>> groupingRouterTable = group(deadHuman);
        //校验是否有down掉的服务
        groupingRouterTable.forEach(((hostAndPort, s) -> {
            if (!humanList.contains(hostAndPort.getHost() + ":" + hostAndPort.getPort())) {
                log.error("机器{} 处于下线状态，无法执行autoReShard", hostAndPort.getHost() + ":" + hostAndPort.getPort());
                throw new RuntimeException("有机器处于下线状态，请先恢复");
            }
        }));
        //账本上没统计到的人，也统计上，财产是0
        for (String human : humanList) {
            Common.HostAndPort hostAndPort = getHostAndPortByStr(human);
            if (!groupingRouterTable.containsKey(hostAndPort)) {
                groupingRouterTable.put(hostAndPort, Collections.emptyList());
            }
        }

        //小根堆， 优先分配最困难的苦难户
        PriorityQueue<SerfInfo> serfInfos = new PriorityQueue<>((Comparator.comparingInt(o -> o.slotCnt)));
        //闲置的钱
        List<Integer> unusedSlots = getUnusedSlots(groupingRouterTable);
        //财产的平均值
        int perSlots = 1024 / humanList.size();
        //从地主上搜刮来的, 高于平均值得钱
        List<Integer> landlordSurplusSlots = new ArrayList<>();
        //找出来困难户
        groupingRouterTable.forEach(((hostAndPort, slots) -> {
            if (slots.size() > perSlots) {
                //搜刮地主
                landlordSurplusSlots.addAll(slots.stream().limit(slots.size() - perSlots).collect(Collectors.toList()));
            } else if (slots.size() < perSlots) {
                //统计苦难户
                serfInfos.add(new SerfInfo(hostAndPort, slots.size()));
            }
        }));

        //分钱计划
        List<Plan> plans = new ArrayList<>();
        //闲置的钱的下标
        int unusedIndex = 0;
        //地主多出来的钱的下标
        int landlordSurplusIndex = 0;
        while (!serfInfos.isEmpty()) {
            SerfInfo serfInfo = serfInfos.poll();
            //困难户差的钱
            int distance = perSlots - serfInfo.slotCnt;
            //记录发了多少钱
            int i;
            //先用闲置的钱发
            for (i = 0; i < distance; i++) {
                if (unusedIndex >= unusedSlots.size()) {
                    break;
                }
                plans.add(new Plan(unusedSlots.get(unusedIndex++), serfInfo.hostAndPort));
            }
            if (i < distance) {
                //如果不够, 再用地主家多出来的发
                for (i = 0; i < distance; i++) {
                    if (landlordSurplusIndex >= landlordSurplusSlots.size()) {
                        break;
                    }
                    plans.add(new Plan(landlordSurplusSlots.get(landlordSurplusIndex++), serfInfo.hostAndPort));
                }
            }
            if (serfInfos.isEmpty()) {
                while (unusedIndex < unusedSlots.size()) {
                    plans.add(new Plan(unusedSlots.get(unusedIndex++), serfInfo.hostAndPort));
                }
                while (landlordSurplusIndex < landlordSurplusSlots.size()) {
                    plans.add(new Plan(landlordSurplusSlots.get(landlordSurplusIndex++), serfInfo.hostAndPort));
                }
            }
        }
        if (CollectionUtils.isEmpty(plans)) {
            return;
        }
        //TODO 保存plans
        //根据分钱计划开始分钱
        //1. 向proxy发送beginMigrating指令，让proxy维护一个迁移完成后的临时路由表, 不重试。快速失败
        List<String> proxyList = zkClient.getChildren(ZKConstants.CACHE_ROOT_PATH + "/" + ZKConstants.CACHE_PROXY);
        this.sendBeginMigratingMsgToProxy(plans, proxyList);
        //2. 向cache-store发送migrate指令 若失败，重试
        ((SlotService)AopContext.currentProxy()).migrating(plans);
        //3. proxy routerTable = tmpRouterTable
        ((SlotService)AopContext.currentProxy()).sendCommitMigratingMsgToProxy(proxyList);

    }

    private List<String> getServiceList(String deadHuman) {
        List<String> humans = zkClient.getChildren(ZKConstants.CACHE_ROOT_PATH + "/" + ZKConstants.CACHE_STORE);
        if (StringUtils.isEmpty(deadHuman)) {
            return humans;
        }
        humans.remove(deadHuman);
        return humans;
    }

    private Map<Common.HostAndPort, List<Integer>> group(String deadHuman) {
        Map<Common.HostAndPort, List<Integer>> groupingRouterTable
                = routerTable.entrySet().stream().collect(Collectors.groupingBy(item -> item.getValue(), Collectors.mapping(item -> item.getKey(), Collectors.toList())));
        if (StringUtils.isEmpty(deadHuman)) {
            return groupingRouterTable;
        }
        groupingRouterTable.remove(getHostAndPortByStr(deadHuman));
        return groupingRouterTable;
    }

    private void sendBeginMigratingMsgToProxy(List<Plan> plans, List<String> proxyList) {
        Map<Integer, Common.HostAndPort> tmpRouterTable = new HashMap<>();
        tmpRouterTable.putAll(routerTable);
        for (Plan plan : plans) {
            tmpRouterTable.put(plan.getSlot(), plan.getTo());
        }
        for (String proxy : proxyList) {
            CacheProxyServiceGrpc.CacheProxyServiceBlockingStub proxyStub = proxyRpcServiceFactory.createBlockingStub(getHostAndPortByStr(proxy));
            proxyStub.beginMigrating(CacheProxy.RouterTableSyncReq.newBuilder().putAllRouterTable(tmpRouterTable).build());
        }
    }

    @DelayRetry(initialDelay = 5, maxTimes = 6)
    public void sendCommitMigratingMsgToProxy(List<String> proxyList) {
        for (String proxy : proxyList) {
            CacheProxyServiceGrpc.CacheProxyServiceBlockingStub proxyStub = proxyRpcServiceFactory.createBlockingStub(getHostAndPortByStr(proxy));
            proxyStub.commitMigrating(CacheProxy.RouterTableSyncReq.newBuilder().build());
        }
    }

    @DelayRetry(initialDelay = 5, maxTimes = 6)
    public void migrating(List<Plan> plans) {
        for (Plan plan : plans) {
            if (routerTable.containsKey(plan.getSlot())) {
                CacheStoreServiceGrpc.CacheStoreServiceBlockingStub blockingStub = storeRpcServiceFactory.createBlockingStub(routerTable.get(plan.getSlot()));
                blockingStub.migrating(CacheStore.CacheMigrateReq.newBuilder().setSlotId(plan.getSlot()).setHostAndPort(plan.getTo()).build());
            }
            routerTable.put(plan.getSlot(), plan.getTo());
        }
    }

    /**
     * 闲置分两种
     * 1. 尚未分配，不在路由表里
     * 2. 在路由表里，但是服务已经停了 (这种情况不处理 忽略 因为slot无法迁移, 需要对准备下线的机器做一次手动迁移，而不是自动分配)
     *
     * @return
     */
    private List<Integer> getUnusedSlots(Map<Common.HostAndPort, List<Integer>> groupingRouterTable) {
        List<Integer> slots = groupingRouterTable.values().stream().flatMap((list -> list.stream())).collect(Collectors.toList());
        List<Integer> allSlots = IntStream.rangeClosed(0, 1023)
                .boxed().collect(Collectors.toList());
        allSlots.removeAll(slots);
        return allSlots;
    }



    private class SerfInfo {
        Common.HostAndPort hostAndPort;
        int slotCnt;

        public SerfInfo(Common.HostAndPort hostAndPort, int slotCnt) {
            this.hostAndPort = hostAndPort;
            this.slotCnt = slotCnt;
        }
    }

    private Common.HostAndPort getHostAndPortByStr(String serviceInfo) {
        if (StringUtils.isEmpty(serviceInfo)) {
            return null;
        }
        String[] split = serviceInfo.split(":");
        return Common.HostAndPort.newBuilder().setHost(split[0]).setPort(Integer.valueOf(split[1])).build();
    }
}











