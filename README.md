# apachemq_jms_eclipseproject
Tutorial ApacheMQ + JMS using Eclipse IDE

How to run this project?
Step 1. Add new java console project

Step 2. Import using existing Maven project

Step 3. Run Application.java

Pre-requisites
==============
Install ApacheMQ
When you have installed this, go to your browser and open http://localhost:8161/admin
default username/password admin/admin

Run Application.java
Open http://localhost:9000/ to populate data from servlet and it will hit to the backend (send and receive service).

com.linkedinjms.chapter4
========================
It's a Send / Receive service using ApacheMQ and a continuation from chapter 3.

The format is using JSON.
We're about to use XML as well however, the latest library XStreamMarshaller is not compatible.
As result, we couldn't test using XML serialization.

Additional Service is WarehouseProcessingService.

The workflow is as below:
From the web interface http://localhost:9000/ add data and data will be queued in book.order.queue.
WarehouseReceiver.java listens messages coming from book.order.queue; then WarehouseReceiver send data to warehouseProcessingService.processOrder(bookOrder);
WarehouseProcessingService sends to book.order.processed.queue and we set up synchronous service to listen this message configured in JmsConfig.java
```
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
  ```
![image](https://user-images.githubusercontent.com/1523220/173213440-f44d3d27-1704-429a-a3db-7eac34023e27.png)

In addition to the info above, we want to change all the hardcoded configuration to the proper configuration method 
using application.yml or application.properties

To use SingleConnectionFactory Approach --> JmsConfig.java
<b> SingleConnectionFactory</b>
- Returns the same Connection from all createConnection() calls
- Ignores calls to Connection.close()
- Is thread-safe compared to JDBC calls
- Shared connections can be recovered in case of exceptions.
  This is done updating setReconnectOnException(true).
  
  ```
  //Using SingleConnectionFactory approach
	@Value("${spring.activemq.broker-url}")
	private String brokerUrl;
	
	@Value("${spring.activemq.user}")
	private String user;
	
	@Value("${spring.activemq.password}")
	private String password;
	.
	.
	.
	.
	//comment out the previous connection and add the new one
	
	/*
	@Bean
	public ActiveMQConnectionFactory connectionFactory(){
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("admin","admin","tcp://localhost:61616");
		return factory;
	}
	*/
	@Bean
	public SingleConnectionFactory connectionFactory(){
		
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(user, password, brokerUrl);
		SingleConnectionFactory singleConnectionFactory = new SingleConnectionFactory(factory); 
		singleConnectionFactory.setReconnectOnException(true);
		singleConnectionFactory.setClientId("myclientId");
		return singleConnectionFactory;
	}
  ```

  <b>CachingConnectionFactory</b>
- Returns same connection from createConnection() calls
- Ignores calls to Connection.close()
- Is thread-safe compared to JDBC calls
- Default setReconnectOnException(true)

```
  //Using CachingConnectionFactory approach
	@Value("${spring.activemq.broker-url}")
	private String brokerUrl;
	
	@Value("${spring.activemq.user}")
	private String user;
	
	@Value("${spring.activemq.password}")
	private String password;
	.
	.
	.
	.
	//comment out the previous connection and add the new one
	
	/*
	@Bean
	public ActiveMQConnectionFactory connectionFactory(){
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("admin","admin","tcp://localhost:61616");
		return factory;
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

```