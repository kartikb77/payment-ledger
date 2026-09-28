package com.kartik.paymentledger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FraudScoreRequest {

    private double amount;

    @JsonProperty("hour_of_day")
    private int hourOfDay;

    @JsonProperty("transaction_velocity")
    private int transactionVelocity;

    @JsonProperty("is_new_device")
    private int isNewDevice;

    @JsonProperty("account_age_days")
    private int accountAgeDays;

    public FraudScoreRequest(double amount, int hourOfDay, int transactionVelocity, int isNewDevice, int accountAgeDays) {
        this.amount = amount;
        this.hourOfDay = hourOfDay;
        this.transactionVelocity = transactionVelocity;
        this.isNewDevice = isNewDevice;
        this.accountAgeDays = accountAgeDays;
    }

    public double getAmount() { return amount; }
    public int getHourOfDay() { return hourOfDay; }
    public int getTransactionVelocity() { return transactionVelocity; }
    public int getIsNewDevice() { return isNewDevice; }
    public int getAccountAgeDays() { return accountAgeDays; }
}