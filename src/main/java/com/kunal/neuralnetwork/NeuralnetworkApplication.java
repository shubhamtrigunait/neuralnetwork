package com.kunal.neuralnetwork;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class NeuralnetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeuralnetworkApplication.class, args);
        List<Map<String, Object>> orders = readFile();
        List<String> l = new ArrayList<>(orders.get(0).keySet());
        System.out.println(l);
        NeuralNetwork brain = new NeuralNetwork(2, 2, 2);
        brain.setLearningRate(0.05);
        brain.setActivationFunction(new NeuralNetwork.ActivationFunction(Mat.TANH, Mat.TANH_DERIVATIVE));

        int[][] xorInputs = {{0, 0}, {0, 1}, {1, 0}, {1, 1}};
        for (int i = 0; i < xorInputs.length * 2000; i++) {
            int[] arr = xorInputs[i % 4];
            double[] inputs = {arr[0], arr[1]};
            double[] outputs = {arr[0] ^ arr[1], arr[0] ^ arr[1]};
            brain.train(inputs, outputs);
        }

        for (int i = 0; i < xorInputs.length; i++) {
            double[] inputs = {xorInputs[i][0], xorInputs[i][1]};
            double[] outputs = brain.process(inputs);
            System.out.println("Test: " + Arrays.toString(inputs) + " -> " + Arrays.toString(outputs));
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
