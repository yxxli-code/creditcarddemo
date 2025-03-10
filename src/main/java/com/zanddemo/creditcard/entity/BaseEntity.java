package com.zanddemo.creditcard.entity;

import lombok.Data;

@Data
public class BaseEntity {
	private Long id;

	private String createdBy;
	private Long createdTime;

	private String updatedBy;
	private Long updatedTime;
}
