package im.cu.api.bloom;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangliming on 2021/3/30
 */
public class ScaleBloomFilter implements Serializable {

    private static final long serialVersionUID = -6579887010221766576L;
    private List<BloomFilterWrapper> bloomFilters;
    private int expectedInsertions;
    private double fpp;

    public ScaleBloomFilter(int expectedInsertions, double fpp) {
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        bloomFilters = new ArrayList<>();
        bloomFilters.add(new BloomFilterWrapper(expectedInsertions, fpp));
    }

    public void put(int value) {
        BloomFilterWrapper bloomFilter = bloomFilters.get(bloomFilters.size() - 1);
        if (bloomFilter.size() >= expectedInsertions) {
            bloomFilter = new BloomFilterWrapper(expectedInsertions, fpp);
            bloomFilters.add(bloomFilter);
        }
        bloomFilter.put(value);
    }

    public boolean mightContain(int value) {
        return bloomFilters.stream().anyMatch(bloomFilter -> bloomFilter.mightContain(value));
    }

    private class BloomFilterWrapper implements Serializable{
        private static final long serialVersionUID = 3890016822208474607L;
        private int count;
        private BloomFilter<Integer> bloomFilter;

        public BloomFilterWrapper(int expectedInsertions, double fpp) {
            this.count = 0;
            bloomFilter = BloomFilter.create(Funnels.integerFunnel(), expectedInsertions, fpp);
        }

        public void put(int value) {
            count++;
            bloomFilter.put(value);
        }

        public boolean mightContain(int value) {
            return bloomFilter.mightContain(value);
        }

        public int size() {
            return count;
        }
    }
}
