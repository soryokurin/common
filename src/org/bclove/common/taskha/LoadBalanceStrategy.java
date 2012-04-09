package org.bclove.common.taskha;

import java.util.List;


/**
 * Consumer的balance策略
 * 
 * @author soryokurin
 * @date 2012-04-05
 * 
 */
public interface LoadBalanceStrategy {

    enum Type {
        DEFAULT,
        CONSIST
    }


    /**
     * 根据consumer id查找对应的分区列表
     * 
     * @param topic
     *            分区topic
     * @param consumerId
     *            consumerId
     * @param curConsumers
     *            当前所有的consumer列表
     * @param curPartitions
     *            当前的分区列表
     * 
     * @return
     */
    public List<String> getPartitions(String topic, String consumerId, final List<String> curConsumers,
            final List<String> curPartitions);
}