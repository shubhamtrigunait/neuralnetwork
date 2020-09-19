package com.kunal.neuralnetwork.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.kunal.neuralnetwork.brain.NeuralNetwork;
import com.kunal.neuralnetwork.constants.QuantityEnum;
import com.kunal.neuralnetwork.constants.ShipmentCategoryEnums;
import com.kunal.neuralnetwork.domain.Order;
import com.kunal.neuralnetwork.domain.Request;
import com.kunal.neuralnetwork.domain.Response;
import com.kunal.neuralnetwork.persistence.OrderModel;
import com.kunal.neuralnetwork.persistence.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.kunal.neuralnetwork.utils.OrderDataUtils.getCarrierCategory;
import static com.kunal.neuralnetwork.utils.OrderDataUtils.getInputNodesForOrder;
import static com.kunal.neuralnetwork.utils.OrderDataUtils.getItemCategory;
import static com.kunal.neuralnetwork.utils.OrderDataUtils.getShipmentCategory;

@Service
public class ClassificationService {

    private ObjectMapper objectMapper;
    private Map<String, NeuralNetwork> neuralNetworkMap;
    private Map<String, Map<String, Integer>> keyMap;
    private OrderRepository orderRepository;

    @Autowired
    public ClassificationService(ObjectMapper objectMapper, OrderRepository orderRepository) {
        this.objectMapper = objectMapper;
        this.neuralNetworkMap = new HashMap<>();
        this.keyMap = new HashMap<>();
        this.orderRepository = orderRepository;
        //TODO INITIALIZE MAPS FROM FILES
    }

    public Response getClassifiedOrder(String orderNumber) {
        OrderModel orderModel = orderRepository.findByOrderNumber(orderNumber);
        Response response = new Response();
        response.setOrderId(orderNumber);
        if (orderModel.getClassification().equalsIgnoreCase("correct")) {
            response.setStatus("200");
            response.setMessage("No anomaly in order data");
        } else {
            response.setStatus("400");
            response.setMessage("Anomaly in order data");
        }

        return response;
    }

    public Response classifyOrder(Request orderRequest) {
        Order order;
        try {
            order = objectMapper.readValue(orderRequest.getOrder(), Order.class);
        } catch (Exception e) {
            return new Response("400", "Syntax Error : " + e.getMessage(), "");
        }
        ShipmentCategoryEnums shipmentCategory = getShipmentCategory(order);
        QuantityEnum itemCategory = getItemCategory(order);
        QuantityEnum carrierCategory = getCarrierCategory(order);

        NeuralNetwork network = getNeuralNetwork(shipmentCategory, itemCategory, carrierCategory);
        Map<String, Integer> keyMap = getKeyMap(shipmentCategory, itemCategory, carrierCategory);

        Map<String, Object> orderMap = JsonFlattener.flattenAsMap(orderRequest.getOrder());
        double[] inputs = getInputNodesForOrder(keyMap.size(), orderMap, keyMap);
        double[] outputs = network.process(inputs);
        String orderNumber = null;
        if (Objects.nonNull(order.getOrderInfo())) {
            orderNumber = order.getOrderInfo().getOrderNumber();
        }
        if (outputs[0] > outputs[1]) {
            saveToDB(orderRequest.getOrder(), "correct", orderNumber, orderRequest.getRetailerMoniker());
            return new Response("200", "No Anomaly In Order Data", orderNumber);
        } else {
            saveToDB(orderRequest.getOrder(), "incorrect", orderNumber, orderRequest.getRetailerMoniker());
            return new Response("400", "Anomaly In Order Data", orderNumber);
        }
    }

    NeuralNetwork getNeuralNetwork(ShipmentCategoryEnums shipmentCategory, QuantityEnum itemCategory, QuantityEnum carrierCategory) {
        return this.neuralNetworkMap.get(shipmentCategory.name() + itemCategory.name() + carrierCategory.name());
    }

    Map<String, Integer> getKeyMap(ShipmentCategoryEnums shipmentCategory, QuantityEnum itemCategory, QuantityEnum carrierCategory) {
        return this.keyMap.get(shipmentCategory.name() + itemCategory.name() + carrierCategory.name());
    }

    private void saveToDB(String order, String classification, String orderNumber, String retailerMoniker) {
        OrderModel orderModel = new OrderModel();
        orderModel.setClassification(classification);
        orderModel.setOrderInfo(order);
        orderModel.setRetailerMoniker(retailerMoniker);
        orderModel.setOrderNumber(orderNumber);

        orderRepository.save(orderModel);
    }

}
