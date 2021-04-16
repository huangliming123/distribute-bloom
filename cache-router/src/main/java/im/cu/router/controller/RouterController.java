package im.cu.router.controller;

import im.cu.api.monitor.Durating;
import im.cu.grpc.api.server.cache.Common;
import im.cu.router.SlotService;
import im.cu.router.dto.SlotsShardReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by huangliming on 2021/3/16
 */
@RestController
@Slf4j
@RequestMapping(value = "/router")
public class RouterController {

    @Autowired
    private SlotService slotService;

    private ReentrantLock lock = new ReentrantLock();

    @Durating
    @PostMapping(value = "offline")
    public void reShard(@RequestBody String serviceInfo) throws Exception {
        if (!lock.tryLock()) {
            throw new UnsupportedOperationException("当前有slot正在迁移");
        }
        try {
            slotService.offline(serviceInfo);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 1. 初始化使用
     * 2. 新增机器使用
     * warn: 机器下线后autoReShard不会分配下线机器的slot，因为无法迁移数据!!!
     */
    @PostMapping(value = "auto-re-shard")
    public void autoReShard() {
        if (!lock.tryLock()) {
            throw new UnsupportedOperationException("当前有slot正在迁移");
        }
        try {
            slotService.autoReShard();
        } finally {
            lock.unlock();
        }
    }

}
