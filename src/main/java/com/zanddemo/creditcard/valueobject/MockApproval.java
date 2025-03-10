package com.zanddemo.creditcard.valueobject;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class MockApproval {
	private String emiratesId;
	private BigDecimal behaviorAnalysis;
	private Boolean compliance;
	private Boolean employment;
	private BigDecimal risk;
}
