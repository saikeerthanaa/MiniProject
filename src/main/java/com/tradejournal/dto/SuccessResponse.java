package com.tradejournal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * SuccessResponse — User-friendly success message after trade submission
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse {
    public String message;
    public TradeResponse trade;
    public AnalyticsResponse analytics;
    
    public SuccessResponse(String message, TradeResponse trade, AnalyticsResponse analytics) {
        this.message = message;
        this.trade = trade;
        this.analytics = analytics;
    }
}
