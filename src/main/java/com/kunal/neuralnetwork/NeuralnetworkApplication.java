package com.kunal.neuralnetwork;

import com.github.wnameless.json.flattener.JsonFlattener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SpringBootApplication
public class NeuralnetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeuralnetworkApplication.class, args);
        List<Map<String, Object>> orders = readFile();
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < orders.size(); i++) {
            keys.addAll(orders.get(i).keySet());
        }
        List<String> l = new ArrayList<>(keys);
        int numInputNodes = l.size();
        NeuralNetwork brain = new NeuralNetwork(numInputNodes, 10, 3);
        brain.setLearningRate(0.10);
        brain.setActivationFunction(new NeuralNetwork.ActivationFunction(Mat.SIGMOID, Mat.SIGMOID_DERIVATIVE));
        Set<Integer> indexes = new HashSet<>();

        for (int iter = 0; iter < 100; iter++) {
            for (int i = 0; i < orders.size(); i++) {
                double[] inputs = new double[numInputNodes];
                double[] outputs = new double[3];
                for (int j = 0; j < numInputNodes; j++) {
                    if (Objects.nonNull(orders.get(i).get(l.get(j)))) {
                        inputs[j] = Double.valueOf(orders.get(i).get(l.get(j)).hashCode()) / Integer.MAX_VALUE;
                    }
                }
                if (!StringUtils.isEmpty(orders.get(i).get("fulfillment_status")) &&
                        orders.get(i).get("fulfillment_status").toString().equalsIgnoreCase("SHIPPED")) {
                    if (!StringUtils.isEmpty(orders.get(i).get("shipment_info[0].tracking_number"))) {
                        outputs[0] = 1;
                        outputs[1] = 0;
                        outputs[2] = 0;
                    } else {
                        outputs[0] = 0;
                        outputs[1] = 0;
                        outputs[2] = 1;
                    }
                    indexes.add(i);
                    indexes.add(i + 1);
                } else {
                    outputs[0] = 0;
                    outputs[1] = 1;
                    outputs[2] = 0;
                }
                brain.train(inputs, outputs);
            }
        }

        List<Integer> indexesList = new ArrayList<>(indexes);
        for (int k = 0; k < indexes.size(); k++) {
            int i = indexesList.get(k);
            System.out.println(orders.get(i).get("order_number"));
            if (!StringUtils.isEmpty(orders.get(i).get("fulfillment_status")) &&
                    orders.get(i).get("fulfillment_status").toString().equalsIgnoreCase("SHIPPED")) {
                if (!StringUtils.isEmpty(orders.get(i).get("shipment_info[0].tracking_number"))) {
                    System.out.println("1 0 0");
                } else {
                    System.out.println("0 0 1");
                }

            } else {
                System.out.println("0 1 0");
            }
            double[] inputs = new double[numInputNodes];
            for (int j = 0; j < numInputNodes; j++) {
                if (Objects.nonNull(orders.get(i).get(l.get(j)))) {
                    inputs[j] = Double.valueOf(orders.get(i).get(l.get(j)).hashCode()) / Integer.MAX_VALUE;
                }
            }
            double[] outputs = brain.process(inputs);
            System.out.println("OUTPUT -> " + Arrays.toString(outputs));
        }
    }

    public static List<Map<String, Object>> readFile() {
        String line;
        List<Map<String, Object>> orders = new ArrayList<>();
        String file = "src/main/java/com/kunal/neuralnetwork/input.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((line = br.readLine()) != null) {
                orders.add(JsonFlattener.flattenAsMap(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orders;
    }

}
