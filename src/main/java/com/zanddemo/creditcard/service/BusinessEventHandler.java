package com.zanddemo.creditcard.service;

import com.alibaba.fastjson.JSON;
import com.zanddemo.creditcard.events.BusinessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BusinessEventHandler {

	@EventListener(value = BusinessEvent.class)
	public void handle(BusinessEvent event) {
		log.info("Received business event {}", JSON.toJSONString(event));
	}
}
