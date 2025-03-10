package com.zanddemo.creditcard.enums;

public enum ValidationResult {
	SERVER_ERROR("40001", "Internal Server error"),
	INVALID_SESSION("400002", "No permission, please check if you have logged in"),
	REQUEST_IN_PROGRESS("50001", "There is an pending request in progress"),
	APP_NOT_SUBMITTED("50002", "The application is not submitted for review yet due to server limit, please re-submit it later"),
	APP_NOT_FOUND("50010", "No application record found"),
	SCORE_BOARD_NOT_FOUND("50020", "No score board record found");

	private String code;
	private String description;

	ValidationResult(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return this.code;
	}

	public String getDescription() {
		return this.description;
	}
}
