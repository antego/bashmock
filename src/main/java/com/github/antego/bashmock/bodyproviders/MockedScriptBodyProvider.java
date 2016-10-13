package com.github.antego.bashmock.bodyproviders;


import java.io.IOException;

public interface MockedScriptBodyProvider {
    String getBody() throws IOException;
}
