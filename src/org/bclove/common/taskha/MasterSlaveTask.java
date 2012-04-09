package org.bclove.common.taskha;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.bclove.common.utils.zk.ZookeeperManager;
import org.bclove.common.utils.zk.ZookeeperNodeEvent;

/**
 * MasterSlave型task的父类
 * 第一个task实例默认为Master，每个task实例启动后会在zk中生成相应的结点，结点按序号递增
 * 每个Task实例自动监控它的前一个Task，前一个Task down 掉后触发事件，slave->master
 *
 */
public abstract class MasterSlaveTask implements ITaskRegister {

	private String monitorPath;

	/**
	 * task 注册
	 * @param pool task所属的pool，详情见ITaskRegister中定义的常量
	 * @param group task 所属的分组
	 * @param name task的名字前缀
	 */
	public void registerTask(String pool, String group, String name) {

		String path = pool + group + "/" + name;
		ZookeeperManager.getInstance().createNode(pool + group, "");
		monitorPath = getMonitorPath(pool + group);
		ZookeeperManager.getInstance().createAndMonitorEphemeralSeqNode(path,
				"1", monitorPath);
		
		//根据monitorPath是否为空来确认是否为Master
		if (!StringUtils.isEmpty(monitorPath)) {
			ZookeeperManager.getInstance().addEventToNode(monitorPath,
					new TaskNodeEvent());
		} else {
			process();
		}

	}

	private static String getMonitorPath(String parent) {
		List<String> childList = ZookeeperManager.getInstance().getChildNodes(
				parent);
		if (childList != null && childList.size() != 0) {
			Collections.sort(childList);
			int size = childList.size();
			return parent + "/" + childList.get(size - 1);
		} else {
			return "";
		}
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

			System.out.println(path);
			if (path.equals(monitorPath)
					&& eventType == Event.EventType.NodeDeleted) {
				process();
			}

		}

	}

	public abstract void process();

}
