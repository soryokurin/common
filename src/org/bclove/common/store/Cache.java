package org.bclove.common.store;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * The Cache. Mainly an interface to memcached.
 */
public abstract class Cache {
	  /**
     * The underlying cache implementation
     */
    private static MemcachedImpl cacheImpl = null;
    private static MemcachedImpl ttCacheImpl = null;
    
    static {
		if(cacheImpl == null){
    		try {
				cacheImpl = new MemcachedImpl(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(ttCacheImpl == null){
    		try {
    			ttCacheImpl = new MemcachedImpl(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }

    /**
     * Add an element only if it doesn't exist.
     * @param key Element key
     * @param value Element value
     * @param expiration Ex: 10s, 3mn, 8h
     * @return return true if successful
     */
    public static boolean add(String key, Object value, String expiration) {
        checkSerializable(value);
        return cacheImpl.add(key, value, TimeUtil.parseDuration(expiration));
    }

    /**
     * Add an element only if it doesn't exist and store it indefinitely.
     * @param key Element key
     * @param value Element value
     * @return return true if successful
     */
    public static boolean add(String key, Object value) {
        checkSerializable(value);
        return cacheImpl.add(key, value, TimeUtil.parseDuration(null));
    }

    /**
     * Set an element.
     * @param key Element key
     * @param value Element value
     * @param expiration Ex: 10s, 3mn, 8h
     * @return return true if successful
     */
    public static boolean set(String key, Object value, String expiration) {
        checkSerializable(value);
        return cacheImpl.set(key, value, TimeUtil.parseDuration(expiration));
    }

    /**
     * Set an element and store it indefinitely.
     * @param key Element key
     * @param value Element value
     * @return return true if successful
     */
    public static boolean set(String key, Object value) {
        checkSerializable(value);
        return cacheImpl.set(key, value, TimeUtil.parseDuration(null));
    }

    /**
     * Replace an element only if it already exists.
     * @param key Element key
     * @param value Element value
     * @param expiration Ex: 10s, 3mn, 8h
     * @return return true if successful
     */
    public static boolean replace(String key, Object value, String expiration) {
        checkSerializable(value);
        return cacheImpl.replace(key, value, TimeUtil.parseDuration(expiration));
    }

    /**
     * Replace an element only if it already exists and store it indefinitely.
     * @param key Element key
     * @param value Element value
     * @return return true if successful
     */
    public static boolean replace(String key, Object value) {
        checkSerializable(value);
        return cacheImpl.replace(key, value, TimeUtil.parseDuration(null));
    }

    /**
     * Increment the element value (must be a Number).
     * @param key Element key 
     * @param by The incr value
     * @return The new value
     */
    public static long incr(String key, int by) {
        return cacheImpl.incr(key, by);
    }

    /**
     * Increment the element value (must be a Number) by 1.
     * @param key Element key 
     * @return The new value
     */
    public static long incr(String key) {
        return cacheImpl.incr(key, 1);
    }

    /**
     * Decrement the element value (must be a Number).
     * @param key Element key 
     * @param by The decr value
     * @return The new value
     */
    public static long decr(String key, int by) {
        return cacheImpl.decr(key, by);
    }

    /**
     * Decrement the element value (must be a Number) by 1.
     * @param key Element key 
     * @return The new value
     */
    public static long decr(String key) {
        return cacheImpl.decr(key, 1);
    }

    /**
     * Retrieve an object.
     * @param key The element key
     * @return The element value or null
     */
    public static Object get(String key) {
        return cacheImpl.get(key);
    }

    /**
     * Bulk retrieve.
     * @param key List of keys
     * @return Map of keys & values
     */
    public static Map<String, Object> get(List<String> keys) {
        return cacheImpl.get(keys);
    }

    /**
     * Delete an element from the cache.
     * @param key The element key     * 
     * @return return true if successful
     */
    public static boolean delete(String key) {
        return cacheImpl.delete(key);
    }

    /**
     * Convenient clazz to get a value a class type;
     * @param <T> The needed type
     * @param key The element key
     * @param clazz The type class
     * @return The element value or null
     */
    @SuppressWarnings("unchecked")
	public static <T> T get(String key, Class<T> clazz) {
    	Object obj = cacheImpl.get(key);
    	if(obj == null || clazz.isInstance(obj)){
    		return (T) obj;
    	} else {
    		System.err.println(String.format("memcacheFail: %s is not instance of %s ;className : %s",key , clazz.getName(), obj.getClass().getName())); 
    		return null;
    	}
    }
    
    /**
     * Utility that check that an object is serializable.
     */
    static void checkSerializable(Object value) {
        if(value != null && !(value instanceof Serializable)) {
           // throw new CacheException("Cannot cache a non-serializable value of type " + value.getClass().getName(), new NotSerializableException(value.getClass().getName()));
        	System.err.println("Cannot cache a non-serializable value of type " + value.getClass().getName());
        }
    }
    
    /**
     * Add an element to tt.
     * @param key Element key
     * @param value Element value
     * @return return true if successful
     */
    public static boolean ttAdd(String key, String value){
    	checkSerializable(value);
        return ttCacheImpl.add(key, value, 0);
    }
    
    /**
     * Set an element to tt.
     * @param key Element key
     * @param value Element value
     * @return return true if successful
     */
    public static boolean ttSet(String key, String value) {
        checkSerializable(value);
        return ttCacheImpl.set(key, value, 0);
    }
    
    /**
     * Increment the element value (must be a Number).
     * @param key Element key 
     * @param by The incr value
     * @return The new value
     */
    public static long ttIncr(String key, int by) {
        return ttCacheImpl.incr(key, by);
    }

    /**
     * Increment the element value (must be a Number) by 1.
     * @param key Element key 
     * @return The new value
     */
    public static long ttIncr(String key) {
        return ttCacheImpl.incr(key, 1);
    }

    /**
     * Decrement the element value (must be a Number).
     * @param key Element key 
     * @param by The decr value
     * @return The new value
     */
    public static long ttDecr(String key, int by) {
        return ttCacheImpl.decr(key, by);
    }

    /**
     * Decrement the element value (must be a Number) by 1.
     * @param key Element key 
     * @return The new value
     */
    public static long ttDecr(String key) {
        return ttCacheImpl.decr(key, 1);
    }

    /**
     * Retrieve an object.
     * @param key The element key
     * @return The element value or null
     */
    public static String ttGet(String key) {
        return (String)ttCacheImpl.get(key); //只会是String类型对象
    }

    /**
     * Bulk retrieve.
     * @param key List of keys
     * @return Map of keys & values
     */
    public static Map<String, Object> ttGet(List<String> keys) {
        return ttCacheImpl.get(keys);
    }

    /**
     * Delete an element from the cache.
     * @param key The element key     * 
     * @return return true if successful
     */
    public static boolean ttDelete(String key) {
        return ttCacheImpl.delete(key);
    }

}
