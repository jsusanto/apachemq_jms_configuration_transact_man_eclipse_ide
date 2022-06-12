package com.linkedinjms.chapter4.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

// We add this annotation Component because we want Spring to pick it up when compiling
@Component
public class BookOrderProcessingMessageListener implements MessageListener{

	private static final Logger LOGGER = LoggerFactory.getLogger(BookOrderProcessingMessageListener.class);
	
	@Override
	public void onMessage(Message message) {
		// This is the jms message and not just a plain message
		try {
			String text = ((TextMessage)message).getText();
			LOGGER.info("BookOrderProcessingMessageListener: " + text);
			System.out.println("Start = " + text);
            Thread.sleep(5000);
            System.out.println("End = " + text);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
