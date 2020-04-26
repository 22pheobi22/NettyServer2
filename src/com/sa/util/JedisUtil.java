package com.sa.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.SetParams;

public class JedisUtil {
	protected static Logger logger = LoggerFactory.getLogger(JedisUtil.class);
	public static ReentrantLock lock = new ReentrantLock();
	private final String DIST_LOCK_SUCCESS = "OK";
	private final Long DIST_LOCK_RELEASE_SUCCESS = 1L;
	private JedisPoolUtil jedisPool = new JedisPoolUtil();

	public boolean setString(String key, String value) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.set(key, value);
			return true;
		} catch (Exception e) {
			System.err.println("setString() key{"+key+"} value{"+value+"} 【"+e.getMessage()+"】");
			//logger.debug("setString() key {} throws:{}", key, e.getMessage());
			return false;
		} finally {
			close(jedis);
		}
	}

	// 模糊查询key
	public Set<String> scanKeys(String key) {
		Jedis jedis = jedisPool.getJedis();
		Set<String> list = new HashSet<>();
		try {
			String cursor = ScanParams.SCAN_POINTER_START;
			boolean cycleIsFinished = false;
			
			ScanParams scanParams = new ScanParams();
			scanParams.count(5000);
			scanParams.match(key + "*");
			
			while (!cycleIsFinished) {
				ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
				list.addAll(scanResult.getResult());
				cursor = scanResult.getCursor();
				if (cursor.equals("0")) {
					cycleIsFinished = true;
				}
			}
			return list;
		} catch (Exception e) {
			System.err.println("scanKeys() key{"+key+"} 【"+e.getMessage()+"】");
			//e.printStackTrace();
			return list;
		}finally {
			close(jedis);
		}
	}

	public boolean setStringEx(String key, int seconds, String value) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.setex(key, seconds, value);
			return true;
		} catch (Exception e) {
			System.err.println("setStringEx() key{"+key+"} seconds{"+seconds+"} value{"+value+"} 【"+e.getMessage()+"】");
			//logger.debug("setStringEx() key {} throws:{}", key, e.getMessage());
			return false;
		} finally {
			close(jedis);
		}
	}

	public String getString(String key) {
		Jedis jedis = jedisPool.getJedis();
		try {
			String str = jedis.get(key);
			return str;
		} catch (Exception e) {
			System.err.println("getString() key{"+key+"} 【"+e.getMessage()+"】");
			//logger.debug("getString() key {} throws:{}", key, e.getMessage());
			return null;
		} finally {
			close(jedis);
		}
	}

	public boolean delString(String key) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.del(key);
			return true;
		} catch (Exception e) {
			System.err.println("delString() key{"+key+"} 【"+e.getMessage()+"】");
			//logger.debug("delString() key {} throws:{}", key, e.getMessage());
			return false;
		} finally {
			close(jedis);
		}
	}

	public boolean delHash(String key) {
        Jedis jedis = jedisPool.getJedis();
        try {
            jedis.del(key);
            return true;
        } catch (Exception e) {
        	System.err.println("delHash() key{"+key+"} 【"+e.getMessage()+"】");
        	//logger.debug("delHash() key {} throws:{}", key,e.getMessage());
            return false;
        } finally {
            close(jedis);
        }
    }
	
	public boolean delHash(String key, String mKey) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.hdel(key, mKey);
			return true;
		} catch (Exception e) {
			System.err.println("delHash() key{"+key+"} mKey{"+mKey+"} 【"+e.getMessage()+"】");
			//logger.debug("setHash() key {} throws:{}", key, e.getMessage());
			return false;
		} finally {
			close(jedis);
		}
	}

	public boolean setHash(String key, String mKey, String mVal) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.hset(key, mKey, mVal);
			return true;
		} catch (Exception e) {
        	System.err.println("setHash() key{"+key+"} mKey{"+mKey+"} mVal{"+mVal+"} 【"+e.getMessage()+"】");
			//logger.debug("setHash() key {} throws:{}", key, e.getMessage());
			return false;
		} finally {
			close(jedis);
		}
	}

	public String getHash(String key, String mKey) {
		Jedis jedis = jedisPool.getJedis();
		try {
			return jedis.hget(key, mKey);
		} catch (Exception e) {
        	System.err.println("getHash() key{"+key+"} mKey{"+mKey+"} 【"+e.getMessage()+"】");
			//logger.debug("setHash() key {} throws:{}", key, e.getMessage());
		} finally {
			close(jedis);
		}
		return null;
	}

	public boolean setHashMulti(String key, Map<String, String> map) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.hmset(key, map);
			return true;
		} catch (Exception e) {
        	System.err.println("setHashMulti() key{"+key+"} map{"+map+"} 【"+e.getMessage()+"】");
			//logger.debug("setHashMulti() key {} throws:{}", key, e.getMessage());
			return false;
		} finally {
			close(jedis);
		}
	}

	public List<String> getHashMulti(String key, String[] members) {
		Jedis jedis = jedisPool.getJedis();
		try {
			return jedis.hmget(key, members);
		} catch (Exception e) {
        	System.err.println("getHashMulti() key{"+key+"} members{"+members+"} 【"+e.getMessage()+"】");
			//logger.debug("getHashMulti() key {} throws:{}", key, e.getMessage());
		} finally {
			close(jedis);
		}
		return null;
	}

	public Map<String, String> getHashAll(String key) {
		Jedis jedis = jedisPool.getJedis();
		try {
			return jedis.hgetAll(key);
		} catch (Exception e) {
        	System.err.println("getHashAll() key{"+key+"} 【"+e.getMessage()+"】");
			//logger.debug("getHashAll() key {} throws:{}", key, e.getMessage());
		} finally {
			close(jedis);
		}
		return null;
	}

	public List<String> getHashValsAll(String key) {
		Jedis jedis = jedisPool.getJedis();
		try {
			return jedis.hvals(key);
		} catch (Exception e) {
        	System.err.println("getHashValsAll() key{"+key+"} 【"+e.getMessage()+"】");
			//logger.debug("getHashValsAll() key {} throws:{}", key, e.getMessage());
		} finally {
			close(jedis);
		}
		return null;
	}

	public Set<String> getHashKeysAll(String key) {
		Jedis jedis = jedisPool.getJedis();
		try {
			return jedis.hkeys(key);
		} catch (Exception e) {
        	System.err.println("getHashKeysAll() key{"+key+"} 【"+e.getMessage()+"】");
			//logger.debug("getHashValsAll() key {} throws:{}", key, e.getMessage());
		} finally {
			close(jedis);
		}
		return null;
	}

	public Set<String> getKeysAll(String key) {
		Jedis jedis = jedisPool.getJedis();
		try {
			return jedis.keys(key);
		} catch (Exception e) {
        	System.err.println("getKeysAll() key{"+key+"} 【"+e.getMessage()+"】");
			//logger.debug("getValsAll() key {} throws:{}", key, e.getMessage());
		} finally {
			close(jedis);
		}
		return null;
	}

	public boolean addScoreSet(String key, String mKey, int score) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.zadd(key, score, mKey);
			return true;
		} catch (Exception e) {
        	System.err.println("addScoreSet() key{"+key+"} mKey{"+mKey+"} score{"+score+"} 【"+e.getMessage()+"】");
			//logger.debug("addScoreSet() key {} throws:{}", key, e.getMessage());
			return false;
		} finally {
			close(jedis);
		}
	}

	public boolean delScoreSet(String key, String mKey) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.zrem(key, mKey);
			return true;
		} catch (Exception e) {
        	System.err.println("delScoreSet() key{"+key+"} mKey{"+mKey+"} 【"+e.getMessage()+"】");
			//logger.debug("delScoreSet() key {} throws:{}", key, e.getMessage());
			return false;
		} finally {
			close(jedis);
		}
	}

	public boolean changeScoreSet(String key, String mKey, int score) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.zincrby(key, score, mKey);
			return true;
		} catch (Exception e) {
        	System.err.println("changeScoreSet() key{"+key+"} mKey{"+mKey+"} score{"+score+"} 【"+e.getMessage()+"】");
			//logger.debug("changeScoreSet() key {} throws:{}", key, e.getMessage());
			return false;
		} finally {
			close(jedis);
		}
	}

	public Set<String> listScoreSetString(String key, int start, int end, boolean asc) {
		Jedis jedis = jedisPool.getJedis();
		try {
			if (asc) {
				return jedis.zrange(key, start, end);
			} else {
				return jedis.zrevrange(key, start, end);
			}
		} catch (Exception e) {
        	System.err.println("listScoreSetString() key{"+key+"} start{"+start+"} end{"+end+"} asc{"+asc+"} 【"+e.getMessage()+"】");
			//logger.debug("listScoreSetString() key {} throws:{}", key, e.getMessage());
		} finally {
			close(jedis);
		}
		return null;
	}

	public Set<Tuple> listScoreSetTuple(String key, int start, int end, boolean asc) {
		Jedis jedis = jedisPool.getJedis();
		try {
			if (asc) {
				return jedis.zrangeWithScores(key, start, end);
			} else {
				return jedis.zrevrangeWithScores(key, start, end);
			}
		} catch (Exception e) {
        	System.err.println("listScoreSetTuple() key{"+key+"} start{"+start+"} end{"+end+"} 【"+e.getMessage()+"】");
			//logger.debug("listScoreSetString() key {} throws:{}", key, e.getMessage());
		} finally {
			close(jedis);
		}
		return null;
	}

	// about redis list
	/**
	 * 獲取集合中指定索引元素
	 * @return  如果指定索引值不在列表的区间范围内，返回 nil 
	 * @param key
	 * @param index
	 * @return
	 */
	public Object getEleOfListByIndex(String key, int index) {
		Jedis jedis = jedisPool.getJedis();
		try {
			return jedis.lindex(key, index);
		} catch (Exception e) {
        	System.err.println("getEleOfListByIndex() key{"+key+"} index{"+index+"} 【"+e.getMessage()+"】");
			//logger.debug("getEleOfListByIndex() key {} index {} throws:{}", key, index, e.getMessage());
			return null;
		} finally {
			close(jedis);
		}
	}

	/**
	 * 將value 插入列表key當中，位於值pivot之前
	 * @param key
	 * @param pivot
	 * @param insertValue
	 * @return 成功返回列表長度；未找到返回-1；key不存在或為空列表返回0
	 */ 
	public boolean insertEleIntoList(String key, String pivot, String insertValue) {
		Jedis jedis = jedisPool.getJedis();
		boolean result = false;
		try {
			Long rs = jedis.linsert(key, ListPosition.BEFORE, pivot, insertValue);
			if (rs > 0) {
				result = true;
			}
			return result;
		} catch (Exception e) {
        	System.err.println("insertEleIntoList() key{"+key+"} pivot{"+pivot+"} insertValue{"+insertValue+"} 【"+e.getMessage()+"】");
			/*logger.debug("insertEleIntoList() key {} pivot {} insertValue {} throws:{}", key, pivot, insertValue,
					e.getMessage());*/
			return false;
		} finally {
			close(jedis);
		}
	}
	
	/**
	 * 替換集合中指定元素
	 * @param key
	 * @param oldValue
	 * @param newValue
	 * @remark 在被替換值之前插入替換元素 根據插入結果（>0表示插入成功，説明集合中仍存在被替換值） 進行一次移除操作 否則，集合已不存在需要替換元素
	 */
	public void replaceEleInList(String key, String oldValue,String newValue){
		Jedis jedis = jedisPool.getJedis();
		try {
    		while(true){
    			Long insertResult = jedis.linsert(key, ListPosition.BEFORE, oldValue, newValue);
    			if(insertResult<=0){
    				break;
    			}
    			jedis.lrem(key, 1, oldValue);
    		}
        } catch (Exception e) {
        	System.err.println("replaceEleInList() key{"+key+"} oldValue{"+oldValue+"} newValue{"+newValue+"} 【"+e.getMessage()+"】");
            //logger.debug("replaceEleInList() key {} oldValue {} newValue {} throws:{}", key,oldValue,newValue,e.getMessage());
        } finally {
            close(jedis);
        }
	}

	/**
	 * 通過索引設置元素值
	 * @param key
	 * @param index
	 * @param value
	 * @return 当索引参数超出范围，或对一个空列表进行 LSET 时，返回一个错误。
	 */
	public boolean setEleOfListByIndex(String key, int index, String value) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.lset(key, index, value);
			return true;
		} catch (Exception e) {
        	System.err.println("setEleOfListByIndex() key{"+key+"} index{"+index+"} value{"+value+"} 【"+e.getMessage()+"】");
			//logger.debug("setEleOfListByIndex() key {} index {} value {} throws:{}", key, index, value, e.getMessage());
			return false;
		} finally {
			close(jedis);
		}
	}

	/**
	 * 向列表中加入元素
	 * @param key
	 * @param value
	 * @return 如果列表不存在，一个空列表会被创建并执行 RPUSH 操作。 当列表存在但不是列表类型时，返回一个错误。
	 */
	public void addEleIntoList(String key, String value) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.rpush(key, value);
		} catch (Exception e) {
        	System.err.println("addEleIntoList() key{"+key+"} value{"+value+"} 【"+e.getMessage()+"】");
			//logger.debug("addEleIntoList() key {} value {} throws:{}", key, value, e.getMessage());
		} finally {
			close(jedis);
		}
	}

	/**
	 * 向列表中批量加入元素
	 * @param key
	 * @param values
	 * @return 如果列表不存在，一个空列表会被创建并执行 RPUSH 操作。 当列表存在但不是列表类型时，返回一个错误。
	 */
	public void addEleIntoList(String key, String[] values) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.rpush(key, values);
		} catch (Exception e) {
        	System.err.println("addEleIntoList() key{"+key+"} values{"+values+"} 【"+e.getMessage()+"】");
			//logger.debug("addEleIntoList() key {} values {} throws:{}", key, values, e.getMessage());
		} finally {
			close(jedis);
		}
	}
	
	/**
	 * 獲取指定範圍内元素
	 * @param key
	 * @param start 0表示第一個
	 * @param end -1表示最後一個
	 * @return 若key不存在或指定索引區間不存在 返回空列表
	 */
	public List<String> getRangeOfList(String key, int start, int end) {
		Jedis jedis = jedisPool.getJedis();
		List<String> list = new ArrayList<>();
		try {
			list = jedis.lrange(key, start, end);
		} catch (Exception e) {
        	System.err.println("getRangeOfList() key{"+key+"} start{"+start+"} end{"+end+"} 【"+e.getMessage()+"】");
			//logger.debug("getRangeOfList() key {} start {} end:{}", key, start, end, e.getMessage());
		} finally {
			close(jedis);
		}
		return list;
	}

	public static void main(String[] args) {
	    JedisPoolUtil jedisPool = new JedisPoolUtil();
		Jedis jedis = jedisPool.getJedis();
/*		int a = 0;
		for (int i = 0; i < 100; i++) {
			long t1 = System.currentTimeMillis();
			//List<String> list = jedis.lrange("lll", 0, -1);
			//String s = "{'command':'trail','content':{'color':0,'trail':[{'x':1.280263,'y':0.521127},{'x':1.236842,'y':0.542254},{'x':1.189474,'y':0.568662},{'x':1.148684,'y':0.586268},{'x':1.106579,'y':0.603873},{'x':1.052632,'y':0.619718},{'x':1.001316,'y':0.630282},{'x':0.960526,'y':0.639085},{'x':0.940789,'y':0.640845},{'x':0.935526,'y':0.642606},{'x':0.934211,'y':0.642606},{'x':0.944737,'y':0.635563},{'x':0.972368,'y':0.612676},{'x':1.006579,'y':0.588028},{'x':1.056579,'y':0.551056},{'x':1.107895,'y':0.514085},{'x':1.167105,'y':0.477113}],'type':'pencil','width':'0.0035','widthType':'1'},'domain':'draw','domain_id':2,'user_id':842}";
			StringBuilder str = new StringBuilder();
		for (int i = 0; i < 5000; i++) {
			str.append(s);
			jedis.lpush("ggg", s);
		}
			//jedis.lpush("lll", s);
			long t2 = System.currentTimeMillis();
			//System.out.println(str);
			System.out.println(t2-t1);
			System.out.println(list.size());
			a+=(t2-t1);
		}
		
		System.out.println((a/100+a%100));*/
		
        long start = System.currentTimeMillis();
		String s = "{'command':'trail','content':{'color':0,'trail':[{'x':1.280263,'y':0.521127},{'x':1.236842,'y':0.542254},{'x':1.189474,'y':0.568662},{'x':1.148684,'y':0.586268},{'x':1.106579,'y':0.603873},{'x':1.052632,'y':0.619718},{'x':1.001316,'y':0.630282},{'x':0.960526,'y':0.639085},{'x':0.940789,'y':0.640845},{'x':0.935526,'y':0.642606},{'x':0.934211,'y':0.642606},{'x':0.944737,'y':0.635563},{'x':0.972368,'y':0.612676},{'x':1.006579,'y':0.588028},{'x':1.056579,'y':0.551056},{'x':1.107895,'y':0.514085},{'x':1.167105,'y':0.477113}],'type':'pencil','width':'0.0035','widthType':'1'},'domain':'draw','domain_id':2,'user_id':842}";
		Pipeline pip = jedis.pipelined();
		for (int i = 0; i < 10000; i++) {
			String[] arr1= new String[50];
        	for (int j = 0; j < 50; j++) {
        		arr1[j]=s;	
        	}
			pip.lpush("mmm", s);
		}
		pip.sync();// 同步获取所有的回应

        System.out.println(System.currentTimeMillis() - start);
/*        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
        	for (int j = 0; j < 50; j++) {
        		//jedis.set(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        		jedis.lpush("nnn", s);
        	}
		}
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
        	String[] arr1= new String[50];
        	for (int j = 0; j < 50; j++) {
        		arr1[j]=s;	
        	}
        	jedis.lpush("ooo", arr1);
		}
        System.out.println(System.currentTimeMillis() - start);*/
    }
	
	/**
	 * 向set集合中添加元素
	 * @param key
	 * @param value
	 */
	public void addEleIntoSet(String key, String value) {
		Jedis jedis = jedisPool.getJedis();
		try {
			jedis.sadd(key, value);
		} catch (Exception e) {
        	System.err.println("addEleIntoSet() key{"+key+"} value{"+value+"} 【"+e.getMessage()+"】");
			//logger.debug("addEleIntoSet() key {} value {}", key, value, e.getMessage());
		} finally {
			close(jedis);
		}
	}

	/**
	 * 判断set中是否包含指定元素
	 * @param key
	 * @param value
	 * @return 元素存在返回1 元素不存在或key不存在返回0
	 */
	public boolean setContain(String key, String value) {
		Jedis jedis = jedisPool.getJedis();
		try {
			return jedis.sismember(key, value);
		} catch (Exception e) {
        	System.err.println("setContain() key{"+key+"} value{"+value+"} 【"+e.getMessage()+"】");
			//logger.debug("setContain() key {} value {}", key, value, e.getMessage());
			return false;
		} finally {
			close(jedis);
		}
	}

	/**從列表中移除count個指定元素
	 * @param key
	 * @param count
	 * @param value
	 * @return 被移除元素的数量。 列表不存在时返回 0 。
	 */
	public long removeEleFromList(String key, int count, String value) {
		Jedis jedis = jedisPool.getJedis();
		try {
			return jedis.lrem(key, count, value);
		} catch (Exception e) {
        	System.err.println("removeEleFromList() key{"+key+"} count{"+count+"} value{"+value+"} 【"+e.getMessage()+"】");
			//logger.debug("removeEleFromList() key {} count{} value {}", key, count, value, e.getMessage());
			return -1;
		} finally {
			close(jedis);
		}
	}

	/**
	 * 獲取列表長度
	 * @param key
	 * @return 返回列表長度，key不存在返回0
	 */
	public long getLengthOfList(String key) {
		Jedis jedis = jedisPool.getJedis();
		try {
			return jedis.llen(key);
		} catch (Exception e) {
        	System.err.println("getLengthOfList() key{"+key+"} 【"+e.getMessage()+"】");
			//logger.debug("getLengthOfList() key {} ", key, e.getMessage());
			return -1;
		} finally {
			close(jedis);
		}
	}

	public boolean getDistributedLock(String lockKey, String requestId, int expireTime) {
		Jedis jedis = jedisPool.getJedis();
		try {
			SetParams setParams = new SetParams();
			setParams.ex(expireTime);
			setParams.nx();
			String result = jedis.set(lockKey, requestId, setParams);
			if (DIST_LOCK_SUCCESS.equals(result)) {
				return true;
			}
			return false;
		} catch (Exception e) {
			// logger.debug("getDistributedLock key {} throws:{}", lockKey,
			// e.getMessage());
        	System.err.println("getDistributedLock() lockKey{"+lockKey+"} requestId{"+requestId+"} expireTime{"+expireTime+"} 【"+e.getMessage()+"】");
			//logger.debug("getDistributedLock throws {}", e);
		} finally {
			close(jedis);
		}
		return false;
	}

	public boolean releaseDistributedLock(String lockKey, String requestId) {
		Jedis jedis = jedisPool.getJedis();
		try {
			String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
			Object result = jedis.eval(script, Collections.singletonList(lockKey),
					Collections.singletonList(requestId));
			if (DIST_LOCK_RELEASE_SUCCESS.equals(result)) {
				return true;
			}
			return false;
		} catch (Exception e) {
        	System.err.println("releaseDistributedLock() lockKey{"+lockKey+"} requestId{"+requestId+"} 【"+e.getMessage()+"】");
			//logger.debug("releaseDistributedLock throws {}", e.getMessage());
		} finally {
			close(jedis);
		}
		return false;

	}

	public void close(Jedis jedis) {
		if (jedis != null) {
			jedis.close();
		}
	}
}