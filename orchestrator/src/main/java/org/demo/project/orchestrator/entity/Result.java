package org.demo.project.orchestrator.entity;

public enum Result {
    SUCCESS(0, "success"),
    FAILURE(1, "failure");

    private int returnCode;
    private String description;

    Result(int returnCode, String description) {
        this.returnCode = returnCode;
        this.description = description;
    }
}
