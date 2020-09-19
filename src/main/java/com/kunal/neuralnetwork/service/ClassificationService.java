package com.kunal.neuralnetwork.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.kunal.neuralnetwork.brain.NeuralNetwork;
import com.kunal.neuralnetwork.constants.QuantityEnum;
import com.kunal.neuralnetwork.constants.ShipmentCategoryEnums;
import com.kunal.neuralnetwork.domain.Order;
import com.kunal.neuralnetwork.domain.Response;
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

    @Autowired
    public ClassificationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.neuralNetworkMap = new HashMap<>();
        this.keyMap = new HashMap<>();
        //TODO INITIALIZE MAPS FROM FILES
    }

    public Response classifyOrder(String orderInfo) {
        Order order;
        try {
            order = objectMapper.readValue(orderInfo, Order.class);
        } catch (JsonProcessingException e) {
            return new Response("400", "Syntax Error : " + e.getMessage(), "");
        }
        ShipmentCategoryEnums shipmentCategory = getShipmentCategory(order);
        QuantityEnum itemCategory = getItemCategory(order);
        QuantityEnum carrierCategory = getCarrierCategory(order);

        NeuralNetwork network = getNeuralNetwork(shipmentCategory, itemCategory, carrierCategory);
        Map<String, Integer> keyMap = getKeyMap(shipmentCategory, itemCategory, carrierCategory);

        Map<String, Object> orderMap = JsonFlattener.flattenAsMap(orderInfo);
        double[] inputs = getInputNodesForOrder(keyMap.size(), orderMap, keyMap);
        double[] outputs = network.process(inputs);
        String orderNumber = null;
        if (Objects.nonNull(order.getOrderInfo())) {
            orderNumber = order.getOrderInfo().getOrderNumber();
        }
        if (outputs[0] > outputs[1]) {
            return new Response("200", "No Anomaly In Order Data", orderNumber);
        } else {
            return new Response("400", "Anomaly In Order Data", orderNumber);
        }
    }

    NeuralNetwork getNeuralNetwork(ShipmentCategoryEnums shipmentCategory, QuantityEnum itemCategory, QuantityEnum carrierCategory) {
        return this.neuralNetworkMap.get(shipmentCategory.name() + itemCategory.name() + carrierCategory.name());
    }

    Map<String, Integer> getKeyMap(ShipmentCategoryEnums shipmentCategory, QuantityEnum itemCategory, QuantityEnum carrierCategory) {
        return this.keyMap.get(shipmentCategory.name() + itemCategory.name() + carrierCategory.name());
    }


}
