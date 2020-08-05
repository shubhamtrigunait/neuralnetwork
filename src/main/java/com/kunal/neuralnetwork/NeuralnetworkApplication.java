package com.kunal.neuralnetwork;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
public class NeuralnetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeuralnetworkApplication.class, args);
        List<Map<String, Object>> orders = readFile();
        List<String> l = new ArrayList<>(orders.get(0).keySet());
        NeuralNetwork brain = new NeuralNetwork(25, 10, 3);
        brain.setLearningRate(0.05);
        brain.setActivationFunction(new NeuralNetwork.ActivationFunction(Mat.TANH, Mat.TANH_DERIVATIVE));
        List<Integer> indexes = new ArrayList<>();

        for (int iter = 0; iter < 100; iter++) {
            for (int i = 0; i < orders.size(); i++) {
                double[] inputs = new double[25];
                double[] outputs = new double[3];
                for (int j = 0; j < 25; j++) {
                    if (Objects.nonNull(orders.get(i).get(l.get(j)))) {
                        inputs[j] = Double.valueOf(orders.get(i).get(l.get(j)).hashCode()) / Integer.MAX_VALUE;
                    }
                }
                if (!StringUtils.isEmpty(orders.get(i).get("fulfillment_status")) &&
                        orders.get(i).get("fulfillment_status").toString().equalsIgnoreCase("SHIPPED")) {
                    if (Objects.nonNull(orders.get(i).get("shipment_info")) && ((List) orders.get(i).get("shipment_info")).size() > 0) {
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


        for (int k = 0; k < indexes.size(); k++) {
            int i = indexes.get(k);
            if (!StringUtils.isEmpty(orders.get(i).get("fulfillment_status")) &&
                    orders.get(i).get("fulfillment_status").toString().equalsIgnoreCase("SHIPPED")) {
                if (Objects.nonNull(orders.get(i).get("shipment_info")) && ((List) orders.get(i).get("shipment_info")).size() > 0) {
                    System.out.println("1 0 0");
                }
                else{
                    System.out.println("0 0 1");
                }

            } else {
                System.out.println("0 1 0");
            }
            double[] inputs = new double[25];
            for (int j = 0; j < 25; j++) {
                if (Objects.nonNull(orders.get(i).get(l.get(j)))) {
                    inputs[j] = Double.valueOf(orders.get(i).get(l.get(j)).hashCode()) / Integer.MAX_VALUE;
                }
            }
            double[] outputs = brain.process(inputs);
            System.out.println("Test: " + Arrays.toString(inputs));
            System.out.println(" -> " + Arrays.toString(outputs));
        }
    }

    public static List<Map<String, Object>> readFile() {
        String line = "";
        List<Map<String, Object>> orders = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String file = "src/main/java/com/kunal/neuralnetwork/input.txt";
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                orders.add(mapper.readValue(line, Map.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orders;
    }

}
