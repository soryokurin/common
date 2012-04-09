package org.bclove.common.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bclove.common.store.DBManager;

/**
 * 生成主键ID，常用于散表的数字ID的生成
 * 该实现方式考虑到多并发的情况，ID不会有重复
 *
 */
public class SequenceGenerator {
	
	public static SequenceGenerator getInstance(){
		if (instance == null) {
			synchronized (SequenceGenerator.class) {
				if (instance == null)
					instance = new SequenceGenerator();
			}
		}

		return instance;
	}
	
	
	private static SequenceGenerator instance = new SequenceGenerator();
	
	
	private SequenceGenerator(){
	}
	
	
	
	/** 获取下一个SequenceID 。
	 */
	public long getNextId(String seqName){
		Connection c = null;
		PreparedStatement pt = null;
		Statement st = null;
		ResultSet rs = null;
		long b = 0;
		try {
			c = DBManager.getInstance().getConnection();
			
			pt = c.prepareStatement("update sequence SET seq_value=last_insert_id(seq_value+1) where seq_name = ?");
			pt.setString(1, seqName);
			pt.executeUpdate();
			
			st = c.createStatement();
			rs = st.executeQuery("select last_insert_id()");
			if(rs.next()){
				b = rs.getLong(1);
			}
		} catch (Exception e1) {
			throw new RuntimeException("Generate Sequence Error",e1);
		} finally {
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if(st != null){
				try {
					st.close();
				} catch (SQLException e) {
				}
			}
			if(pt != null){
				try {
					pt.close();
				} catch (SQLException e) {
				}
			}
			if(c != null){
				try {
					c.close();
				} catch (SQLException e) {
				}
			}
		}
		return b;
	}
	
	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		ExecutorService s = new ThreadPoolExecutor(10,30,60L,TimeUnit.SECONDS,new SynchronousQueue<Runnable>());
		long t2 = System.currentTimeMillis();
		for(int j = 0;j < 1000;j++){
			final int k = j;
			s.execute(new Runnable() {

				public void run() {
					long b;
					try {
						b = getInstance().getNextId("test");
						System.out.println(k + ":" + b);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			});
		}
		long t3 = System.currentTimeMillis();
		
		System.out.println("time:" + (t2 - t1));
		System.out.println("time:" + (t3 - t1));
		
		//System.exit(0);
//		for(int i = 0 ;i < 1020;i++){
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//		}
	}
}
