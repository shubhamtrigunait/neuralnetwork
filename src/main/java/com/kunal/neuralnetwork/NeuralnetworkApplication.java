package com.kunal.neuralnetwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class NeuralnetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeuralnetworkApplication.class, args);

        //TEST TO CHECK NEURAL NETWORK
        List<Map<String, Object>> orders = OrderDataUtils.readInputFileAsList("input.txt");
        Map<String, Integer> keyMap = OrderDataUtils.getKeySuperSetMapFromList(orders);
        int numInputNodes = keyMap.size();

        OrderDataSupervisedTrainer orderDataSupervisedTrainer = new OrderDataSupervisedTrainer();
        orderDataSupervisedTrainer.init(numInputNodes, 2, 10, 3);
        orderDataSupervisedTrainer.trainUsingInput(orders, keyMap, 20);
        orderDataSupervisedTrainer.writeWeightsToFile("weights.txt");

        OrderDataClassifier classifier = new OrderDataClassifier();
        classifier.initWeights(numInputNodes, 2, 10, 3, "weights.txt");
        classifier.classifyOrders(orders, keyMap);
    }


}
