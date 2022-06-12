package com.linkedinjms.chapter4.service.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import com.linkedinjms.chapter4.pojos.BookOrder;

/*
 * Java Spring Annotation
 * 
 * @Component is an annotation that allows Spring to automatically detect our custom beans.
 * In other words, without having to write any explicit code, Spring will:
 * - Scan our application for classes annotated with @Component
 * - Instantiate them and inject any specified dependencies into them
 * - Inject them wherever needed
 * 
 * Spring @Service annotation is used with classes that provide some business functionalities.
 * We mark beans with @Service to indicate that they're holding the business logic.
 * */

@Service
public class WarehouseReceiverService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WarehouseReceiverService.class);
	
	@JmsListener(destination ="book.order.queue")
	public void receive(BookOrder bookOrder) {
		LOGGER.info("receive a message");
		LOGGER.info("Message is == " + bookOrder);
	}
}
