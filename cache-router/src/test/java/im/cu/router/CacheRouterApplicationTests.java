package im.cu.router;

import im.cu.api.grpc.factory.CacheProxyRpcServiceFactory;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.proxy.CacheProxy;
import im.cu.grpc.api.server.cache.proxy.CacheProxyServiceGrpc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CacheRouterApplicationTests {

	@Autowired
	private CacheProxyRpcServiceFactory proxyRpcServiceFactory;

	@Autowired
	private SlotService slotService;

	@Test
	void contextLoads() {
		CacheProxyServiceGrpc.CacheProxyServiceBlockingStub blockingStub = proxyRpcServiceFactory.createBlockingStub(
				Common.HostAndPort.newBuilder().setHost("192.168.1.3").setPort(19090).build()
		);
		CacheProxy.CacheProxyAddRes aloha = blockingStub.add(
				CacheProxy.CacheProxyAddReq.newBuilder()
						.setCacheType(Common.CacheType.Full)
						.setCacheKey(Common.CacheKey.newBuilder().setPrefix("aloha").setUserId(451).build())
						.setValue(124115).setDate(System.currentTimeMillis()).build()
		);
		System.out.println(aloha.getResponseCode());
	}

	@Test
	void testAutoReShard() {
		slotService.autoReShard();
	}
}
