package com.zanddemo.creditcard.repository;

import com.zanddemo.creditcard.entity.CreditCardApplicationBasicInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardRepository {

	CreditCardApplicationBasicInfo findById(@Param("emiratesId") String emiratesId);

	void update(CreditCardApplicationBasicInfo creditCardApplicationBasicInfo);

	void add(CreditCardApplicationBasicInfo creditCardApplicationBasicInfo);

	String findFileId(@Param("emiratesId") String emiratesId);
}
