package dev.syntax.external.kis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MultiPriceRes {

    @JsonProperty("output")
    private List<PriceItem> output;
}