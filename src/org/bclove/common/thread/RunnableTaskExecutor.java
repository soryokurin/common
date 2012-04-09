package org.bclove.common.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RunnableTaskExecutor {
	
    protected final Log logger = LogFactory.getLog(RunnableTaskExecutor.class);
    
	private RunnableTaskExecutor(){}
	
	private static final RunnableTaskExecutor rte = new RunnableTaskExecutor();
	
	private static final Executor executor = Executors.newCachedThreadPool();
	
	public static RunnableTaskExecutor getInstance()
	{
		return rte;
	}
	
	public void runTask(Runnable task,String taskName)
	{
		logger.info("### Task is execute :" + taskName );
		executor.execute(task);
	}
}
