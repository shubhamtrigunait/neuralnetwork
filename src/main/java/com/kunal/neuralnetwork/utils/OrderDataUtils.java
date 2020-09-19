package com.kunal.neuralnetwork.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.kunal.neuralnetwork.constants.QuantityEnum;
import com.kunal.neuralnetwork.constants.ShipmentCategoryEnums;
import com.kunal.neuralnetwork.domain.ItemsInfo;
import com.kunal.neuralnetwork.domain.Order;
import com.kunal.neuralnetwork.domain.OrderItem;
import com.kunal.neuralnetwork.domain.Shipment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.lang.Math.abs;

@Slf4j
public class OrderDataUtils {

    private OrderDataUtils() {

    }

    public static ShipmentCategoryEnums getShipmentCategory(Order order) {
        if (Objects.nonNull(order)
                && Objects.nonNull(order.getOrderInfo())) {
            List<Shipment> shipmentList = order.getOrderInfo().getShipments();
            if (!CollectionUtils.isEmpty(shipmentList)) {
                if (shipmentList.size() == 1) {
                    return ShipmentCategoryEnums.SINGLE;
                } else {
                    Set<String> skus = new HashSet<>();
                    for (Shipment shipment : shipmentList) {
                        for (ItemsInfo itemsInfo : shipment.getItemsInfo()) {
                            if (skus.contains(itemsInfo.getSku())) {
                                return ShipmentCategoryEnums.SPLIT;
                            }
                            skus.add(itemsInfo.getSku());
                        }
                    }
                    return ShipmentCategoryEnums.MULTIPLE;
                }
            }

        }
        return ShipmentCategoryEnums.NONE;
    }

    public static QuantityEnum getItemCategory(Order order) {
        if (Objects.nonNull(order)
                && Objects.nonNull(order.getOrderInfo())) {
            List<OrderItem> orderItems = order.getOrderInfo().getOrderItems();
            if (!CollectionUtils.isEmpty(orderItems)) {
                if (orderItems.size() == 1) {
                    return QuantityEnum.SINGLE;
                } else {
                    return QuantityEnum.MULTIPLE;
                }
            }
        }
        return QuantityEnum.NONE;
    }

    public static QuantityEnum getCarrierCategory(Order order) {
        if (Objects.nonNull(order)
                && Objects.nonNull(order.getOrderInfo())) {
            List<Shipment> shipmentList = order.getOrderInfo().getShipments();
            if (!CollectionUtils.isEmpty(shipmentList)) {
                if (shipmentList.size() == 1) {
                    return QuantityEnum.SINGLE;
                } else {
                    Set<String> carriers = new HashSet<>();
                    for (Shipment shipment : shipmentList) {
                        if (!carriers.contains(shipment.getCarrier())) {
                            return QuantityEnum.MULTIPLE;
                        }
                        carriers.add(shipment.getCarrier());
                    }
                    return QuantityEnum.SINGLE;
                }
            }
        }
        return QuantityEnum.NONE;
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
    public static double[] getInputNodesForOrder(int numInputNodes, Map<String, Object> order, Map<String, Integer> keyHashMap) {
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
    public static List<String> readInputFileAsList(String fileName) {
        String line;
        List<String> orders = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                // READING THE JSON AS A FLAT MAP TO GET ALL NODES AT INPUT LEVEL
                try {
                    orders.add(line);
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
    public static Map<String, Map<String, Integer>> getKeyMap(List<String> orderList) {
        Map<String, Map<String, Integer>> keyMap = new HashMap<>();
        for (ShipmentCategoryEnums shipment : ShipmentCategoryEnums.values()) {
            for (QuantityEnum item : QuantityEnum.values()) {
                for (QuantityEnum carrier : QuantityEnum.values()) {
                    keyMap.put(shipment.name() + item.name() + carrier.name(), new HashMap<>());
                }
            }
        }
        for (int i = 0; i < orderList.size(); i++) {
            String orderInfo = orderList.get(i);
            Order order;
            try {
                order = new ObjectMapper().readValue(orderInfo, Order.class);
                ShipmentCategoryEnums shipmentCategory = getShipmentCategory(order);
                QuantityEnum itemCategory = getItemCategory(order);
                QuantityEnum carrierCategory = getCarrierCategory(order);

                Map<String, Integer> keyMapForOrderType = getKeyMapForOrderType(keyMap, shipmentCategory, itemCategory, carrierCategory);

                Map<String, Object> orderMap = JsonFlattener.flattenAsMap(orderInfo);
                for (Map.Entry<String, Object> entry : orderMap.entrySet()) {
                    Integer hashCode = 1;
                    if (Objects.nonNull(entry.getValue())) {
                        hashCode = entry.getValue().hashCode();
                    }
                    hashCode = abs(hashCode);
                    if (keyMapForOrderType.containsKey(entry.getKey())) {
                        Integer max = keyMapForOrderType.get(entry.getKey());
                        if (max < hashCode) {
                            keyMapForOrderType.put(entry.getKey(), hashCode);
                        }
                    } else {
                        keyMapForOrderType.put(entry.getKey(), hashCode);
                    }
                }
            } catch (Exception e) {
                log.error("invalid order");
            }
        }
        return keyMap;
    }

    static Map<String, Integer> getKeyMapForOrderType(Map<String, Map<String, Integer>> keyMap, ShipmentCategoryEnums shipmentCategory, QuantityEnum itemCategory, QuantityEnum carrierCategory) {
        return keyMap.get(shipmentCategory.name() + itemCategory.name() + carrierCategory.name());
    }
}
