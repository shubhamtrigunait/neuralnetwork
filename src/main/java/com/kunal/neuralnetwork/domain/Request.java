package com.kunal.neuralnetwork.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class Request {
    private String retailerMoniker;
    private String order;
}
