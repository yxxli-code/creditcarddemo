package com.zanddemo.creditcard.rpc;

import static com.zanddemo.creditcard.constants.MockCacheKey.CC_DEMO_RPC_MOCK;
import static com.zanddemo.creditcard.constants.MockCacheKey.IDENTITY_VERIFIED;

import com.zanddemo.creditcard.entity.CreditCardApplicationBasicInfo;
import com.zanddemo.creditcard.enums.ValidationResult;
import com.zanddemo.creditcard.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IdentityService {
	@Autowired
	private RedisTemplate redisTemplate;

	public boolean validate(CreditCardApplicationBasicInfo creditCardApplicationBasicInfo) {
		//remotely call the endpoint to check.
		//if the call failed due to network unresponsive, downgrade and return false.
		//if the call success, check the result and confirm it's submitted.
		//if it's submitted, return and let the caller to update ScoreBoard to submitted
		log.info("Submitting for Identity Verification {} {}", creditCardApplicationBasicInfo.getEmiratesId(), creditCardApplicationBasicInfo.getIdempotentId());
		Boolean result = (Boolean)redisTemplate.opsForHash().get(CC_DEMO_RPC_MOCK, IDENTITY_VERIFIED);
		if(result == null || !result) {
			throw new BusinessException(ValidationResult.APP_NOT_SUBMITTED.getCode(), ValidationResult.APP_NOT_SUBMITTED.getDescription());
		}
		return result;
	}

}
