package com.example.demo.demos.common;

public enum TaskStatus {
    INIT("INIT"),
    SUCCESS("SUCCESS"),
    FAIL("FAIL"),
    RUNNING("RUNNING"),
    STOP("STOP"),
    ;

    private final String status;

    TaskStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
