package com.zanddemo.creditcard.service;

import com.alibaba.fastjson.JSON;
import com.zanddemo.creditcard.constants.OutcomeThreshold;
import com.zanddemo.creditcard.entity.CreditCardApplicationBasicInfo;
import com.zanddemo.creditcard.entity.ScoreBoard;
import com.zanddemo.creditcard.enums.BusinessEventType;
import com.zanddemo.creditcard.enums.CreditCardApplicationStatus;
import com.zanddemo.creditcard.enums.ValidationResult;
import com.zanddemo.creditcard.events.BusinessEvent;
import com.zanddemo.creditcard.events.EventBus;
import com.zanddemo.creditcard.exceptions.BusinessException;
import com.zanddemo.creditcard.repository.CreditCardRepository;
import com.zanddemo.creditcard.repository.ScoreBoardRepository;
import com.zanddemo.creditcard.rpc.BehaviorAnalysisService;
import com.zanddemo.creditcard.rpc.ComplianceService;
import com.zanddemo.creditcard.rpc.EmploymentService;
import com.zanddemo.creditcard.rpc.IdentityService;
import com.zanddemo.creditcard.rpc.RiskService;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 *
 * Domain Service for maintaining credit card applications, score boards.
 * */
@Slf4j
@Service
public class CreditCardService {
	@Autowired
	private EventBus eventBus;

	@Autowired
	private CreditCardRepository creditCardRepository;

	@Autowired
	private ScoreBoardRepository scoreBoardRepository;

	@Autowired
	private BehaviorAnalysisService analysisService;

	@Autowired
	private ComplianceService complianceService;

	@Autowired
	private EmploymentService employmentService;

	@Autowired
	private IdentityService identityService;

	@Autowired
	private RiskService riskService;

	/**
	 * To get the application request information.
	 *
	 * @param emiratesID
	 * @return the application request info
	 * */
	public CreditCardApplicationBasicInfo findById(String emiratesID) throws BusinessException {
		return creditCardRepository.findById(emiratesID);
	}

	/**
	 * To save the application request.
	 * If the request is not editable, reject the action.
	 * If the request is new, add new records.
	 * If the request is existing, update existing records.
	 *
	 * @param creditCardApplicationBasicInfo
	 * @return true if the save action is successful, otherwise throw exceptions.
	 * */
	@Transactional(rollbackFor = Exception.class)
	public boolean save(CreditCardApplicationBasicInfo creditCardApplicationBasicInfo) throws BusinessException {
		CreditCardApplicationBasicInfo applicationBasicInfo = this.findById(creditCardApplicationBasicInfo.getEmiratesId());
		//one user can only have one application in progress:
		if(applicationBasicInfo != null) {
			if (!applicationBasicInfo.getStatus().equals(CreditCardApplicationStatus.DRAFT)
				&& !applicationBasicInfo.getStatus().equals(CreditCardApplicationStatus.REJECTED)
			) {
				throw new BusinessException(ValidationResult.REQUEST_IN_PROGRESS.getCode(), ValidationResult.REQUEST_IN_PROGRESS.getDescription());
			}
			//update existing application record
			log.info("updating existing application request {}", JSON.toJSONString(creditCardApplicationBasicInfo));
			creditCardApplicationBasicInfo.setStatus(CreditCardApplicationStatus.DRAFT);
			creditCardApplicationBasicInfo.setIdempotentId(UUID.randomUUID().toString()); //re-new idempotentId
			creditCardApplicationBasicInfo.setUpdatedBy(creditCardApplicationBasicInfo.getEmiratesId());
			creditCardApplicationBasicInfo.setUpdatedTime(System.currentTimeMillis());
			creditCardRepository.update(creditCardApplicationBasicInfo);
			//reset score board
			resetScoreBoard(creditCardApplicationBasicInfo);
		} else {
			//create new application record
			log.info("creating new application request {}", JSON.toJSONString(creditCardApplicationBasicInfo));
			creditCardApplicationBasicInfo.setIdempotentId(UUID.randomUUID().toString()); //re-new idempotentId
			creditCardApplicationBasicInfo.setStatus(CreditCardApplicationStatus.DRAFT);
			creditCardApplicationBasicInfo.setCreatedBy(creditCardApplicationBasicInfo.getEmiratesId());
			creditCardApplicationBasicInfo.setCreatedTime(System.currentTimeMillis());
			creditCardRepository.add(creditCardApplicationBasicInfo);

			ScoreBoard scoreBoard = new ScoreBoard();
			scoreBoard.setEmiratesId(creditCardApplicationBasicInfo.getEmiratesId());
			scoreBoard.setCreatedBy(creditCardApplicationBasicInfo.getEmiratesId());
			scoreBoard.setCreatedTime(System.currentTimeMillis());
			scoreBoardRepository.add(scoreBoard);
		}
		return true;
	}

	/**
	 * To reset score board so that it can proceed new application request.
	 *
	 * @param creditCardApplicationBasicInfo
	 * */
	public void resetScoreBoard(CreditCardApplicationBasicInfo creditCardApplicationBasicInfo) {
		log.info("resetting score board {}", JSON.toJSONString(creditCardApplicationBasicInfo));
		ScoreBoard scoreBoard = scoreBoardRepository.findById(creditCardApplicationBasicInfo.getEmiratesId());
		scoreBoard.reset();
		scoreBoard.setUpdatedBy(creditCardApplicationBasicInfo.getEmiratesId());
		scoreBoardRepository.reset(scoreBoard);
	}

	/**
	 * To check if the application request is allowed to edit by user.
	 *
	 * @param emiratesId
	 * @return true if it's editable, otherwise false.
	 * */
	public boolean isApplicationEditable(String emiratesId) {
		CreditCardApplicationBasicInfo applicationBasicInfo = this.findById(emiratesId);
		//one user can only have one application in progress:
		if(applicationBasicInfo != null) {
			if (!applicationBasicInfo.getStatus().equals(CreditCardApplicationStatus.DRAFT)
				&& !applicationBasicInfo.getStatus().equals(CreditCardApplicationStatus.REJECTED)
			) {
				return false;
			}
		}
		return true;
	}

	/**
	 * To query the score board details.
	 *
	 * @param emiratesId
	 * @return the score board
	 * */
	public ScoreBoard findScoreById(String emiratesId) {
		return scoreBoardRepository.findById(emiratesId);
	}

	/**
	 * To submit the user's application request to review.
	 * If any of down-streaming evaluation services is unavailable or un-responsive, just return to the client and let the user to try to submit the request later.
	 * - accept the submission only when the application request is in status: DRAFT or last REJECTED.
	 * - reject the submission if the application request is in reviewing status: SUBMITTED or finalized status: STP, NEAR_STP, MANUAL_REVIEW
	 *
	 * @param emiratesId
	 * @return true if the submission is accepted, otherwise throw exceptions.
	 * */
	@Transactional(rollbackFor = Exception.class)
	public boolean submitApplication(String emiratesId) throws BusinessException {
		CreditCardApplicationBasicInfo applicationBasicInfo = this.findById(emiratesId);
		//one user can only have one application in progress:
		if(applicationBasicInfo != null) {
			if (!applicationBasicInfo.getStatus().equals(CreditCardApplicationStatus.DRAFT)
				&& !applicationBasicInfo.getStatus().equals(CreditCardApplicationStatus.REJECTED)
			) {
				throw new BusinessException(ValidationResult.REQUEST_IN_PROGRESS.getCode(), ValidationResult.REQUEST_IN_PROGRESS.getDescription());
			}
			resetScoreBoard(applicationBasicInfo);
			//validate identity
			boolean result = identityService.validate(applicationBasicInfo);
			if(result) {
				log.info("continue to check other evaluations {}", emiratesId);
				ScoreBoard scoreBoard = scoreBoardRepository.findById(emiratesId);
				if(scoreBoard == null) {
					throw new BusinessException(ValidationResult.SCORE_BOARD_NOT_FOUND.getCode(), ValidationResult.SCORE_BOARD_NOT_FOUND.getDescription());
				}
				String fileId = creditCardRepository.findFileId(emiratesId);
				scoreBoard.setIdentityVerified(true);
				//if any network error or service unavailable, submitRequest will throw exception and rollback the submission, and the user should try to submit it again.
				//however, we need to pass idempotentId to other evaluation services since some request may be duplicatedly submitted since they may be already submitted
				//to the evaluation service but miss the response due to broken network.
				scoreBoard.setBehaviorSubmitted(analysisService.submitRequest(emiratesId, applicationBasicInfo.getIdempotentId(), fileId));
				scoreBoard.setComplianceSubmitted(complianceService.submitRequest(applicationBasicInfo));
				scoreBoard.setRiskSubmitted(riskService.submitRequest(applicationBasicInfo));
				scoreBoard.setEmploymentSubmitted(employmentService.submitRequest(applicationBasicInfo));

				scoreBoard.setUpdatedBy(emiratesId);
				scoreBoard.setUpdatedTime(System.currentTimeMillis());
				scoreBoardRepository.update(scoreBoard);

				applicationBasicInfo.setStatus(CreditCardApplicationStatus.SUBMITTED);
				applicationBasicInfo.setUpdatedBy(emiratesId);
				applicationBasicInfo.setUpdatedTime(System.currentTimeMillis());
				creditCardRepository.update(applicationBasicInfo);
				log.info("requested to check other evaluations {}", emiratesId);
			} else {
				log.info("rejecting the submission {}", emiratesId);
				ScoreBoard scoreBoard = scoreBoardRepository.findById(emiratesId);
				if(scoreBoard == null) {
					throw new BusinessException(ValidationResult.SCORE_BOARD_NOT_FOUND.getCode(), ValidationResult.SCORE_BOARD_NOT_FOUND.getDescription());
				}
				scoreBoard.setIdentityVerified(false);
				scoreBoard.setTotalScore(BigDecimal.ZERO);
				scoreBoard.setUpdatedBy(emiratesId);
				scoreBoard.setUpdatedTime(System.currentTimeMillis());
				scoreBoardRepository.update(scoreBoard);

				applicationBasicInfo.setStatus(CreditCardApplicationStatus.REJECTED);
				applicationBasicInfo.setUpdatedBy(emiratesId);
				applicationBasicInfo.setUpdatedTime(System.currentTimeMillis());
				creditCardRepository.update(applicationBasicInfo);
				eventBus.publish(new BusinessEvent(BusinessEventType.REJECTED, applicationBasicInfo));
			}
			return true;

		}
		throw new BusinessException(ValidationResult.APP_NOT_FOUND.getCode(), ValidationResult.APP_NOT_FOUND.getDescription());
	}

	/**
	 * To update scoreboard based on scoring outcomes of other services.
	 * This is triggered when the Remote service callbacks.
	 * It's event/message based as we don't want to query status in a scheduled job.
	 *
	 * @param idempotentId
	 * @param emiratesId
	 * @param behaviorPercentage
	 * @param riskPercentage
	 * @param compliancePassed
	 * @param employmentPassed
	 * @return true if the update is accepted, otherwise throw exceptions.
	 * */
	@Transactional(rollbackFor = Exception.class)
	public boolean updateScoreBoard(String idempotentId, String emiratesId, BigDecimal behaviorPercentage, BigDecimal riskPercentage, Boolean compliancePassed, Boolean employmentPassed) {
		ScoreBoard scoreBoard = scoreBoardRepository.findById(emiratesId);
		if(scoreBoard == null) {
			//something wrong, no record found to update.
			log.warn("No score board found {}", emiratesId);
			return true; // return true to avoid retry
		}

		CreditCardApplicationBasicInfo applicationBasicInfo = creditCardRepository.findById(emiratesId);
		if(applicationBasicInfo == null) {
			//something wrong, no record found to update.
			log.warn("No application request found {}", emiratesId);
			return true; // return true to avoid retry
		}

		if(!applicationBasicInfo.getStatus().equals(CreditCardApplicationStatus.SUBMITTED)) {
			log.warn("Invalid update {} since the application is not submitted yet so ignoring this update {}", emiratesId, idempotentId);
			return true; // return true to avoid retry
		}

		if(scoreBoard.getTotalScore() != null) {
			//we already have total score, just ignore the setting.
			log.warn("The total score of {} has been calculated earlier so ignoring this update {}", emiratesId, idempotentId);
			return true; // return true to avoid retry
		}

		//only update the field when it's not updated yet.
		if(riskPercentage != null) {
			scoreBoard.setRiskPercentage(riskPercentage);
			if(scoreBoard.getRiskSubmitted() == null || !scoreBoard.getRiskSubmitted()) {
				scoreBoard.setRiskPercentage(null);
			}
		}

		if(behaviorPercentage != null) {
			scoreBoard.setBehaviorPercentage(behaviorPercentage);
			if(scoreBoard.getBehaviorSubmitted() == null || !scoreBoard.getBehaviorSubmitted()) {
				scoreBoard.setBehaviorPercentage(null);
			}
		}

		if(compliancePassed != null) {
			scoreBoard.setComplianceChecked(compliancePassed);
			if(scoreBoard.getComplianceSubmitted() == null || !scoreBoard.getComplianceSubmitted()) {
				scoreBoard.setComplianceChecked(null);
			}
		}

		if(employmentPassed != null) {
			scoreBoard.setEmploymentVerified(employmentPassed);
			if(scoreBoard.getEmploymentSubmitted() == null || !scoreBoard.getEmploymentSubmitted()) {
				scoreBoard.setEmploymentVerified(null);
			}
		}
		scoreBoard.setUpdatedBy("-1");

		//trigger total score calculation when all criteria result are back.
		if(scoreBoard.getIdentityVerified() != null
			&& scoreBoard.getRiskPercentage() != null
		    && scoreBoard.getBehaviorPercentage() != null
		    && scoreBoard.getComplianceChecked() != null
		    && scoreBoard.getEmploymentVerified() != null) {

			log.info("calculating total score now {}", JSON.toJSONString(scoreBoard));
			if(scoreBoard.getIdentityVerified().booleanValue()) {
				BigDecimal riskWeighted = scoreBoard.getRiskPercentage().multiply(new BigDecimal("0.2"));
				BigDecimal behaviorWeighted = scoreBoard.getBehaviorPercentage().multiply(new BigDecimal("0.2"));
				BigDecimal complianceWeighed = scoreBoard.getComplianceChecked().booleanValue() ? new BigDecimal("0.2") : BigDecimal.ZERO;
				BigDecimal identityWeighted = scoreBoard.getIdentityVerified().booleanValue() ? new BigDecimal("0.2") : BigDecimal.ZERO;
				BigDecimal employmentWeighted = scoreBoard.getEmploymentVerified().booleanValue() ? new BigDecimal("0.2") : BigDecimal.ZERO;
				BigDecimal totalScore = riskWeighted.add(behaviorWeighted).add(complianceWeighed).add(identityWeighted).add(employmentWeighted);
				scoreBoard.setTotalScore(totalScore);
			} else {
				scoreBoard.setTotalScore(BigDecimal.ZERO);
			}
			scoreBoardRepository.update(scoreBoard);
			eventBus.publish(decideOnScoreThreshold(emiratesId, scoreBoard.getTotalScore()));

		} else {
			//partially status update
			log.info("partially updating score board {}", JSON.toJSONString(scoreBoard));
			scoreBoardRepository.update(scoreBoard);
		}

		return true;
	}

	/**
	 * To derive the outcome based on total score and publish notification events.
	 *
	 * @param emiratesId
	 * @param totalScore
	 * @return the notification event
	 * */
	private BusinessEvent decideOnScoreThreshold(String emiratesId, BigDecimal totalScore) {
		BusinessEvent event;
		CreditCardApplicationBasicInfo creditCardApplicationBasicInfo = creditCardRepository.findById(emiratesId);
		if(totalScore.compareTo(OutcomeThreshold.P90) >= 0) {
			creditCardApplicationBasicInfo.setStatus(CreditCardApplicationStatus.STP);
			event = new BusinessEvent(BusinessEventType.STP, creditCardApplicationBasicInfo);
		} else if(totalScore.compareTo(OutcomeThreshold.P75) >=0) {
			creditCardApplicationBasicInfo.setStatus(CreditCardApplicationStatus.NEAR_STP);
			event = new BusinessEvent(BusinessEventType.NEAR_STP, creditCardApplicationBasicInfo);
		} else if(totalScore.compareTo(OutcomeThreshold.P50) >=0) {
			creditCardApplicationBasicInfo.setStatus(CreditCardApplicationStatus.MANUAL_REVIEW);
			event = new BusinessEvent(BusinessEventType.MANUAL_REVIEW, creditCardApplicationBasicInfo);
		} else {
			creditCardApplicationBasicInfo.setStatus(CreditCardApplicationStatus.REJECTED);
			event = new BusinessEvent(BusinessEventType.REJECTED, creditCardApplicationBasicInfo);
		}
		creditCardApplicationBasicInfo.setUpdatedBy("-1");
		creditCardRepository.update(creditCardApplicationBasicInfo);
		//the reason why we return event after the update is because the update may fail, we don't publish message if update fails.
		return event;
	}

}
