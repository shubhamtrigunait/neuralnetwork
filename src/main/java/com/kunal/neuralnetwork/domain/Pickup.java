
/*
 * *
 *  * Copyright (c) 2019 Narvar Inc.
 *  * All rights reserved
 *
 */

package com.kunal.neuralnetwork.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "carrier",
        "tracking_number",
        "type",
        "items_info",
        "status",
        "eta",
        "pickup_by_date",
        "attributes",
        "store"
})
public class Pickup {

    @JsonProperty("id")
    public String id;
    @JsonProperty("carrier")
    public String carrier;
    @JsonProperty("tracking_number")
    public String trackingNumber;
    @JsonProperty("type")
    public String type;
    @JsonProperty("items_info")
    public List<ItemsInfo> itemsInfo = null;
    @JsonProperty("status")
    public Status status;
    @JsonProperty("eta")
    public String eta;
    @JsonProperty("pickup_by_date")
    public String pickupByDate;
    @JsonProperty("attributes")
    public Attributes attributes;
    @JsonProperty("store")
    public Store store;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
