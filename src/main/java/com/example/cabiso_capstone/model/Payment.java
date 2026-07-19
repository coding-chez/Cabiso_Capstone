package com.example.cabiso_capstone.model;

import java.time.LocalDate;

public class Payment {

    private int paymentId;
    private Tenant tenant;
    private String billingMonth;
    private double amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String referenceNumber;
    private String remarks;
    private String status;

    public Payment(
            int paymentId,
            Tenant tenant,
            String billingMonth,
            double amount,
            LocalDate paymentDate,
            String paymentMethod,
            String referenceNumber,
            String remarks,
            String status
    ) {

        this.paymentId = paymentId;
        this.tenant = tenant;
        this.billingMonth = billingMonth;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.referenceNumber = referenceNumber;
        this.remarks = remarks;
        this.status = status;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getBillingMonth() {
        return billingMonth;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getStatus() {
        return status;
    }
}