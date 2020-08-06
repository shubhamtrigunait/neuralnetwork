package com.kunal.neuralnetwork;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
class OrderDataSupervisedTrainer {

    NeuralNetwork brain;

    public void init(int numInputNodes, int numHiddenLayers, int numHiddenNodes, int outputNodes) {
        brain = new NeuralNetwork(numInputNodes, numHiddenLayers, numHiddenNodes, outputNodes);
        brain.setLearningRate(0.10);
        brain.setActivationFunction(new NeuralNetwork.ActivationFunction(Mat.SIGMOID, Mat.SIGMOID_DERIVATIVE));
    }

    void trainUsingInput(List<Map<String, Object>> orders, Map<String, Integer> keyHashMap, Integer iterations) {
        int sizeInputList = orders.size();
        for (int i = 0; i < sizeInputList * iterations; i++) {
            int index = i % sizeInputList;
            Map<String, Object> order = orders.get(index);
            double[] inputs = OrderDataUtils.getInputNodesForOrder(order, keyHashMap);
            double[] outputs = OrderDataUtils.getSupervisedDesiredOutputNodesForOrder(order);
            brain.train(inputs, outputs);
        }
    }

    void writeWeightsToFile(String fileName) {
        try {
            File file = new File(fileName);
            brain.writeTo(file);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
