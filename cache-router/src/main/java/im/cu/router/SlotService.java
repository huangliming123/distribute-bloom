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
     * ?????????????????? slot?????????????????????
     * ????????????slot??????????????????
     * zk??????????????????????????????
     * @param serviceInfo
     */
    public void offline(String serviceInfo) {
        this.autoReShard(serviceInfo);
    }

    public void autoReShard() {
        this.autoReShard(null);
    }

    /**
     * slot????????????-??????
     */
    private void autoReShard(String deadHuman) {
        //???????????????????????????
        List<String> humanList = getServiceList(deadHuman);
        //??????????????????????????????????????????????????????
        Map<Common.HostAndPort, List<Integer>> groupingRouterTable = group(deadHuman);
        //???????????????down????????????
        groupingRouterTable.forEach(((hostAndPort, s) -> {
            if (!humanList.contains(hostAndPort.getHost() + ":" + hostAndPort.getPort())) {
                log.error("??????{} ?????????????????????????????????autoReShard", hostAndPort.getHost() + ":" + hostAndPort.getPort());
                throw new RuntimeException("??????????????????????????????????????????");
            }
        }));
        //??????????????????????????????????????????????????????0
        for (String human : humanList) {
            Common.HostAndPort hostAndPort = getHostAndPortByStr(human);
            if (!groupingRouterTable.containsKey(hostAndPort)) {
                groupingRouterTable.put(hostAndPort, Collections.emptyList());
            }
        }

        //???????????? ?????????????????????????????????
        PriorityQueue<SerfInfo> serfInfos = new PriorityQueue<>((Comparator.comparingInt(o -> o.slotCnt)));
        //????????????
        List<Integer> unusedSlots = getUnusedSlots(groupingRouterTable);
        //??????????????????
        int perSlots = 1024 / humanList.size();
        //????????????????????????, ?????????????????????
        List<Integer> landlordSurplusSlots = new ArrayList<>();
        //??????????????????
        groupingRouterTable.forEach(((hostAndPort, slots) -> {
            if (slots.size() > perSlots) {
                //????????????
                landlordSurplusSlots.addAll(slots.stream().limit(slots.size() - perSlots).collect(Collectors.toList()));
            } else if (slots.size() < perSlots) {
                //???????????????
                serfInfos.add(new SerfInfo(hostAndPort, slots.size()));
            }
        }));

        //????????????
        List<Plan> plans = new ArrayList<>();
        //?????????????????????
        int unusedIndex = 0;
        //??????????????????????????????
        int landlordSurplusIndex = 0;
        while (!serfInfos.isEmpty()) {
            SerfInfo serfInfo = serfInfos.poll();
            //??????????????????
            int distance = perSlots - serfInfo.slotCnt;
            //?????????????????????
            int i;
            //?????????????????????
            for (i = 0; i < distance; i++) {
                if (unusedIndex >= unusedSlots.size()) {
                    break;
                }
                plans.add(new Plan(unusedSlots.get(unusedIndex++), serfInfo.hostAndPort));
            }
            if (i < distance) {
                //????????????, ??????????????????????????????
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
        //TODO ??????plans
        //??????????????????????????????
        //1. ???proxy??????beginMigrating????????????proxy?????????????????????????????????????????????, ????????????????????????
        List<String> proxyList = zkClient.getChildren(ZKConstants.CACHE_ROOT_PATH + "/" + ZKConstants.CACHE_PROXY);
        this.sendBeginMigratingMsgToProxy(plans, proxyList);
        //2. ???cache-store??????migrate?????? ??????????????????
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
     * ???????????????
     * 1. ?????????????????????????????????
     * 2. ?????????????????????????????????????????? (????????????????????? ?????? ??????slot????????????, ???????????????????????????????????????????????????????????????????????????)
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











