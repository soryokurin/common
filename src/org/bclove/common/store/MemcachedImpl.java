package org.bclove.common.store;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class MemcachedImpl {
	
	private MemcachedClient client;

	protected MemcachedImpl(boolean isTTStore) throws Exception {
		Properties configuration = new Properties();
		configuration.load(MemcachedImpl.class.getResourceAsStream("/config.properties"));
		
		List<InetSocketAddress> addrList = new ArrayList<InetSocketAddress>();
		int nb = 1;
		String prefix = isTTStore ? "ttcached." : "memcached.";
		while (configuration.containsKey(prefix + nb + ".host")) {
			String hostandport = configuration.getProperty(prefix + nb + ".host");
			String host = StringUtils.substringBefore(hostandport, ":");
			String portStr = StringUtils.substringAfter(hostandport, ":");
			int port = NumberUtils.toInt(portStr, 11211);
			System.out.println("memcached init [host:" + host + ",port:" + port + ",portStr:" + portStr + "]");
			addrList.add(new InetSocketAddress(host,port));
			nb++;
		}
		
		if(addrList.size() == 0) {
			throw new Exception("Bad configuration for memcached or tt store");
		}

		MemcachedClientBuilder builder = new XMemcachedClientBuilder(addrList);
		try {
			builder.setConnectionPoolSize(5);
			this.client = builder.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void ensureOpen() throws IOException {
		if (this.client == null || this.client.isShutdown())
			throw new IOException("Client is closed!");
	}
	
	public boolean add(String key, Object value, int expiration) {
		boolean suc = false;
		try {
			ensureOpen();
			suc = this.client.add(key, expiration, value);
			
			if(!suc){
				System.err.println(String.format("memcacheFail(add),key=%s", key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return suc;
	}

	public Object get(String key) {
		Object val = null;
		try {
			ensureOpen();
			val = this.client.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return val;
	}

	public boolean delete(String key) {
		boolean suc = false;

		try {
			ensureOpen();
			suc = this.client.delete(key);
			
//			if(!suc){
//				System.err.println(String.format("memcacheFail(delete),key=%s", key));
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return suc;
	}
	
	public Object getWithException(String key) throws TimeoutException, InterruptedException, MemcachedException {
		return client.get(key);
	}

	public Map<String, Object> get(List<String> keys) {
		Map<String, Object> val = null;
		try {
			ensureOpen();
			val = this.client.get(keys);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return val;
	}

	public long incr(String key, int by) {
		long num = 0;
		try {
			ensureOpen();
			num = this.client.incr(key, by);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return num;
	}

	public long decr(String key, int by) {
		long num = 0;

		try {
			ensureOpen();
			num = this.client.decr(key, by);
		} catch (Exception e) {
			
			e.printStackTrace();
		}

		return num;
	}

	public boolean replace(String key, Object value, int expiration) {
		boolean suc = false;
		try {
			ensureOpen();
			suc = this.client.replace(key, expiration, value);
			
			if(!suc){
				System.err.println(String.format("memcacheFail(replace),replace=%s", key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return suc;
	}

	public boolean set(String key, Object value, int expiration) {
		boolean suc = false;
		try {
			ensureOpen();
			suc = this.client.set(key, expiration, value);
			
			if(!suc){
				System.err.println(String.format("memcacheFail(set),key=%s", key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return suc;
	}

	public static void main(String[] args) {
		String hostandport = "192.168.10.11:11218";
		String host = StringUtils.substringBefore(hostandport, ":");
		Integer port = NumberUtils.toInt(StringUtils.substringAfter(hostandport, ":"), 11211);
		
		System.out.println(host);
		System.out.println(port);
	}
}