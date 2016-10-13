package com.github.antego.bashmock;


import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandMock {
    private String command;

    private int exitStatus;

    private File mockScriptFile;

    private List<String> calledArgs;

    private Map<String, Integer> statusesForArgs = new HashMap<>();

    private CommandMock(String command) {
        this.command = command;
    }

    public static CommandMockBuilder of(String command) {
        return new CommandMockBuilder(command);
    }

    public Map<String, Integer> getStatusesForArgs() {
        return statusesForArgs;
    }

    public List<String> getCalledArgs() {
        return calledArgs;
    }

    public void setCalledArgs(List<String> calledArgs) {
        this.calledArgs = calledArgs;
    }

    public File getMockScriptFile() {
        return mockScriptFile;
    }

    public void setMockScriptFile(File mockScriptFile) {
        this.mockScriptFile = mockScriptFile;
    }

    public String getCommand() {
        return command;
    }

    public WhenCondition when(String arg) {
        return new WhenCondition(arg);
    }

    public int getExitStatus() {
        return exitStatus;
    }

    private void setExitStatus(int exitStatus) {
        if (exitStatus < 0 || exitStatus > 255) {
            throw new IllegalArgumentException("Must be between 0 and 255");
        }
        this.exitStatus = exitStatus;
    }

    public static class CommandMockBuilder {
        private String command;

        private int exitStatus;

        private CommandMockBuilder(String command) {
            this.command = command;
        }

        public CommandMockBuilder withExitStatus(int exitStatus) {
            this.exitStatus = exitStatus;
            return this;
        }

        public CommandMock build() {
            CommandMock mock = new CommandMock(command);
            mock.setExitStatus(exitStatus);
            return mock;
        }
    }

    public class WhenCondition {
        private String args;

        private int exitStatus;

        private WhenCondition(String args) {
            this.args = args;
        }

        public void then(int exitStatus) {
            statusesForArgs.put(args, exitStatus);
        }
    }
}
