
package org.bclove.common.taskha;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;


/**
 * 基于一致性哈希的负载均衡策略：</br>
 * <ul>
 * <li>将所有consumer组织成一个环</li>
 * <li>将所有分区根据hash值插入到环上</li>
 * <li>获取指定consumer前面，前一个consumer之后的分区列表作为结果</li>
 * </ul>
 * 
 * @author soryokurin
 * @date 2012-04-05
 * 
 */
public class ConsisHashStrategy implements LoadBalanceStrategy {
    // 一个consumer虚拟节点数目，一致性hash总的虚拟节点数为Num(consumer)*NUM_REPS,一般来说节点数越小需要NUM_REPS越大才能保证hash的均匀
    static final int NUM_REPS = 1600;
    HashAlgorithm alg = HashAlgorithm.XFEED_HASH;


    /**
     * Get the md5 of the given key.
     */
    public static byte[] computeMd5(final String k) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        }
        catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        md5.reset();
        md5.update(k.getBytes());
        return md5.digest();
    }


    @Override
    public List<String> getPartitions(final String topic, final String consumerId, final List<String> curConsumers,
            final List<String> curPartitions) {

        final TreeMap<Long, String> consumerMap = this.buildConsumerMap(curConsumers);

        final Set<String> rt = new HashSet<String>();
        // 根据partition查找对应的consumer
        for (final String partition : curPartitions) {
            final String targetConsumer = this.findConsumerByPartition(consumerMap, partition);
            // 保存本consumer需要挂载的分区
            if (consumerId.equals(targetConsumer)) {
                rt.add(partition);
            }
        }
        return new ArrayList<String>(rt);
    }


    private String findConsumerByPartition(final TreeMap<Long, String> consumerMap, final String partition) {
        final Long hash = this.alg.hash(partition);
        Long target = hash;
        if (!consumerMap.containsKey(hash)) {
            target = consumerMap.ceilingKey(hash);
            if (target == null && !consumerMap.isEmpty()) {
                target = consumerMap.firstKey();
            }
        }
        final String targetConsumer = consumerMap.get(target);
        return targetConsumer;
    }


    private TreeMap<Long, String> buildConsumerMap(final List<String> curConsumers) {
        final TreeMap<Long/* hash */, String/* consumerId */> consumerMap = new TreeMap<Long, String>();
        for (final String consumer : curConsumers) {
            if (this.alg == HashAlgorithm.XFEED_HASH) {
                for (int i = 0; i < NUM_REPS / 4; i++) {
                    final byte[] digest = HashAlgorithm.computeMd5(consumer + "-" + i);
                    for (int h = 0; h < 4; h++) {
                        final long k =
                                (long) (digest[3 + h * 4] & 0xFF) << 24 | (long) (digest[2 + h * 4] & 0xFF) << 16
                                        | (long) (digest[1 + h * 4] & 0xFF) << 8 | digest[h * 4] & 0xFF;
//                        System.out.println("key "+k);
                        consumerMap.put(k, consumer);
                    }

                }
            }
            else {
                for (int i = 0; i < NUM_REPS; i++) {
                    final long key = this.alg.hash(consumer + "-" + i);
                    consumerMap.put(key, consumer);
                }
            }

        }
        return consumerMap;
    }
    
    public static void main(String[] args){
    	
    	List<String> list = new ArrayList<String>();
    	for(int i=0;i<100;i++){
    		list.add("consumer"+i);
    	}
    	
    	List<String> list1 = new ArrayList<String>();
    	for(int i=0;i<1000;i++){
    		list1.add("partition"+i);
    	}
    	
    	for(String con1:list){
    	ConsisHashStrategy con = new ConsisHashStrategy();
    	
    	
    	List<String> list3 = con.getPartitions("news", con1, list, list1);
    	System.out.println(list3.size());
    	}
    	
    	
    	
    }
}