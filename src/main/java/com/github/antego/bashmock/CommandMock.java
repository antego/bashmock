package com.github.antego.bashmock;


public class CommandMock {
    private String command;

    private int exitStatus;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(int exitStatus) {
        if (exitStatus < 0 || exitStatus > 255) {
            throw new IllegalArgumentException("Must be between 0 and 255");
        }
        this.exitStatus = exitStatus;
    }
}
