package com.kunal.neuralnetwork.persistence;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "orders")
public class OrderModel {
    @Column
    @PrimaryKey
    private String orderNumber;
    @Column
    private String classification;
    @Column
    private String retailerMoniker;
    @Column
    private String orderInfo;
}
