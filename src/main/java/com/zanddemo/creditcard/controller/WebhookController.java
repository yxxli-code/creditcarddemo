package com.zanddemo.creditcard.controller;

import com.zanddemo.creditcard.service.CreditCardService;
import com.zanddemo.creditcard.valueobject.ApiResult;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * To provide a set of endpoints working as web-hooks so that the 3rd-party services can callback on-demand.
 *
 * Web-hook endpoints are designed for the following case:
 * Service A submits a request to Service B.
 * Service B needs more time to handle asynchronously.
 * Once Service B completes the process, it calls back the web-hook to notify Service A.
 *
 * Web-hook endpoints can be used for internet/intranet communication while messaging queue is used for intranet.
 * Web-hook endpoints can be protected with token or apiKey authentication in API gateway.
 *
 * The caller (Service B) need to do retry if the invocation fails and the callee (Service A) need to do the idempotent check.
 * */
@RestController
@RequestMapping("/webhook")
public class WebhookController {
	@Autowired
	private CreditCardService creditCardService;

	/**
	 * An callback endpoint for Employment Verification Service. So that it can be called in-time when Employment Verification is done.
	 * Also the Employment Verification Service should retry the call if it cannot get the result with success=true.
	 * This service will do idempotent check and it's safe to be called repeatedly.
	 * */
	@PostMapping("/employment-check/result")
	public ApiResult updateScoreBoardWthEmploymentCheckResult(@RequestParam("idempotentId") String idempotentId,
		@RequestParam("emiratesId") String emiratesId, @RequestParam("passed") Boolean passed) {
		boolean result = creditCardService.updateScoreBoard(idempotentId, emiratesId, null, null, null, passed);
		return new ApiResult().setSuccess(result);
	}

	/**
	 * An callback endpoint for Compliance Check Service. So that it can be called in-time when Compliance Check is done.
	 * Also the Compliance Check Service should retry the call if it cannot get the result with success=true.
	 * This service will do idempotent check and it's safe to be called repeatedly.
	 * */
	@PostMapping("/compliance-check/result")
	public ApiResult updateScoreBoardWithComplianceCheckResult(@RequestParam("idempotentId") String idempotentId,
		@RequestParam("emiratesId") String emiratesId, @RequestParam("passed") Boolean passed) {
		boolean result = creditCardService.updateScoreBoard(idempotentId, emiratesId, null, null, passed, null);
		return new ApiResult().setSuccess(result);
	}

	/**
	 * An callback endpoint for behavior analysis Service. So that it can be called in-time when behavior analysis is done.
	 * Also the behavior analysis Service should retry the call if it cannot get the result with success=true.
	 * This service will do idempotent check and it's safe to be called repeatedly.
	 * */
	@PostMapping("/behavior-analysis/result")
	public ApiResult updateScoreBoardWithBehaviorCheckResult(@RequestParam("idempotentId") String idempotentId,
		@RequestParam("emiratesId") String emiratesId, @RequestParam("behaviorPercentage") BigDecimal behaviorPercentage) {
		boolean result = creditCardService.updateScoreBoard(idempotentId, emiratesId, behaviorPercentage, null, null, null);
		return new ApiResult().setSuccess(result);
	}

	/**
	 * An callback endpoint for risk evaluation Service. So that it can be called in-time when risk evaluation is done.
	 * Also the risk evaluation Service should retry the call if it cannot get the result with success=true.
	 * This service will do idempotent check and it's safe to be called repeatedly.
	 * */
	@PostMapping("/risk-evaluation/result")
	public ApiResult updateScoreBoardWithRiskCheckResult(@RequestParam("idempotentId") String idempotentId,
		@RequestParam("emiratesId") String emiratesId, @RequestParam("riskPercentage") BigDecimal riskPercentage) {
		boolean result = creditCardService.updateScoreBoard(idempotentId, emiratesId, null, riskPercentage, null, null);
		return new ApiResult().setSuccess(result);
	}
}
