package com.github.antego.bashmock.bodyproviders;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathBodyProvider implements MockedScriptBodyProvider {
    private Path scriptPath;

    public PathBodyProvider(Path scriptPath) {
        this.scriptPath = scriptPath;
    }

    @Override
    public String getBody() throws IOException {
        byte[] byteContent = Files.readAllBytes(scriptPath);
        return new String(byteContent, Charset.defaultCharset());
    }
}
