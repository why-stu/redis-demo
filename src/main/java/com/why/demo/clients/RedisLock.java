package com.why.demo.clients;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

public class RedisLock implements Closeable {

    private final static Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private final static int MAX_LOCKED_SECONDS = 60;

    private RedisClient redis;
    private String lockKey;
    private long lockedTime;
    private boolean locked;

    public RedisLock(RedisClient redis, String lockKey) {
        this.redis = redis;
        this.lockKey = lockKey;
        this.lockedTime = 0;
        this.locked = false;
    }

    public boolean tryLock(long timeout, TimeUnit timeUnit) {
        try {
            long now = System.currentTimeMillis();
            long tryEndTime = now + (timeUnit != null ? timeUnit.toMillis(timeout) : 0);
            do {
                locked = setIfAbsent(lockKey, String.valueOf(now), MAX_LOCKED_SECONDS);
                if (locked) {
                    logger.info(String.format("Success to locked Redis on %s, value is %s", lockKey, now));
                    lockedTime = now;
                    break;
                }
                Thread.sleep(100);
                now = System.currentTimeMillis();
            } while (!locked && now < tryEndTime);

            if (!locked) {
                long lockVal = getLockValue();
                // 超过5倍的最大Locked时间，强制释放
                if (now > lockVal + MAX_LOCKED_SECONDS * 5 * 1000) {
                    logger.warn(String.format("Redis locked on %s for too long, force unlock it", lockKey));
                    // force to unlock it.
                    redis.delete(lockKey);
                    locked = setIfAbsent(lockKey, String.valueOf(now), MAX_LOCKED_SECONDS);
                    if (locked) {
                        logger.info(String.format("Success to locked Redis on %s, value is %s", lockKey, now));
                        lockedTime = now;
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Exception occurred to acquire redis lock on " + lockKey, t);
            locked = false;
        }

        return locked;
    }

    public void unlock() {
        if (!locked) {
            logger.info(String.format("Redis lock invalid on %s", lockKey));
            return;
        }
        long lockVal = getLockValue();
        if (lockVal == lockedTime) {
            logger.info(String.format("Success to unlock Redis on %s, value is %s, lockedTime is %s", lockKey, lockVal, lockedTime));
            redis.delete(lockKey);
            locked = false;
            lockedTime = 0;
        }
    }

    @Override
    public void close() {
        try {
            unlock();
        } catch (Throwable t) {
            // ignore
        }
    }

    private long getLockValue() {
        String lockedTimeStr =  redis.get(lockKey);
        if (StringUtils.isBlank(lockedTimeStr)) {
            return 0;
        }
        return Long.parseLong(lockedTimeStr);
    }

    public boolean setIfAbsent(String key, String value, int time) {
        return redis.setIfAbsent(key, value, time);
    }
}