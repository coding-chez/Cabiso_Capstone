package com.example.cabiso_capstone.model;

import java.time.LocalDate;

public class Payment {
    private int paymentId;
    private Tenant tenant;
    private double amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String status;
    private String referenceNumber;

    public Payment() {
    }

    public Payment(int paymentId, Tenant tenant, double amount, LocalDate paymentDate, String paymentMethod, String status, String referenceNumber) {
        this.paymentId = paymentId;
        this.tenant = tenant;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.referenceNumber = referenceNumber;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getTenantName() {
        if (tenant == null) {
            return "Unknown Tenant";
        }

        return tenant.getFullName();
    }

    public String getRoomNumber() {
        if (tenant == null) {
            return "Not Assigned";
        }

        return tenant.getAssignedRoomNumber();
    }

    public boolean isPaid() {
        return "Paid".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Payment #" + paymentId
                + " - " + getTenantName();
    }
}
