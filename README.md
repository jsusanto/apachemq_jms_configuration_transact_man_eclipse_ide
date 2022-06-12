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

<b>Transaction Management with Spring JMS</b>
By having this you can rollback if it fails to send.

How to?
Step 1. Add PlatformTransactionManager method in JmsConfig.java

```
	@Bean
	public PlatformTransactionManager jmsTransactionManager() {
		return new JmsTransactionManager(connectionFactory());
	}
```

Step 2. Add @EnableTransactionManagement annotation on the top of JmsConfig.java class
```
@EnableTransactionManagement
@EnableJms
@Configuration
public class JmsConfig implements JmsListenerConfigurer{
```

Step 3. Add @Transactional annotation on all sender services (Sender.java, BookOrderService.java)

```
  @Transactional
	public void send(BookOrder bookOrder) {
		jmsTemplate.convertAndSend(BOOK_QUEUE, bookOrder);
	}

```

How to test?
Note: in this project, we removed WarehouseReceiverService.java to avoid any stand-by reading to test transaction management method

Step 1. Ensure that your ApacheMQ and application are running.

Step 2. Add order via web app for book starts with 'L'

Step 3. Check in the ApacheMQ dashboard, you'll see the following screenshots

![image](https://user-images.githubusercontent.com/1523220/173228555-dd7d1c90-98e7-4c4c-ae9b-24df73efb93e.png)

![image](https://user-images.githubusercontent.com/1523220/173228568-aeb231a0-7a6e-401d-9213-c8486d95dc47.png)

![image](https://user-images.githubusercontent.com/1523220/173228581-947a8628-6f1b-42a8-9839-5887154576b7.png)


