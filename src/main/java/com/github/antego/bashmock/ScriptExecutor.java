package com.github.antego.bashmock;


import java.io.IOException;
import java.util.List;

public interface ScriptExecutor {
    public ExecuteResult execute(MockedScript mockedScript, List<String> args) throws IOException, InterruptedException;
}
