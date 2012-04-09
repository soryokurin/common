package org.bclove.common.taskha;

public interface ITaskRegister {
	
	public static final String ROOT = "/task/";
	
	public static final String POOL_SITE = ROOT + "site/";
	
	public static final String POOL_API = ROOT + "api/";
	
	public static final String POOL_ENGINE = ROOT + "engine/";
	
	public static final String POOL_GAME = ROOT + "game/";
	
	public static final String MASTER_SLAVE = "ms";
	
	public static final String LOAD_BALANCE ="loadbalance";
	
	/**
	 * 真正的工作在这里面执行
	 */
	public void process();

}
