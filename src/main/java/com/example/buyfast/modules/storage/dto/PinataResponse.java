package com.example.buyfast.modules.storage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PinataResponse {

    @JsonProperty("IpfsHash")
    private String ipfsHash;

    @JsonProperty("PinSize")
    private long pinSize;

    @JsonProperty("Timestamp")
    private String timestamp;
}