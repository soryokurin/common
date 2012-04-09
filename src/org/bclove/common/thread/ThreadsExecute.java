package org.bclove.common.thread;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多线程工具类
 * @author soryokurin
 *
 */
public class ThreadsExecute
{
	//是否超时
	boolean isTimeout;
	
	
	/**
	 * cached 线程池::60秒未使用的线程自动关闭
	 */
	private static ExecutorService espool = Executors.newCachedThreadPool();

	private int maxthread;

	private int maxWaitingTimes;

	/**
	 * 构造函数
	 * 
	 * @param threadCount
	 *            需要使用的总线程数
	 * @param tasks
	 *            任务个数
	 * @param mainThreadWating
	 *            主线程单位等待时间：(ms)在开启threadCount个线程后，主线程将进入sleep，每次等待mainThreadWating毫秒
	 *            ，如传递0则表示主线程不等待，
	 * @param maxWaitingTime
	 *            主线程等待超时次数,等待时间~=maxWaitingTime * mainThreadWating
	 */
	public ThreadsExecute(int threadCount, Vector<Runnable> tasks,
			int mainThreadWating, int maxWaitingTimes)
	{
		this.maxthread = threadCount;
		this.tasks = tasks;
		this.mainThreadWating = mainThreadWating;
		this.maxWaitingTimes = maxWaitingTimes;
	}

	private Vector<Runnable> tasks = null;

	/**
	 * 构造函数
	 * 
	 * @param threadCount
	 *            需要使用的总线程数
	 * @param tasks
	 *            任务个数
	 * @param mainThreadWating
	 *            主线程单位等待时间：在开启threadCount个线程后，主线程将进入sleep，每次等待mainThreadWating毫秒
	 *            ，如传递0则表示主线程不等待，
	 */
	public ThreadsExecute(int threadCount, Vector<Runnable> tasks,
			int mainThreadWating)
	{
		this.maxthread = threadCount;
		this.tasks = tasks;
		this.mainThreadWating = mainThreadWating;
	}

	/**
	 * 构造函数
	 * 
	 * @param threadCount
	 *            需要使用的总线程数
	 * @param tasks
	 *            任务个数
	 */
	public ThreadsExecute(int threadCount, Vector<Runnable> tasks)
	{
		this.maxthread = threadCount;
		this.tasks = tasks;
	}

	// 正在执行的线程
	private int runningThread;

	// 判断线程是否结束时的等待时间
	private int mainThreadWating;

	/**
	 * <ul>
	 * <li>减少一个线程</li>
	 * </ul>
	 */
	public synchronized void dec()
	{
		runningThread--;
	}

	/**
	 * <ul>
	 * <li>执行计算</li>
	 * </ul>
	 */
	public void execute()
	{
		runningThread = this.maxthread;
		// 这里考虑各线程执行不可能有这么快了，呵呵
		for (int i = 0; i < maxthread /* && tasks.size()>0 */; i++)
		{
			espool.execute(new Runnable()
			{
				@Override
				public void run()
				{
					while (!isTimeout && tasks.size()>0)
					{
						try
						{
							Runnable run = null;
							synchronized(tasks)
							{
								if(tasks.size()>0)
								{
									run = tasks.remove(0);
								}
							}
							
							if(run != null)
							run.run();
						} catch (Exception e)
						{
//							e.printStackTrace();
						}
					}

					dec();
				}
			});
		}

		if (mainThreadWating > 0)
		{// 主要线程需要等待

			if (maxWaitingTimes > 0)
			{// 有等待超时限制
				int times = 0;

				while (this.runningThread > 0 && !isTimeout)
				{
					times++;
					isTimeout = maxWaitingTimes < times;

					try
					{
						Thread.sleep(mainThreadWating);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			} else
			{// 没有等待超时
				while (this.runningThread > 0)
				{
					try
					{
						Thread.sleep(mainThreadWating);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void main(String[] args)
	{
		// for (int m = 0; m < 1000; m++)
		// {
		Vector<Runnable> v = new Vector<Runnable>(20);
		for (int i = 0; i < 20; i++)
		{
			v.add(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						int j = (int) (100);
						Thread.sleep(j);
						System.out.println(Thread.currentThread().getId()
								+ "\t-----------------\t" + j);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			});
		}

		ThreadsExecute e = new ThreadsExecute(10, v, 10,3);
		e.execute();
		// }

		System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
	}
}