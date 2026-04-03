package com.tradejournal.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TradeResponse — Outgoing trade data to frontend
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeResponse {
    @JsonProperty("id")
    public Long tradeId;
    
    @JsonProperty("symbol")
    public String symbol;
    
    @JsonProperty("type")
    public String tradeType;
    
    @JsonProperty("qty")
    public Double quantity;
    
    @JsonProperty("price")
    public Double price;
    
    @JsonProperty("value")
    public Double tradeValue;
    
    @JsonProperty("strategy")
    public String strategyId;
    
    @JsonProperty("date")
    public LocalDateTime tradeDate;
}
