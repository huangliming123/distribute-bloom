package im.cu.proxy;

import im.cu.api.grpc.factory.CacheStoreRpcServiceFactory;
import im.cu.api.match_smart_cache.thrift.CacheKey;
import im.cu.api.match_smart_cache.thrift.CacheProxyAddReq;
import im.cu.api.match_smart_cache.thrift.CacheType;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.store.CacheStore;
import im.cu.grpc.api.server.cache.store.CacheStoreServiceGrpc;
import im.cu.proxy.server.thrift.ThriftCacheProxyServer;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CacheProxyApplicationTests {

	@Autowired
	private CacheStoreRpcServiceFactory factory;

	@Autowired
	private ThriftCacheProxyServer thriftCacheProxyServer;

	@Test
	void contextLoads() {
		CacheStoreServiceGrpc.CacheStoreServiceBlockingStub blockingStub = factory.createBlockingStub(Common.HostAndPort.newBuilder()
				.setHost("10.0.18.75").setPort(18888).build());
		CacheStore.CacheStoreAddRes aloha = blockingStub.add(
				CacheStore.CacheStoreAddReq.newBuilder()
						.setCacheType(Common.CacheType.Full)
						.setCacheKey(Common.CacheKey.newBuilder().setPrefix("aloha").setUserId(451).build())
						.setValue(124115).setDate(System.currentTimeMillis()).build()
		);
		System.out.println(aloha.getResponseCode());
	}


	@Test
	void testLongPolling() {
		CacheStoreServiceGrpc.CacheStoreServiceBlockingStub stub
				= factory.createBlockingStub(Common.CacheKey.newBuilder().setPrefix("aloha").setUserId(1).build());
		CacheStore.CacheStoreAddRes aloha = stub.add(
				CacheStore.CacheStoreAddReq.newBuilder()
						.setCacheType(Common.CacheType.Full)
						.setCacheKey(Common.CacheKey.newBuilder().setPrefix("aloha").setUserId(1).build())
						.setValue(124115).setDate(System.currentTimeMillis()).build()
		);
	}

	@Test
	void testThrift() throws TException {
		thriftCacheProxyServer.add(new CacheProxyAddReq(new CacheKey("aloha", 13), CacheType.Full, 123, System.currentTimeMillis()));
	}
}
