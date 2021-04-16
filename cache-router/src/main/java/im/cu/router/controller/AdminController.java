package im.cu.router.controller;

import com.google.common.collect.Lists;
import com.google.protobuf.Empty;
import im.cu.api.grpc.factory.CacheStoreRpcServiceFactory;
import im.cu.api.register.ZKConstants;
import im.cu.api.register.ZkClient;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.store.CacheStore;
import im.cu.grpc.api.server.cache.store.CacheStoreServiceGrpc;
import im.cu.router.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by huangliming on 2021/3/22
 */
@Controller
@RequestMapping(value = "admin")
public class AdminController {

    @Autowired
    private SlotService slotService;

    @Autowired
    private ZkClient zkClient;

    @Autowired
    private CacheStoreRpcServiceFactory cacheStoreRpcServiceFactory;

    @RequestMapping(value = "slots")
    public ModelAndView slots() {
        Map<Integer, Common.HostAndPort> routerTable = slotService.getRouterTable();
        Map<Common.HostAndPort, List<Integer>> ipSlotMap = new HashMap<>();
        routerTable.forEach((slot, hostAndPort) -> {
            if (ipSlotMap.containsKey(hostAndPort)) {
                ipSlotMap.get(hostAndPort).add(slot);
            } else {
                ipSlotMap.put(hostAndPort, Lists.newArrayList(slot));
            }
        });

        Map<Common.HostAndPort, List<Integer>> result = new HashMap<>();
        List<String> cacheStores = zkClient.getChildren(ZKConstants.CACHE_ROOT_PATH + "/" + ZKConstants.CACHE_STORE);
        for (String cacheStore : cacheStores) {
            String[] split = cacheStore.split(":");
            Common.HostAndPort build = Common.HostAndPort.newBuilder()
                    .setHost(split[0]).setPort(Integer.valueOf(split[1])).build();
            List<Integer> slots = new ArrayList<>();
            if (ipSlotMap.containsKey(build)) {
                slots.addAll(ipSlotMap.get(build));
            }
            result.put(build, slots);
        }

        Map<String, String> prettyResult = new HashMap<>();
        result.forEach((hostAndPort, slots) -> {
            if (CollectionUtils.isEmpty(slots)) {
                prettyResult.put(hostAndPort.getHost() + ":" + hostAndPort.getPort(), null);
            } else {
                StringBuilder prettySlotsBuilder = new StringBuilder();
                slots.sort(Comparator.comparing(Function.identity()));
                prettySlotsBuilder.append(slots.get(0));
                prettySlotsBuilder.append("...");
                for (int i=1; i<slots.size(); i++) {
                    if (slots.get(i) - slots.get(i-1) > 1) {
                        prettySlotsBuilder.append(slots.get(i-1));
                        prettySlotsBuilder.append(",");
                        prettySlotsBuilder.append(slots.get(i));
                        prettySlotsBuilder.append("...");
                    } else if (i == slots.size() - 1) {
                        prettySlotsBuilder.append(slots.get(i));
                    }
                }
                prettyResult.put(hostAndPort.getHost() + ":" + hostAndPort.getPort(), prettySlotsBuilder.toString());
            }
        });

        Map<String, Integer> keys = new HashMap<>();
        prettyResult.forEach((k, v) -> {
            String[] split = k.split(":");
            CacheStoreServiceGrpc.CacheStoreServiceBlockingStub blockingStub = cacheStoreRpcServiceFactory.createBlockingStub(Common.HostAndPort.newBuilder()
                    .setHost(split[0])
                    .setPort(Integer.valueOf(split[1])).build());
            CacheStore.SlotsGetRes slotsGetRes = blockingStub.keys(Empty.newBuilder().build());
            keys.put(k, slotsGetRes.getSlotsMap().values().stream().mapToInt(item -> item).sum());
        });
        ModelAndView modelAndView = new ModelAndView("routerTable");
        modelAndView.addObject("routerTable", prettyResult);
        modelAndView.addObject("keys", keys);
        return modelAndView;
    }

    @GetMapping(value = "node/{url}")
    public ModelAndView getNode(@PathVariable String url, @RequestParam(required = false) String slotPart) {
//        String[] serviceInfo = url.split(":");
//        CacheStoreServiceGrpc.CacheStoreServiceBlockingStub blockingStub = cacheStoreRpcServiceFactory.createBlockingStub(Common.HostAndPort.newBuilder()
//                .setHost(serviceInfo[0])
//                .setPort(Integer.valueOf(serviceInfo[1])).build());
//        CacheStore.SlotsGetRes slotsGetRes = blockingStub.keys(Empty.newBuilder().build());
//        Map<Integer, String> slotMap = slotsGetRes.getSlotsMap();
//        if (!StringUtils.isEmpty(slotPart)) {
//            String[] slots = slotPart.split("-");
//            int start = Integer.parseInt(serviceInfo[0]);
//            int end = Integer.parseInt(serviceInfo[1]);
//            Map<Integer, String> filterMap = new HashMap<>();
//            slotsGetRes.getSlotsMap().forEach((k, v) -> {
//                if (k >= start && k <= end) {
//                    filterMap.put(k, v);
//                }
//            });
//            slotMap = filterMap;
//        }
//        ModelAndView modelAndView = new ModelAndView("nodeDetail");
//        modelAndView.addObject("map", slotMap);
        return null;
    }
}








