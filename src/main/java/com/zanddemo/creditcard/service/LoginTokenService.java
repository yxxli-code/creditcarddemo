package com.zanddemo.creditcard.service;

import static com.zanddemo.creditcard.constants.CacheKey.CC_TOKEN_LOGIN_PREFIX;

import com.zanddemo.creditcard.enums.ValidationResult;
import com.zanddemo.creditcard.exceptions.BusinessException;
import com.zanddemo.creditcard.valueobject.UserBase;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
public class LoginTokenService {
	private static final String USERINFO_ATTRIBUTE = "userAttribute";
	public static final String X_CC_TOKEN = "X-CC-TOKEN";

	private static RedisTemplate redisTemplate;

	@Autowired
	public void setRedisTemplate(RedisTemplate redisTemplate) {
		LoginTokenService.redisTemplate = redisTemplate;
	}

	public static UserBase getUserInfo() {
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (sra == null) {
			log.error("server error, no request context");
			throw new BusinessException(ValidationResult.INVALID_SESSION.getCode());
		}
		UserBase userBase = (UserBase) sra.getRequest().getAttribute(USERINFO_ATTRIBUTE);
		if (userBase == null) {
			UserBase userInfo = getUserInfo(getHeaderToken());
			if(userInfo == null) {
				throw new BusinessException(ValidationResult.INVALID_SESSION.getCode(), ValidationResult.INVALID_SESSION.getDescription());
			}
			sra.getRequest().setAttribute(USERINFO_ATTRIBUTE, userInfo);
			return userInfo;
		}
		return userBase;
	}

	public static void validateLogin(String emiratesId) {
		UserBase userBase = getUserInfo();
		if(userBase ==null || !userBase.getEmiratesId().equals(emiratesId)) {
			throw new BusinessException(ValidationResult.INVALID_SESSION.getCode(), ValidationResult.INVALID_SESSION.getDescription());
		}
	}

	private static UserBase getUserInfo(String headerToken) {
		//for demo only, just get from redis cache.
		return (UserBase)redisTemplate.opsForValue().get(CC_TOKEN_LOGIN_PREFIX + headerToken);
	}

	public static String getHeaderToken() {
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		String authToken = sra.getRequest().getHeader(X_CC_TOKEN);
		if (authToken == null) {
			log.error("Bad request, x-cc-token not found");
			throw new BusinessException(ValidationResult.INVALID_SESSION.getCode(), ValidationResult.INVALID_SESSION.getDescription());
		}
		return authToken;
	}

	public static String generateLoginToken(UserBase userBase) {
		//for demo only, we should use other token algorithm like jwt instead.
		String token = UUID.randomUUID().toString();
		redisTemplate.opsForValue().set(CC_TOKEN_LOGIN_PREFIX + token, userBase);
		redisTemplate.expire(CC_TOKEN_LOGIN_PREFIX + token, 24, TimeUnit.HOURS);
		return token;
	}

	public static void removeToken(String token) {
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (sra != null) {
			UserBase userBase = (UserBase) sra.getRequest().getAttribute(USERINFO_ATTRIBUTE);
			if(userBase != null) {
				UserBase userBase2 = getUserInfo(token);
				if (userBase2.getEmiratesId().equals(userBase.getEmiratesId())) {
					redisTemplate.delete(CC_TOKEN_LOGIN_PREFIX + token);
				}
				sra.getRequest().removeAttribute(USERINFO_ATTRIBUTE);
			}
		}
	}
}
