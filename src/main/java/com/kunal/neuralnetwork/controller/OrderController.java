package com.kunal.neuralnetwork.controller;

import com.kunal.neuralnetwork.domain.Request;
import com.kunal.neuralnetwork.domain.Response;
import com.kunal.neuralnetwork.service.ClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
public class OrderController {
    private ClassificationService service;

    @Autowired
    public OrderController(final ClassificationService service) {
        this.service = service;
    }

    @PostMapping(path = "/api/order", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public Response processOrder(Request order) {
        return service.classifyOrder(order);
    }

    @GetMapping(path = "api/order/{orderNumber}", produces = APPLICATION_JSON_VALUE)
    public Response getOrder(@PathVariable("orderNumber") String orderNumber) {
        return service.getClassifiedOrder(orderNumber);
    }
}
