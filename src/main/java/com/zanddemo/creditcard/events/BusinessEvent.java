package com.zanddemo.creditcard.events;

import com.zanddemo.creditcard.enums.BusinessEventType;
import lombok.Data;

@Data
public class BusinessEvent<T> {
	private BusinessEventType type;
	private Long timestamp;
	private T payload;

	public BusinessEvent() {
		this.timestamp = System.currentTimeMillis();
	}

	public BusinessEvent(BusinessEventType type, T payload) {
		this.type = type;
		this.payload = payload;
		this.timestamp = System.currentTimeMillis();
	}
}
