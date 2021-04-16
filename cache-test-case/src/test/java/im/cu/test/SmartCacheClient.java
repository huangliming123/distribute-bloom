package im.cu.test;

import im.cu.api.match_smart_cache.thrift.*;
import im.cu.exception.ThriftClientPoolException;
import im.cu.rpc.thrift.client.BaseThriftClientPool;
import im.cu.rpc.thrift.client.PoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransport;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by huangliming on 2021/4/14
 */
@Slf4j
@Component
public class SmartCacheClient extends BaseThriftClientPool<Void, CacheProxyService.Client> {

    @Override
    protected String getRegisterRootPath() {
        return "/rpc/thrift";
    }

    @Override
    protected String getServiceKey() {
        return "im.cu.proxy.server.thrift.ThriftCacheProxyServer";
    }

    @Override
    protected CacheProxyService.Client getClient(TTransport transport) {
        return new CacheProxyService.Client(new TBinaryProtocol(new TFramedTransport(transport)));
    }

    @Override
    protected void adjustPoolConfig(PoolConfig poolConfig) {
        super.adjustPoolConfig(poolConfig);
        poolConfig.setConnectTimeout(1000_000);
        poolConfig.setSocketTimeout(1000_000);
    }

    public void add(CacheKey cacheKey, CacheType cacheType, int value, Date date) {
        try {
            CacheProxyService.Iface iface = getClientPool().iface();
            CacheProxyAddRes res = iface.add(new CacheProxyAddReq(cacheKey, cacheType, value, date.getTime()));
        } catch (TException | ThriftClientPoolException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void add(CacheKey cacheKey, CacheType cacheType, int value) {
        this.add(cacheKey, cacheType, value, new Date());
    }

    public List<Integer> findContains(CacheKey cacheKey, CacheConfig cacheConfig, List<Integer> values) {
        try {
            CacheProxyService.Iface iface = getClientPool().iface();
            CacheProxyFilterRes contains = iface.findContains(new CacheProxyFilterReq(cacheKey, cacheConfig, values));
            return contains.values;
        } catch (TException | ThriftClientPoolException e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    public List<Integer> findNotContains(CacheKey cacheKey, CacheConfig cacheConfig, List<Integer> values) {
        try {
            CacheProxyService.Iface iface = getClientPool().iface();
            CacheProxyFilterRes notContains = iface.findNotContains(new CacheProxyFilterReq(cacheKey, cacheConfig, values));
            return notContains.values;
        } catch (TException | ThriftClientPoolException e) {
            logger.error(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }
}
