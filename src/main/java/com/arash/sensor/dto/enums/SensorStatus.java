package com.arash.sensor.dto.enums;

public enum SensorStatus {
    OK("OK"),
    WARN("WARN"),
    ALERT("ALERT"),
    UNKNOWN("UNKNOWN");

    SensorStatus(String value) {
        this.value = value;
    }

    private String value;
}
