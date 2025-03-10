package com.zanddemo.creditcard.valueobject;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ApiResult<T> {

	private String event;
	private boolean success;
	private String message;
	private T Data;
	private String code;

	public ApiResult(String message) {
		this.message = message;
	}

	public ApiResult(String code, String message) {
		this.message = message;
		this.code = code;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
