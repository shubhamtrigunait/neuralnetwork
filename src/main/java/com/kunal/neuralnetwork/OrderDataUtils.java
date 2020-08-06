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

    /**
     * Returns the value of desired output nodes for one order
     */
    static double[] getSupervisedDesiredOutputNodesForOrder(Map<String, Object> order) {
        double[] outputs = new double[3];
        if (!StringUtils.isEmpty(order.get("retailer_moniker")) &&
                !order.get("retailer_moniker").toString().equalsIgnoreCase("7forallmankind")) {
            // SPECIFIES THAT THE ORDER IS FROM INPUT FILE 2 AS RETAILER IS NOT 7forallmankind
            outputs[2] = 1;
        } else if (!StringUtils.isEmpty(order.get("fulfillment_status")) &&
                order.get("fulfillment_status").toString().equalsIgnoreCase("SHIPPED")) {
            if (!StringUtils.isEmpty(order.get("shipment_info[0].tracking_number"))) {
                // SPECIFIES THAT THE ORDER IS SHIPPED AND HAS SHIPMENTS PRESENT OUTPUT -> 1 0 0
                outputs[0] = 1;
            } else {
                // SPECIFIES THAT THE ORDER IS SHIPPED AND HAS NO SHIPMENTS PRESENT OUTPUT -> 0 0 1
                outputs[2] = 1;
            }
        } else {
            // SPECIFIES THAT THE ORDER IS NOT SHIPPED OUTPUT -> 0 1 0
            outputs[1] = 1;
        }
        return outputs;
    }

    /**
     * Returns the value of input nodes for one order
     */
    static double[] getInputNodesForOrder(int numInputNodes, Map<String, Object> order, Map<String, Integer> keyHashMap) {
        List<String> keyList = new ArrayList<>(keyHashMap.keySet());
        double[] inputs = new double[numInputNodes];
        for (int j = 0; j < keyList.size(); j++) {
            if (Objects.nonNull(order.get(keyList.get(j)))
            ) {
                // COMPUTING INTEGER HASH FOR THE KEY AND DIVIDING BY MAX VALUE FOR THAT KEY -> RANGE [0,1]
                int max = keyHashMap.get(keyList.get(j));
                if (max > 0) {
                    inputs[j] = (double) order.get(keyList.get(j)).hashCode() / (double) max;
                }
            }
        }
        return inputs;
    }

    /**
     * Returns a List of Orders as a Flat Map with all keys at root level
     */
    static List<Map<String, Object>> readInputFileAsList(String fileName) {
        String line;
        List<Map<String, Object>> orders = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                // READING THE JSON AS A FLAT MAP TO GET ALL NODES AT INPUT LEVEL
                try {
                    orders.add(JsonFlattener.flattenAsMap(line));
                } catch (Exception e) {
                    log.error("Error in order line : " + orders.size());
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return orders;
    }

    /**
     * Returns a Map with super set of keys for all orders with value as maximum value for that key in all orders
     */
    static Map<String, Integer> getKeySuperSetMapFromList(List<Map<String, Object>> list) {
        Map<String, Integer> keyHashMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Integer hashCode = 1;
                if (Objects.nonNull(entry.getValue())) {
                    hashCode = entry.getValue().hashCode();
                }
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
