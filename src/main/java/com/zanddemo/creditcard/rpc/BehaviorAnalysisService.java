package com.zanddemo.creditcard.rpc;

import static com.zanddemo.creditcard.constants.MockCacheKey.BEHAVIOR_SB;
import static com.zanddemo.creditcard.constants.MockCacheKey.CC_DEMO_RPC_MOCK;

import com.zanddemo.creditcard.enums.ValidationResult;
import com.zanddemo.creditcard.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BehaviorAnalysisService {
	@Autowired
	private RedisTemplate redisTemplate;

	public boolean submitRequest(String emiratesId, String idempotentId, String fileId) {
		//assumption: the file specified by fileId is stored in a private file system that both credit card service and behavior analysis service
		//can access. Usually, it's stored in AWS S3 private bucket(cheaper storage solution than others).

		//remotely call the endpoint of behavior analysis service
		//if the call failed due to network unresponsive, downgrade and return false.
		//if the call success, check the result and confirm it's submitted.
		//if it's submitted, return and let the caller to update ScoreBoard to submitted
		log.info("Submitting for Behavior Analysis {} {} {}", emiratesId, idempotentId, fileId);
		Boolean result = (Boolean)redisTemplate.opsForHash().get(CC_DEMO_RPC_MOCK, BEHAVIOR_SB);
		if(result == null || !result) {
			throw new BusinessException(ValidationResult.APP_NOT_SUBMITTED.getCode(), ValidationResult.APP_NOT_SUBMITTED.getDescription());
		}
		return result;
	}

}
