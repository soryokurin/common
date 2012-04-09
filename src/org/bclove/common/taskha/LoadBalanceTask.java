package org.bclove.common.taskha;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.zookeeper.Watcher.Event.EventType;
import org.bclove.common.utils.zk.ZookeeperManager;
import org.bclove.common.utils.zk.ZookeeperNodeEvent;

/**
 * 需要对多个task进行负载均衡的task的父类
 * 有2种负载均衡策略，默认为根据task平均分配，另一个为按一致性hash算法进行分配
 * @author soryokurin
 *
 */
public abstract class LoadBalanceTask implements ITaskRegister {
	
	public static final String DEFAULT_STRATEGY = "default";
	public static final String CONSISHASH_STARTEGY = "consishash";

	private String taskName;

	private String group;

	private String pool;

	private String monitorPath;
	
	private String strategy = DEFAULT_STRATEGY;
	
	private List<String> allQueueList;
	
	private List<String> curQueueList;

	public List<String> getCurQueueList() {
		return curQueueList;
	}


	public void setCurQueueList(List<String> curQueueList) {
		this.curQueueList = curQueueList;
	}


	/**
	 * 注册Task
	 * @param pool task所属pool
	 * @param group task 所属group
	 * @param strategy task的负载均衡策略，默认为平均分布，另一种为一致性hash
	 * @param allQueueList 所有待消费的Queue 列表，List的值(String),可以唯一标识队列
	 */
	public void registerTask(String pool, String group, String strategy ,List<String> allQueueList) {

		this.pool = pool;
		this.group = group;
		this.taskName = UUID.randomUUID().toString().replace("-", "");
		this.strategy = strategy;
		this.allQueueList = allQueueList;
		String path = pool + group + "/" + taskName;
		monitorPath = pool + group;
		ZookeeperManager.getInstance().createNode(pool + group, "");
		ZookeeperManager.getInstance().createAndMonitorEphemeralNode(path,
				taskName, monitorPath);

		ZookeeperManager.getInstance().addEventToNode(monitorPath,
					new TaskNodeEvent());
		
		rebalance();
		
		process();

	}


	/**
	 * ZK事件响应
	 * 
	 * @author lvlin zeng
	 * 
	 */
	private class TaskNodeEvent implements ZookeeperNodeEvent {

		@Override
		public void onNodeDataChanged(String path) {

		}

		@Override
		public void onOtherEvent(String path, EventType eventType) {
			System.out.println("rebalance start");
			//重新进行负载均衡，发生在新任务启动和任务down掉的情况
			rebalance();
			process();

		}

	}

	/**
	 * 重新进行负载均衡
	 */
	private void rebalance(){
		LoadBalanceStrategy strategy = null;
		if (DEFAULT_STRATEGY.equals(this.strategy)) {
			strategy = new DefaultLoadBalanceStrategy();
		} else {
			strategy = new ConsisHashStrategy();
		}

		String parent = this.pool+this.group;
		//取得所有的task列表
		List<String> childList = ZookeeperManager.getInstance().getChildNodes(parent,true);
		List<String> curConsumers = new ArrayList<String>();
		for (String str:childList) {
			curConsumers.add(str);
		}
		
		//根据负载均衡策略计算出当前任务消费的队列列表
		curQueueList = strategy.getPartitions("feed", this.taskName,
				curConsumers, this.allQueueList);
	}
	

	/**
	 * 实际task所做的工作
	 */
	public abstract void process();
	

}
