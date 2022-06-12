package com.linkedinjms.chapter4.service.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.linkedinjms.chapter4.pojos.BookOrder;
import com.linkedinjms.chapter4.pojos.ProcessedBookOrder;

import java.util.Date;

@Service
public class WarehouseProcessingService {
	private static final String BOOK_ORDER = "book.order.processed.queue";
	
    @Autowired
    private JmsTemplate jmsTemplate;

    public void processOrder(BookOrder bookOrder){
        ProcessedBookOrder order = new ProcessedBookOrder(
                bookOrder,
                new Date(),
                new Date()
        );
        jmsTemplate.convertAndSend(BOOK_ORDER, bookOrder);
    }

}
