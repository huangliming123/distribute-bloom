package im.cu.store;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import im.cu.api.bloom.ScaleBloomFilter;
import im.cu.api.grpc.factory.CacheStoreRpcServiceFactory;
import im.cu.api.register.ZkClient;
import im.cu.grpc.api.server.cache.Common;
import im.cu.grpc.api.server.cache.store.CacheStore;
import im.cu.grpc.api.server.cache.store.CacheStoreServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootTest
@Slf4j
class CacheStoreApplicationTests {

	@Autowired
	private CacheStoreRpcServiceFactory factory;

	@Autowired
	private ZkClient zkClient;

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
	void testScaleBloom() {
		ScaleBloomFilter scaleBloomFilter = new ScaleBloomFilter(1000, 0.01);
		for (int i=0; i<1000; i++) {
			scaleBloomFilter.put(i);
		}
		scaleBloomFilter.put(99999);
	}

	@Test
	void testBloomApproximateElementCount() {
		BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), 1000, 0.01);
		System.out.println(bloomFilter.approximateElementCount());
	}

	@Test
	void serialBloom() throws IOException {
		Map<Integer, BloomFilter> map = new HashMap<>();
		for (int i=0; i<300000; i++) {
			BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), 3000, 0.03);
			map.put(i, bloomFilter);
		}
		long l = System.currentTimeMillis();
		ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("/data/cache/test.txt.tmp")));
		outputStream.writeObject(map);
		outputStream.flush();
		outputStream.close();
		System.out.println(System.currentTimeMillis() - l);
	}

	@Test
	void read() throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream("/data/cache/test.txt.tmp")));
		long l = System.currentTimeMillis();
		Object o = objectInputStream.readObject();
		Map<Integer, BloomFilter> map = new HashMap(300000);
		map.putAll((Map<Integer, BloomFilter>)o);
		System.out.println(System.currentTimeMillis() - l);
	}
}








