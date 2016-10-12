package com.github.antego.bashmock;


public class ExecuteResult {
    private int exitStatus;

    private String output;

    public ExecuteResult(int exitStatus, String output) {
        this.exitStatus = exitStatus;
        this.output = output;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public String getOutput() {
        return output;
    }
}
