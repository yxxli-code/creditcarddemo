package com.zanddemo.creditcard.controller;

import static com.zanddemo.creditcard.constants.MockCacheKey.BEHAVIOR_SB;
import static com.zanddemo.creditcard.constants.MockCacheKey.CC_DEMO_RPC_MOCK;
import static com.zanddemo.creditcard.constants.MockCacheKey.COMPLIANCE_SB;
import static com.zanddemo.creditcard.constants.MockCacheKey.EMPLOYMENT_SB;
import static com.zanddemo.creditcard.constants.MockCacheKey.IDENTITY_VERIFIED;
import static com.zanddemo.creditcard.constants.MockCacheKey.RISK_SB;

import com.zanddemo.creditcard.service.LoginTokenService;
import com.zanddemo.creditcard.valueobject.ApiResult;
import com.zanddemo.creditcard.valueobject.MockApproval;
import com.zanddemo.creditcard.valueobject.MockSubmission;
import com.zanddemo.creditcard.valueobject.UserBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * To provide a set of endpoints to assist on demonstrating the project.
 * */
@RestController
public class DemoAssistantController {
	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private WebhookController webhookController;

	/**
	 * POST /demo/login
	 *
	 * {
	 *     "emiratesId": "440101"
	 *     "name": "Brian"
	 * }
	 * */
	@PostMapping("/demo/login")
	public ApiResult<String> loginToDemoEnv(@RequestBody UserBase userInfo) {
		String token = LoginTokenService.generateLoginToken(userInfo);
		return new ApiResult().setSuccess(true).setData(token).setMessage("Usage: append X-CC-TOKEN in HTTP header with this token");
	}

	/**
	 * POST /demo/logout
	 * */
	@PostMapping("/demo/logout")
	public ApiResult logoutFromDemoEnv() {
		LoginTokenService.removeToken(LoginTokenService.getHeaderToken());
		return new ApiResult().setSuccess(true);
	}

	/**
	 * POST /demo/rpc/submission/mock
	 * {
	 *     "identity": true,
	 *     "behaviorAnalysis": true,
	 *     "compliance": true,
	 *     "employment": true,
	 *     "risk": true
	 * }
	 * */
	@PostMapping("/demo/rpc/submission/mock")
	public ApiResult updateRpcMockSubmissionSetting(@RequestBody MockSubmission mockSubmission) {

		redisTemplate.opsForHash().put(CC_DEMO_RPC_MOCK, IDENTITY_VERIFIED, mockSubmission.getIdentity());
		redisTemplate.opsForHash().put(CC_DEMO_RPC_MOCK, BEHAVIOR_SB, mockSubmission.getBehaviorAnalysis());
		redisTemplate.opsForHash().put(CC_DEMO_RPC_MOCK, COMPLIANCE_SB, mockSubmission.getCompliance());
		redisTemplate.opsForHash().put(CC_DEMO_RPC_MOCK, EMPLOYMENT_SB, mockSubmission.getEmployment());
		redisTemplate.opsForHash().put(CC_DEMO_RPC_MOCK, RISK_SB, mockSubmission.getRisk());

		return new ApiResult().setSuccess(true);
	}

	/**
	 * POST /demo/rpc/approval/mock
	 * {
	 *     "emiratesId": "440101",
	 *     "behaviorAnalysis": 0.4,
	 *     "compliance": true,
	 *     "employment": true,
	 *     "risk": 0.6
	 * }
	 * */
	@PostMapping("/demo/rpc/approval/mock")
	public ApiResult updateRpcMockApprovalSetting(@RequestBody MockApproval mockApproval) {

		ApiResult behaviorCheckResult = webhookController.updateScoreBoardWithBehaviorCheckResult("-1", mockApproval.getEmiratesId(), mockApproval.getBehaviorAnalysis());
		ApiResult complianceCheckResult = webhookController.updateScoreBoardWithComplianceCheckResult("-1", mockApproval.getEmiratesId(), mockApproval.getCompliance());
		ApiResult riskCheckResult = webhookController.updateScoreBoardWithRiskCheckResult("-1", mockApproval.getEmiratesId(), mockApproval.getRisk());
		ApiResult employmentCheckResult = webhookController.updateScoreBoardWthEmploymentCheckResult("-1", mockApproval.getEmiratesId(), mockApproval.getEmployment());

		return new ApiResult().setSuccess(behaviorCheckResult.isSuccess()
			&& complianceCheckResult.isSuccess()
			&& riskCheckResult.isSuccess()
			&& employmentCheckResult.isSuccess());
	}
}
