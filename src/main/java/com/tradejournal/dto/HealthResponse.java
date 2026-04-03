package com.tradejournal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * HealthResponse — Server health check
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthResponse {
    public String status = "OK";
    public String version = "1.0.0";
    public String database = "TradeJournal";
    public Long timestamp = System.currentTimeMillis();
}
