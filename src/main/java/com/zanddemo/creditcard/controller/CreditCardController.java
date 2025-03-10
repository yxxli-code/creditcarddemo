package com.zanddemo.creditcard.controller;

import static com.zanddemo.creditcard.constants.CacheKey.CC_BANK_STAT_FILE;
import static com.zanddemo.creditcard.constants.CacheKey.CC_REQ_INFO;
import static com.zanddemo.creditcard.constants.CacheKey.CC_REQ_SUBMIT;

import com.zanddemo.creditcard.entity.CreditCardApplicationBasicInfo;
import com.zanddemo.creditcard.entity.ScoreBoard;
import com.zanddemo.creditcard.enums.ValidationResult;
import com.zanddemo.creditcard.exceptions.BusinessException;
import com.zanddemo.creditcard.service.CreditCardService;
import com.zanddemo.creditcard.service.LoginTokenService;
import com.zanddemo.creditcard.service.RedisLock;
import com.zanddemo.creditcard.valueobject.ApiResult;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * To provide a set of endpoints to maintain credit card applications, score boards etc.
 * */
@Slf4j
@RestController
public class CreditCardController {

	@Autowired
	private CreditCardService creditCardService;

	@Autowired
	private RedisLock redisLock;

	/**
	 * An endpoint to get the credit-card application information.
	 * It will first verify the authentication and get the application info based on current session's user info.
	 * */
	@GetMapping("/protected/credit-card/info")
	public ApiResult<CreditCardApplicationBasicInfo> getCreditCardApplication() {
		String emiratesID = LoginTokenService.getUserInfo().getEmiratesId();
		return new ApiResult().setSuccess(true).setData(creditCardService.findById(emiratesID));
	}

	/**
	 * An endpoint to save the credit-card application information.
	 * It will first verify the authentication and save the application info based on current session's user info.
	 * */
	@PostMapping("/protected/credit-card/request")
	public ApiResult saveCreditCardApplication(@RequestBody @Valid CreditCardApplicationBasicInfo creditCardApplicationBasicInfo) {
		//validate token/login
		LoginTokenService.validateLogin(creditCardApplicationBasicInfo.getEmiratesId());

		String key = CC_REQ_INFO + creditCardApplicationBasicInfo.getEmiratesId();
		String lockTime = "" + (System.currentTimeMillis() + 60000);
		//to avoid concurrent duplicate request from same user
		if(redisLock.lock(key, lockTime, 60000L)) {
			try {
				try {
					boolean result = creditCardService.save(creditCardApplicationBasicInfo);
					return new ApiResult().setSuccess(result);
				} catch (BusinessException e) {
					return new ApiResult().setSuccess(false).setCode(e.getCode()).setMessage(e.getMessage());
				}
			} finally {
				redisLock.unlock(key, lockTime);
			}
		}
		return new ApiResult().setSuccess(false).setCode(ValidationResult.REQUEST_IN_PROGRESS
			.getCode()).setMessage(ValidationResult.REQUEST_IN_PROGRESS.getDescription());
	}

	/**
	 * An endpoint to upload the bank-statement-file for the credit-card application.
	 * It will first verify the authentication and upload the file based on current session's user info.
	 * For decoupling services, we should save the file to private cloud file system like AWS S3 so that granted user/client can access it.
	 * It's not recommended to save it to database field like LONGLBLOB, resulting in that the database must be shared between different services.
	 * */
	@PostMapping("/protected/credit-card/bank-statement-file")
	public ApiResult uploadBankStatementFile(@RequestParam("file") MultipartFile uploadFile) {
		String emiratesId = LoginTokenService.getUserInfo().getEmiratesId();
		if(creditCardService.isApplicationEditable(emiratesId)) {
			//upload file
			String key = CC_BANK_STAT_FILE + emiratesId;
			String lockTime = "" + (System.currentTimeMillis() + 60000);
			//to avoid concurrent duplicate request from same user
			if (redisLock.lock(key, lockTime, 60000L)) {
				try {
					try {
						//TODO upload file
						//we need to check file name, file extension and ensure the file content is safe and valid to save.
						return new ApiResult().setSuccess(true);
					} catch (BusinessException e) {
						return new ApiResult().setSuccess(false).setCode(e.getCode()).setMessage(e.getMessage());
					}
				} finally {
					redisLock.unlock(key, lockTime);
				}
			}
		}

		return new ApiResult().setSuccess(false).setCode(ValidationResult.REQUEST_IN_PROGRESS.getCode()).setMessage(ValidationResult.REQUEST_IN_PROGRESS.getDescription());
	}

	/**
	 * An endpoint to submit the credit-card application to review.
	 * It will first verify the authentication and send the application info based on current session's user info to review.
	 * We separate the save and submit is to allow the user to edit the application several times until it's ready for review.
	 * */
	@PostMapping("/protected/credit-card/submit")
	public ApiResult submitCreditCardApplication() {
		String emiratesId = LoginTokenService.getUserInfo().getEmiratesId();

		String key = CC_REQ_SUBMIT + emiratesId;
		String lockTime = "" + (System.currentTimeMillis() + 60000);
		//to avoid concurrent duplicate request from same user
		if(redisLock.lock(key, lockTime, 60000L)) {
			try {
				try {
					//submit request
					boolean result = creditCardService.submitApplication(emiratesId);
					return new ApiResult().setSuccess(result);
				} catch (BusinessException e) {
					return new ApiResult().setSuccess(false).setCode(e.getCode()).setMessage(e.getMessage());
				}
			} finally {
				redisLock.unlock(key, lockTime);
			}
		}

		return null;
	}

	/**
	 * An endpoint to get the score board of the credit-card application.
	 * It will first verify the authentication and get the score board based on current session's user info.
	 * */
	@GetMapping("/protected/submission-score")
	public ApiResult<ScoreBoard> getCreditCardApplicationScore() {
		String emiratesID = LoginTokenService.getUserInfo().getEmiratesId();
		return new ApiResult().setSuccess(true).setData(creditCardService.findScoreById(emiratesID));
	}

}
