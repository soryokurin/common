package org.bclove.common.taskha;

import java.util.ArrayList;
import java.util.List;

/**
 * 负载均衡型task 测试demo
 * @author soryokurin
 *
 */
public class LoadBalanceTestTask extends LoadBalanceTask {

	
	public static void main(String[] args){
		LoadBalanceTestTask task = new LoadBalanceTestTask();
		
		//消费队列所有list，List的每个值可以唯一标识一个Queue
		List<String> allQueueList = new ArrayList<String>();
		for (int i = 0; i < 100; i++) {
			allQueueList.add("" + i);
		}

		//task 注册
		task.registerTask(ITaskRegister.POOL_ENGINE,"loadbalance",LoadBalanceTask.DEFAULT_STRATEGY,allQueueList);
		
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void process(){
		
		//取得当前task消费的队列List
		List<String> curQueues = this.getCurQueueList();
		
		//根据queue唯一标识进行相应的队列消费
		for(String queue:curQueues){
			System.out.println(queue+" task start");
		}
	}

}
