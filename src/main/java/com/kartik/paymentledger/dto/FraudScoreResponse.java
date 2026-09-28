package com.kartik.paymentledger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FraudScoreResponse {
    @JsonProperty("is_fraud")
    private boolean isFraud;

    @JsonProperty("fraud_probability")
    private double fraudProbability;

    private String decision;

    public boolean isFraud() { return isFraud; }
    public void setFraud(boolean fraud) { isFraud = fraud; }

    public double getFraudProbability() { return fraudProbability; }
    public void setFraudProbability(double fraudProbability) { this.fraudProbability = fraudProbability; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
}