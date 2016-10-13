package com.github.antego.bashmock;


import com.github.antego.bashmock.bodyproviders.MockedScriptBodyProvider;
import com.github.antego.bashmock.bodyproviders.PathBodyProvider;
import com.github.antego.bashmock.bodyproviders.StringBodyProvider;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

//TODO remember called parameters
//TODO add arg aware mocking (when arg then return)
//TODO create mocking script from string
//TODO classpath lookup
//TODO infer updated path into script
//TODO detect unmocked commands
//TODO mock bash builtins
//TODO mock command on absolute path
//TODO list env variables
//TODO list all commands
public class MockedScript {
    private Set<CommandMock> mocks = new HashSet<>();

    private MockedScriptBodyProvider bodyProvider;

    public MockedScript() {
    }

    public MockedScript(Path absolutePath) {
        bodyProvider = new PathBodyProvider(absolutePath);
    }

    public MockedScript(String scriptString) {
        bodyProvider = new StringBodyProvider(scriptString);
    }

    Set<CommandMock> getMocks() {
        return mocks;
    }

    public void addMock(CommandMock mock) {
        mocks.add(mock);
    }

    public MockedScriptBodyProvider getBodyProvider() {
        return bodyProvider;
    }
}
