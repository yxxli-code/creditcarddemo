package com.zanddemo.creditcard.entity;

import com.alibaba.fastjson.JSON;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ScoreBoard extends BaseEntity {
	private String emiratesId;

	private Boolean identityVerified;

	private Boolean employmentSubmitted;
	private Boolean employmentVerified;

	private Boolean complianceSubmitted;
	private Boolean complianceChecked;

	private Boolean riskSubmitted;
	private BigDecimal riskPercentage;

	private Boolean behaviorSubmitted;
	private BigDecimal behaviorPercentage;

	private BigDecimal totalScore;

	public ScoreBoard() {
		reset();
	}

	public void reset() {
		this.totalScore = null;
		this.employmentSubmitted = false;
		this.employmentVerified = null;
		this.complianceSubmitted = false;
		this.complianceChecked = null;
		this.riskSubmitted = false;
		this.riskPercentage = null;
		this.behaviorSubmitted = false;
		this.behaviorPercentage = null;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
