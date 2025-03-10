package com.zanddemo.creditcard.service;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * A redis lock for non-production demo.
 * */
@Component
@Slf4j
public class RedisLock {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public boolean lock(String key, String value, Long expirationTimeMs) {
        boolean lock = this._lock(key, value);
        if(lock){
            stringRedisTemplate.expire(key,expirationTimeMs, TimeUnit.MILLISECONDS);
        }
        return lock;
    }

    private boolean _lock(String key, String value) {

        if (stringRedisTemplate.opsForValue().setIfAbsent(key, value)) {
            return true;
        }

        String currentValue = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue) < System.currentTimeMillis()) {
            String oldValue = stringRedisTemplate.opsForValue().getAndSet(key, value);
            if (!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)) {
                return true;
            }
        }
        return false;
    }


    public void unlock(String key, String value) {
        try {
            String currentValue = stringRedisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
                stringRedisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            log.error("[Redis lock] failure，{}", e);
        }
    }

}