package com.kunal.neuralnetwork;

import com.github.wnameless.json.flattener.JsonFlattener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.Math.abs;

@Slf4j
class OrderDataUtils {

    private OrderDataUtils() {

    }

    static double[] getSupervisedDesiredOutputNodesForOrder(Map<String, Object> order) {
        double[] outputs = new double[3];
        if (!StringUtils.isEmpty(order.get("fulfillment_status")) &&
                order.get("fulfillment_status").toString().equalsIgnoreCase("SHIPPED")) {
            if (!StringUtils.isEmpty(order.get("shipment_info[0].tracking_number"))) {
                outputs[0] = 1;
            } else {
                outputs[2] = 1;
            }
        } else {
            outputs[1] = 1;
        }
        return outputs;
    }

    static double[] getInputNodesForOrder(Map<String, Object> order, Map<String, Integer> keyHashMap) {
        int numInputNodes = keyHashMap.size();
        List<String> keyList = new ArrayList<>(keyHashMap.keySet());
        double[] inputs = new double[numInputNodes];
        for (int j = 0; j < numInputNodes; j++) {
            if (Objects.nonNull(order.get(keyList.get(j)))
            ) {
                int max = keyHashMap.get(keyList.get(j));
                if (max > 0) {
                    inputs[j] = (double) order.get(keyList.get(j)).hashCode() / (double) max;
                }
            }
        }
        return inputs;
    }

    static List<Map<String, Object>> readInputFileAsList(String fileName) {
        String line;
        List<Map<String, Object>> orders = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                orders.add(JsonFlattener.flattenAsMap(line));
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return orders;
    }

    static Map<String, Integer> getKeySuperSetMapFromList(List<Map<String, Object>> list) {
        Map<String, Integer> keyHashMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Integer hashCode = entry.getValue().hashCode();
                hashCode = abs(hashCode);
                if (keyHashMap.containsKey(entry.getKey())) {
                    Integer max = keyHashMap.get(entry.getKey());
                    if (max < hashCode) {
                        keyHashMap.put(entry.getKey(), hashCode);
                    }
                } else {
                    keyHashMap.put(entry.getKey(), hashCode);
                }
            }
        }
        return keyHashMap;
    }
}
