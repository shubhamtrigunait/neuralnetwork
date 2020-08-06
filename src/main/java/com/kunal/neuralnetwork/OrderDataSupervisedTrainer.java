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
    int numInputNodes;

    /**
     * Initialises the neural network with random weights and biases
     */
    public void init(int numInputNodes, int numHiddenLayers, int numHiddenNodes, int outputNodes) {
        this.numInputNodes = numInputNodes;
        brain = new NeuralNetwork(numInputNodes, numHiddenLayers, numHiddenNodes, outputNodes);
        brain.setLearningRate(0.10);
        brain.setActivationFunction(new NeuralNetwork.ActivationFunction(Mat.SIGMOID, Mat.SIGMOID_DERIVATIVE));
    }

    /**
     * Trains the neural network using the given list of orders and their desired outputs
     */
    void trainUsingInput(List<Map<String, Object>> orders, Map<String, Integer> keyHashMap, Integer iterations) {
        int sizeInputList = orders.size();
        for (int i = 0; i < sizeInputList * iterations; i++) {
            int index = i % sizeInputList;
            Map<String, Object> order = orders.get(index);
            double[] inputs = OrderDataUtils.getInputNodesForOrder(numInputNodes, order, keyHashMap);
            double[] outputs = OrderDataUtils.getSupervisedDesiredOutputNodesForOrder(order);
            brain.train(inputs, outputs);
        }
    }

    /**
     * Writes the current weight and biases of the neural network to a file
     */
    void writeWeightsToFile(String fileName) {
        try {
            File file = new File(fileName);
            brain.writeTo(file);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
