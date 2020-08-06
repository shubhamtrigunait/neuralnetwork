package com.kunal.neuralnetwork;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class OrderDataClassifier {

    NeuralNetwork brain;

    /**
     * Initialises the neural network with weights and biases using file saved after training
     */
    public void initWeights(int numInputNodes, int numHiddenLayers, int numHiddenNodes, int outputNodes, String file) {
        brain = new NeuralNetwork(numInputNodes, numHiddenLayers, numHiddenNodes, outputNodes);
        try {
            brain.readFrom(new File(file));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Classifies the list of orders into outputs
     */
    public void classifyOrders(List<Map<String, Object>> orders, Map<String, Integer> keyMap) {
        for (int k = 0; k < orders.size(); k++) {
            Map<String, Object> order = orders.get(k);
            double[] inputs = OrderDataUtils.getInputNodesForOrder(order, keyMap);
            double[] supervisedOutputs = OrderDataUtils.getSupervisedDesiredOutputNodesForOrder(order);

            double[] outputs = brain.process(inputs);
            log.info("SUPERVISED OUTPUT -> " + Arrays.toString(supervisedOutputs));
            log.info("OUTPUT -> " + Arrays.toString(outputs));
        }
    }
}
