package com.linkedinjms.chapter4.config;

import java.awt.List;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MarshallingMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.linkedinjms.chapter4.listener.BookOrderProcessingMessageListener;
import com.linkedinjms.chapter4.pojos.Book;
import com.linkedinjms.chapter4.pojos.BookOrder;
import com.linkedinjms.chapter4.pojos.Customer;

@EnableTransactionManagement
// Add annotation EnableJms, so the JmsListener will be picked up.
@EnableJms
//Add the annotation below so that Spring will know that this is part of configuration
@Configuration
public class JmsConfig implements JmsListenerConfigurer{
	private static final Logger LOGGER = LoggerFactory.getLogger(JmsConfig.class);
	
	//Using SingleConnectionFactory approach
	@Value("${spring.activemq.broker-url}")
	private String brokerUrl;
	
	@Value("${spring.activemq.user}")
	private String user;
	
	@Value("${spring.activemq.password}")
	private String password;
	
	/*
	 * Remember to use JmsSupportConverterOne
	 * @Bean to tell Spring to prepopulate these config
	 * When enabling xmlMarshallingMessageConverter @Bean and send using XML
	 * This following @Bean should be turned off, otherwise it will throw an error
	 * TURN ON/OFF, if you want to send using JSON
	*/
	@Bean
	public MessageConverter jacksonJmsMessageConverter() {
		//Use the jms support converter one
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		
		/*
		 * Below is doesn't matter whatever the type id as long as what you set should be set in 
		 * both converter.
		 * */
		converter.setTypeIdPropertyName("_type");
		return converter;
	}
	
	/*
	@Bean
	public ActiveMQConnectionFactory connectionFactory(){
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("admin","admin","tcp://localhost:61616");
		return factory;
	}
	*/

	/*
	// SingleConnectionFactory approach
	@Bean
	public SingleConnectionFactory connectionFactory(){
		
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(user, password, brokerUrl);
		SingleConnectionFactory singleConnectionFactory = new SingleConnectionFactory(factory); 
		singleConnectionFactory.setReconnectOnException(true);
		singleConnectionFactory.setClientId("myclientId");
		return singleConnectionFactory;
	}
	*/

	// CachingConnectionFactory approach
	@Bean
	public CachingConnectionFactory connectionFactory(){
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(user, password, brokerUrl);
		CachingConnectionFactory cacheConnectionFactory = new CachingConnectionFactory(factory);
		cacheConnectionFactory.setClientId("StoreFront");
		cacheConnectionFactory.setSessionCacheSize(100); //use anything more than 1
		return cacheConnectionFactory;
	}

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(){
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory());
		
		//Turn on/off if you want to use JSON file format
		factory.setMessageConverter(jacksonJmsMessageConverter());
		factory.setTransactionManager(jmsTransactionManager());
		factory.setErrorHandler(t -> {
			LOGGER.info("Handling error in listener for messages, error: " + t.getMessage());
		});
		
		return factory;
	}

	//********************************************************************************
	//Introduce our listener that we have created BookOrderProcessingMessageListener
	
	@Override
	public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
		SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setMessageListener(jmsMessageListener());
        endpoint.setDestination("book.order.processed.queue");
        endpoint.setId("book-order-processed-queue");
        endpoint.setConcurrency("1");
        endpoint.setSubscription("my-subscription");
        registrar.registerEndpoint(endpoint, jmsListenerContainerFactory());
        registrar.setContainerFactory(jmsListenerContainerFactory());

		/*
		 * By having the configuration above, when we send message
		 * it will process to the warehouse and to the book order process and to 
		 * book border.order.processed.queue and our customer will listen from that queue
		 * */
	}
	
	@Bean
	public BookOrderProcessingMessageListener jmsMessageListener() {
		return new BookOrderProcessingMessageListener();
	}
	//********************************************************************************

	@Bean
	public PlatformTransactionManager jmsTransactionManager() {
		return new JmsTransactionManager(connectionFactory());
	}
	
	@Bean
	public JmsTemplate jmsTemplate() {
		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
		
		// Ensure JmsTemplate is using the message converter
		jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
		
		// Ensure message is persistence
		jmsTemplate.setDeliveryPersistent(true);
		
		// Ensure the force of usage Transaction Management
		jmsTemplate.setSessionTransacted(true);
		
		return jmsTemplate;
	}
}
