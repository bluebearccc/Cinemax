package com.bluebear.cinemax.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "Customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private int customerId;


    @Column(name = "AccountID", nullable = false)
    private String account;

    @Column(name = "FullName", length = 100, nullable = false)
    private String fullName;

    @Column(name = "Phone", length = 20)
    private String phone;

    // Constructors
    public Customer() {
    }

    public Customer(String account, String fullName, String phone) {
        this.account = account;
        this.fullName = fullName;
        this.phone = phone;
    }

    // Getters and Setters
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // toString Method

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return customerId == customer.customerId && Objects.equals(account, customer.account) && Objects.equals(fullName, customer.fullName) && Objects.equals(phone, customer.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, account, fullName, phone);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", account='" + account + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
