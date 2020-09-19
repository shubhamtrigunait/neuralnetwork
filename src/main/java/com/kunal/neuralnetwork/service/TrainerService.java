package com.kunal.neuralnetwork.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.kunal.neuralnetwork.brain.Mat;
import com.kunal.neuralnetwork.brain.NeuralNetwork;
import com.kunal.neuralnetwork.constants.QuantityEnum;
import com.kunal.neuralnetwork.constants.ShipmentCategoryEnums;
import com.kunal.neuralnetwork.domain.Order;
import com.kunal.neuralnetwork.utils.OrderDataUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kunal.neuralnetwork.utils.OrderDataUtils.getCarrierCategory;
import static com.kunal.neuralnetwork.utils.OrderDataUtils.getInputNodesForOrder;
import static com.kunal.neuralnetwork.utils.OrderDataUtils.getItemCategory;
import static com.kunal.neuralnetwork.utils.OrderDataUtils.getShipmentCategory;

@Slf4j
@Data
public class TrainerService {

    private Map<String, NeuralNetwork> neuralNetworkMap;
    private Map<String, Map<String, Integer>> keyMap;

    /**
     * Initialises the neural network with random weights and biases
     */
    public void init(Map<String, Map<String, Integer>> keyMapInput) {
        neuralNetworkMap = new HashMap<>();
        this.keyMap = keyMapInput;
        for (ShipmentCategoryEnums shipment : ShipmentCategoryEnums.values()) {
            for (QuantityEnum item : QuantityEnum.values()) {
                for (QuantityEnum carrier : QuantityEnum.values()) {
                    NeuralNetwork neuralNetwork = new NeuralNetwork(keyMapInput.get(shipment.name() + item.name() + carrier.name()).size(), 3, 10, 2);
                    neuralNetwork.setLearningRate(0.10);
                    neuralNetwork.setActivationFunction(new NeuralNetwork.ActivationFunction(Mat.SIGMOID, Mat.SIGMOID_DERIVATIVE));
                    neuralNetworkMap.put(shipment.name() + item.name() + carrier.name(), neuralNetwork);
                }
            }
        }

    }

    /**
     * Trains the neural network using the given list of orders and their desired outputs
     */
    void trainUsingInput(String orderInfo, Boolean isValid) {
        Order order;
        try {
            order = new ObjectMapper().readValue(orderInfo, Order.class);
            ShipmentCategoryEnums shipmentCategory = getShipmentCategory(order);
            QuantityEnum itemCategory = getItemCategory(order);
            QuantityEnum carrierCategory = getCarrierCategory(order);

            NeuralNetwork network = getNeuralNetwork(shipmentCategory, itemCategory, carrierCategory);
            Map<String, Integer> keyMap = getKeyMap(shipmentCategory, itemCategory, carrierCategory);

            Map<String, Object> orderMap = JsonFlattener.flattenAsMap(orderInfo);
            double[] inputs = getInputNodesForOrder(keyMap.size(), orderMap, keyMap);
            double[] outputs = new double[2];
            if (isValid) {
                outputs[0] = 1;
                outputs[1] = 0;

            } else {
                outputs[0] = 0;
                outputs[1] = 1;
            }
            network.train(inputs, outputs);
        } catch (JsonProcessingException e) {
            log.error("invalid order");
        }

    }

    /**
     * Writes the current weight and biases of the neural network to a file
     */
    void writeWeightsAndKeyMapToFile() {
        try {
            for (ShipmentCategoryEnums shipment : ShipmentCategoryEnums.values()) {
                for (QuantityEnum item : QuantityEnum.values()) {
                    for (QuantityEnum carrier : QuantityEnum.values()) {
                        NeuralNetwork neuralNetwork = neuralNetworkMap.get(shipment.name() + item.name() + carrier.name());
                        File file = new File(shipment.name() + item.name() + carrier.name() + ".txt");
                        neuralNetwork.writeTo(file);
                    }
                }
            }
            FileWriter fileWriter = new FileWriter("keyMap.txt");
            fileWriter.write(new ObjectMapper().writeValueAsString(keyMap));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    NeuralNetwork getNeuralNetwork(ShipmentCategoryEnums shipmentCategory, QuantityEnum itemCategory, QuantityEnum carrierCategory) {
        return this.neuralNetworkMap.get(shipmentCategory.name() + itemCategory.name() + carrierCategory.name());
    }

    Map<String, Integer> getKeyMap(ShipmentCategoryEnums shipmentCategory, QuantityEnum itemCategory, QuantityEnum carrierCategory) {
        return this.keyMap.get(shipmentCategory.name() + itemCategory.name() + carrierCategory.name());
    }

    public static void main(String[] args) throws Exception {
        //TEST TO CHECK NEURAL NETWORK
        List<String> orders = OrderDataUtils.readInputFileAsList("input.txt");
        List<String> orders2 = OrderDataUtils.readInputFileAsList("input2.txt");
        orders.addAll(orders2);
        Map<String, Map<String, Integer>> keyMap = OrderDataUtils.getKeyMap(orders);
        TrainerService trainerService = new TrainerService();
        trainerService.init(keyMap);

        orders = OrderDataUtils.readInputFileAsList("input.txt");
        orders2 = OrderDataUtils.readInputFileAsList("input2.txt");

        for (int j = 0; j < 11; j++) {
            for (int i = 0; i < 11; i++) {
                trainerService.trainUsingInput(orders.get(i), true);
            }

            for (int i = 0; i < 11; i++) {
                trainerService.trainUsingInput(orders2.get(i), false);
            }
        }
        trainerService.writeWeightsAndKeyMapToFile();


    }
}
