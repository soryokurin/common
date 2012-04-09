package org.bclove.common.taskha;

/**
 * masterslave 型task 测试demo
 *
 */
public class MasterSlaveTestTask extends MasterSlaveTask {

	
	public static void main(String[] args){
		MasterSlaveTestTask task = new MasterSlaveTestTask();
		task.registerTask(ITaskRegister.POOL_ENGINE,"masterslavetest","task");
		
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void process(){
		System.out.println("task start");
	}

}
