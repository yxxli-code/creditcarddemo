package com.zanddemo.creditcard.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

@Service
public class EventBus implements ApplicationEventPublisherAware {
	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void publish(BusinessEvent businessEvent) {
		this.applicationEventPublisher.publishEvent(businessEvent);
	}
}
