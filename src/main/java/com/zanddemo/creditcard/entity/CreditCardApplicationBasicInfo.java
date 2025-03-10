package com.zanddemo.creditcard.entity;

import com.alibaba.fastjson.JSON;
import com.zanddemo.creditcard.enums.CreditCardApplicationStatus;
import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreditCardApplicationBasicInfo extends BaseEntity {

	@NotBlank
	private String emiratesId;

	@NotBlank
	private String name;

	@NotBlank
	private String mobileNumber;

	@NotBlank
	private String nationality;

	@NotBlank
	private String address;

	@NotNull
	private BigDecimal annualIncome;

	@NotBlank
	private String employmentDetails;

	@NotNull
	private BigDecimal requestedCreditLimit;

	private String fileId;

	//derived fields:
	private String idempotentId;
	private CreditCardApplicationStatus status;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		CreditCardApplicationBasicInfo that = (CreditCardApplicationBasicInfo) o;
		return Objects.equals(emiratesId, that.emiratesId) &&
			Objects.equals(idempotentId, that.idempotentId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), emiratesId, idempotentId);
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
