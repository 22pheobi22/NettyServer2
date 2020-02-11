package com.sa.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
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
            logger.debug("setString() key {} throws:{}", key, e.getMessage());
            return false;
        } finally {
            close(jedis);
        }
    }

    //模糊查询key
    public Set<String> scanKeys(String key){
    	Jedis jedis = jedisPool.getJedis();
    	Set<String> list = new HashSet<>();
    	String cursor = ScanParams.SCAN_POINTER_START;
        boolean cycleIsFinished = false;
        
		ScanParams scanParams = new ScanParams();
		scanParams.count(5000);
		scanParams.match(key + "*");
		while (!cycleIsFinished) {
			ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
			list.addAll(scanResult.getResult());
			cursor = scanResult.getCursor();
			if(cursor.equals("0")){
				cycleIsFinished = true;
			}
		}
    	return list;
    }
    
    public boolean setStringEx(String key, int seconds, String value) {
        Jedis jedis = jedisPool.getJedis();
        try {
            jedis.setex(key, seconds, value);
            return true;
        } catch (Exception e) {
            logger.debug("setStringEx() key {} throws:{}",key, e.getMessage());
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
            logger.debug("getString() key {} throws:{}", key,e.getMessage());
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
            logger.debug("delString() key {} throws:{}", key,e.getMessage());
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
            logger.debug("setHash() key {} throws:{}", key,e.getMessage());
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
            logger.debug("setHash() key {} throws:{}", key,e.getMessage());
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
            logger.debug("setHash() key {} throws:{}", key,e.getMessage());
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
            logger.debug("setMHash() key {} throws:{}", key,e.getMessage());
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
            logger.debug("getHashMulti() key {} throws:{}", key,e.getMessage());
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
            logger.debug("getHashAll() key {} throws:{}", key,e.getMessage());
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
            logger.debug("getHashValsAll() key {} throws:{}", key,e.getMessage());
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
            logger.debug("getHashValsAll() key {} throws:{}", key,e.getMessage());
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
            logger.debug("getValsAll() key {} throws:{}", key,e.getMessage());
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
            logger.debug("addScoreSet() key {} throws:{}", key,e.getMessage());
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
            logger.debug("delScoreSet() key {} throws:{}", key,e.getMessage());
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
            logger.debug("changeScoreSet() key {} throws:{}", key,e.getMessage());
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
            logger.debug("listScoreSetString() key {} throws:{}", key,e.getMessage());
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
            logger.debug("listScoreSetString() key {} throws:{}", key,e.getMessage());
        } finally {
            close(jedis);
        }
        return null;
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
//            logger.debug("getDistributedLock key {} throws:{}", lockKey, e.getMessage());
            logger.debug("getDistributedLock throws {}", e);
        } finally {
            close(jedis);
        }
        return false;
    }

    public boolean releaseDistributedLock(String lockKey, String requestId) {
        Jedis jedis = jedisPool.getJedis();
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
            if (DIST_LOCK_RELEASE_SUCCESS.equals(result)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.debug("releaseDistributedLock throws {}", e.getMessage());
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