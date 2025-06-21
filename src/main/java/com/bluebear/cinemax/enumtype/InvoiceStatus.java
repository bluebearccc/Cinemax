package com.bluebear.cinemax.enumtype;

public enum InvoiceStatus {
    Booked("Booked"),
    Cancelled("Cancelled");

    private final String dbValue;

    InvoiceStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static InvoiceStatus fromDbValue(String value) {
        for (InvoiceStatus status : values()) {
            if (status.dbValue.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status value: " + value);
    }
}

