package org.bclove.common.utils.zk;

import org.apache.zookeeper.Watcher.Event.EventType;

public interface ZookeeperNodeEvent {
	
	public void onNodeDataChanged(String path);
	
	public void onOtherEvent(String path, EventType eventType);
	
}