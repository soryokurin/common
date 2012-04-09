package org.bclove.common.utils.zk;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZookeeperManager {
	
	private static Log logger = LogFactory.getLog(ZookeeperManager.class);
	
	private static ZookeeperManager instance;
	
	private ZooKeeper zookeeper;
	
	//当前进程所用到的节点有变化时，执行相应的Event（也可理解为Callback）
	private Map<String,ZookeeperNodeEvent> nodeEventMap;

	private Watcher zookeeperWatcher = new GummyZookeeperWatcher();
	
	private ZookeeperManager(){
	}
	
	public static ZookeeperManager getInstance(){
		if(instance == null){
			synchronized (logger) {
				if(instance == null){
					instance = new ZookeeperManager();
					instance.nodeEventMap = new HashMap<String,ZookeeperNodeEvent>();
					instance.initZooKeeper(); //初始化
				}
			}
		}
		return instance;
	}
	
	/**
	 * 为节点变化增加事件支持，如配置更新后，做相应的处理
	 * @param nodePath  节点完整路径
	 * @param nodeEvent 事件对象
	 */
	public void addEventToNode(String nodePath, ZookeeperNodeEvent nodeEvent){
		if(!nodeEventMap.containsKey(nodePath)){
			logger.info("watch node:" + nodePath);
			nodeEventMap.put(nodePath, nodeEvent);
		}
	}
	
	/** 初始化一个新的Zookeeper实例，如果有老的就销毁 */
    private void initZooKeeper() {
    	ZooKeeper old = zookeeper;
    	try {
    		if (logger.isInfoEnabled()) {
        		logger.info("initializing ZooKeeper");
        	}
    		
    		Properties zkConfig = new Properties();
			InputStream is = getClass().getClassLoader().getResourceAsStream("zookeeper.properties");
			if (is == null) {
				throw new RuntimeException("ZooKeeper Configuration not found. Please make sure 'zookeeper.properties' is in in your classpath");
			}
			zkConfig.load(is);
			String connectString = zkConfig.getProperty("connectString");
			int sessionTimeout = Integer.parseInt(zkConfig.getProperty("sessionTimeout"));
	    	zookeeper = new ZooKeeper(connectString, sessionTimeout, zookeeperWatcher);
    	} catch (IOException e) {
    		throw new RuntimeException("Error occurs while creating ZooKeeper instance.", e);
		} finally {	//确保老的资源能顺利被释放
	    	if(old != null){
	    		if (logger.isInfoEnabled()) {
	        		logger.info("closing expired ZooKeeper:" + old.hashCode());
	        	}
	        	try {
					old.close();	//将旧的关闭掉
				} catch (InterruptedException e) {
					logger.error("zookeeper error", e);
				}
	    	}
    	}
    }
    
    /**
	 * 刷新数据, 防止重连时可能造成的信息丢失
	 * 
	 * 刷新数据的时候会重新注册Watcher，也能解决Watcher丢失的问题
	 */
    private void refreshZookeeperData() {
    	Iterator<String> it = nodeEventMap.keySet().iterator();
    	while(it.hasNext()){
    		String nodePath = it.next();
    		ZookeeperNodeEvent nodeEvent = nodeEventMap.get(nodePath);
    		nodeEvent.onNodeDataChanged(nodePath);
    	}
	}
    
    /**
     * 从ZooKeeper的Server获取内容，组装为Properties对象(一般为配置信息)
     * @param path 结点的完整路径
     * @return 如果不存在结点，就返回null
     */
    public Properties getDataAsProperties(String nodePath){
		Properties newConf = new Properties();
		try {
			newConf.load(new StringReader(getDataAsString(nodePath)));
			return newConf;
		} catch (IOException e) {
			//基本上不发生
			throw new RuntimeException("Error occurs while getData from ZooKeeper ,path:" + nodePath, e);
		}
    }
    
    public boolean createAndMonitorEphemeralNode(String path,String data,String monitorPath){
    	try {
    		Stat s = zookeeper.exists(path, false);
    		if(monitorPath!=null&&!"".equals(monitorPath)){
    			zookeeper.exists(monitorPath, true);
    		}
    		if (s == null) {
    		   zookeeper.create(path, data.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    		   return false;
    		}
    		
    		
    		
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	return true;
    }
    
    public boolean createAndMonitorEphemeralSeqNode(String path,String data,String monitorPath){
    	try {
    		Stat s = zookeeper.exists(path, false);
    		if(monitorPath!=null&&!"".equals(monitorPath)){
    			zookeeper.exists(monitorPath, true);
    		}
    		if (s == null) {
    		   zookeeper.create(path, data.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    		   return false;
    		}
    		
    		
    		
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	return true;
    }
    
    public boolean createNode(String path,String data){
    	try {
    		Stat s = zookeeper.exists(path, false);
    		if (s == null) {
    		   zookeeper.create(path, data.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    		   return false;
    		}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	return true;
    }
    
   
    
//    /**
//     * 从ZooKeeper的Server获取内容，解析为JsonElement对象(一般为配置信息)
//     * @param path 结点的完整路径
//     * @return 如果不存在结点，就返回null
//     */
//    public JsonElement getDataAsJson(String nodePath){
//		JsonElement jsonElm;
//		try {
//			jsonElm = parser.parse(getDataAsString(nodePath));
//			return jsonElm;
//		} catch (JsonSyntaxException e) {
//			//不是json格式
//			throw new RuntimeException("Error occurs while getData from ZooKeeper(Json Syntax Error) ,path:" + nodePath, e);
//		}
//    }
    
    /**
     * 从ZooKeeper的Server获取内容字符串
     * @param path 结点的完整路径
     * @return 如果不存在结点，就返回null
     */
    public String getDataAsString(String nodePath){
    	try{
    		String str = null;
    		int retryTimes = 5; //3台ZooKeeper服务器，防止其中一个服务器连接不上或者长时间连接被断开，最多重试5次
    		while(retryTimes > 0){
    			try{
	    			byte[] data = zookeeper.getData(nodePath, true, null);
	    			//System.out.println(str);
	    			try {
						return new String(data, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						//一般不发生这个异常
						throw new RuntimeException("charsetName is invalid");
					}
    			}catch(KeeperException.ConnectionLossException e){
    				//如果是3台当中某一个ZooKeeper服务器不能用，可尝试连接其他的服务器，不影响获取数据
    				//看到以下警告，通知系统维护人员，重新启用
    				logger.warn("WARN: ZooKeeper ConnectionLossException ,please make sure all ZookeeerServers are avaliabled!!!");
    				retryTimes--;
    			}
    		}
    		return str;
    	}catch (KeeperException e){
    		//ConnectionLossException
    		if(e instanceof KeeperException.NoNodeException){
    			return null;//不存在这个结点，返回null
    		}else {
    			throw new RuntimeException("Error occurs while getData from ZooKeeper ,path:" + nodePath, e);
    		}
    	} catch (InterruptedException e) {
    		throw new RuntimeException("Error occurs while getData from ZooKeeper ,path:" + nodePath, e);
		}
    }
    
    public List<String> getChildNodes(String nodePath){
    	return getChildNodes(nodePath,false);
    }
    
    public int incrData(String nodePath){
    	try {
    		
    		Stat s = zookeeper.exists(nodePath, true);
    		if (s == null) {
    		   zookeeper.create(nodePath, "1".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    		   return 1;
    		}else{
    		   String value = getDataAsString(nodePath);
    		   int val = NumberUtils.toInt(value,1)+1;
    		   zookeeper.setData(nodePath, String.valueOf(val).getBytes(), -1);
    		   return val;
    		}
			
			
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	   return 0;
    }
    
    public List<String> getChildNodes(String nodePath, boolean watch){
    	try {
    		List<String> children = null;
    		int retryTimes = 5; //3台ZooKeeper服务器，防止其中一个服务器连接不上或者长时间连接被断开，最多重试5次
    		while(retryTimes > 0){
    			try {
    				children = zookeeper.getChildren(nodePath, watch);
    				break;
    			}catch(KeeperException.ConnectionLossException e){
    				//如果是3台当中某一个ZooKeeper服务器不能用，可尝试连接其他的服务器，不影响获取数据
    				//看到以下警告，通知系统维护人员，重新启用
    				logger.warn("WARN: ZooKeeper ConnectionLossException ,please make sure all ZookeeerServers are avaliabled!!!");
    				retryTimes--;
    			}
    		}
			return children;
		} catch (KeeperException e) {
			throw new RuntimeException("Error occurs while getData from ZooKeeper ,path:" + nodePath, e);
		} catch (InterruptedException e) {
			throw new RuntimeException("Error occurs while getData from ZooKeeper ,path:" + nodePath, e);
		}
    }

    private class GummyZookeeperWatcher implements Watcher {
		public void process(WatchedEvent event) {
			if (logger.isDebugEnabled()) {
				logger.debug("WatchedEvent received: " + event);
			}
			logger.info("event: " + event.getType() + ",path: "
					+ event.getPath());
			if (event.getType() == Event.EventType.None) {
				// We are are being told that the state of the
				// connection has changed
				switch (event.getState()) {
				case SyncConnected:
					if (logger.isInfoEnabled()) {
						logger.info("Zookeeper SyncConnected: " + event);
					}
					try {
						refreshZookeeperData(); // 刷新数据
					} catch (Exception e) {
						if (e instanceof ConnectionLossException
								|| (e.getCause() != null && e.getCause() instanceof ConnectionLossException)) {

							// 如果是ConnectionLossException引起的，重连
							initZooKeeper(); // 重连成功后会自动进入SyncConnected状态，所以会执行refreshZookeeperData()操作
						} else {
							logger.error(
									"Error while refreshing data from ZooKeeper on event: "
											+ event, e);
						}
					}
					break;
				case Expired:
					logger.error("Zookeeper session expired:" + event);
					initZooKeeper(); // 重连成功后会自动进入SyncConnected状态，所以会执行refreshZookeeperData()操作
					break;
				}
			} else if (event.getType() == Event.EventType.NodeDataChanged) {
				String path = event.getPath();
				ZookeeperNodeEvent nodeEvent = nodeEventMap.get(path);
				if(nodeEvent != null){
					nodeEvent.onNodeDataChanged(path);
				}
			} else if (event.getType() == Event.EventType.NodeChildrenChanged
					|| event.getType() == Event.EventType.NodeDeleted
					|| event.getType() == Event.EventType.NodeCreated) {
				/*
				 * if (logger.isInfoEnabled()) { logger.info("node deleted:" +
				 * path); }
				 */
				String path = event.getPath();
				ZookeeperNodeEvent nodeEvent = nodeEventMap.get(path);
				if(nodeEvent != null){
					nodeEvent.onOtherEvent(path, event.getType());
				}
				
			} else {
				logger.warn("Unhandled event:" + event);
			}
		}
	}
    
    public static void main(String[] args){
//    	ZookeeperManager.getInstance().createOrGetEphemeralNode("/feedTask/leader", "task1");
////    	ZookeeperManager.getInstance().createEphemeralNode("/feedTask/task2", "task2");
//    	String childs = ZookeeperManager.getInstance().getDataAsString("/feedTask/leader");
//    	System.out.println(childs);
    }
}
