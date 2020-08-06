package com.kunal.neuralnetwork;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@Slf4j
public class NeuralnetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeuralnetworkApplication.class, args);

        //TEST TO CHECK NEURAL NETWORK
        List<Map<String, Object>> orders = OrderDataUtils.readInputFileAsList("input.txt");
        Map<String, Integer> keyMap = OrderDataUtils.getKeySuperSetMapFromList(orders);
        int numInputNodes = keyMap.size();

        List<Map<String, Object>> orders2 = OrderDataUtils.readInputFileAsList("input2.txt");
        Map<String, Integer> keyMap2 = OrderDataUtils.getKeySuperSetMapFromList(orders2);
        int numInputNodes2 = keyMap2.size();

        int maxInputs = numInputNodes2;
        if (numInputNodes > numInputNodes2) {
            maxInputs = numInputNodes;
        }

        OrderDataSupervisedTrainer orderDataSupervisedTrainer = new OrderDataSupervisedTrainer();
        orderDataSupervisedTrainer.init(maxInputs, 2, 10, 3);
        orderDataSupervisedTrainer.trainUsingInput(orders, keyMap, 20);
        orderDataSupervisedTrainer.trainUsingInput(orders2, keyMap2, 20);

        orderDataSupervisedTrainer.writeWeightsToFile("weights.txt");

        OrderDataClassifier classifier = new OrderDataClassifier();
        classifier.initWeights(maxInputs, 2, 10, 3, "weights.txt");
        classifier.classifyOrders(orders, keyMap);
        classifier.classifyOrders(orders2, keyMap2);

        log.info("CORRECT CLASSIFICATIONS ->  " + classifier.getCorrectClassification());
        log.info("WRONG CLASSIFICATIONS ->  " + classifier.getIncorrectClassifications());
    }


}
