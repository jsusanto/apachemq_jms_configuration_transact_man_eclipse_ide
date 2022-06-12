package com.linkedinjms.chapter4.service.jms;

import javax.management.RuntimeErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import com.linkedinjms.chapter4.pojos.BookOrder;

@Service
public class WarehouseReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WarehouseReceiver.class);

    @Autowired
    private WarehouseProcessingService warehouseProcessingService;

    @JmsListener(destination = "book.order.queue")
    public void receive(BookOrder bookOrder){
        LOGGER.info("Message received!");
        LOGGER.info("Message is == " + bookOrder);
        
        //Force exception to test the Transaction Manager approach
        if(bookOrder.getBook().getTitle().startsWith("L")) {
        	throw new RuntimeException("OrderId=" + bookOrder.getBookOrderId() + 
        			", Book begins with L is not allowed");
        }
        
        warehouseProcessingService.processOrder(bookOrder);
    }
}
