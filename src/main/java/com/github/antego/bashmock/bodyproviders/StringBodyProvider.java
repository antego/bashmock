package com.github.antego.bashmock.bodyproviders;


public class StringBodyProvider implements MockedScriptBodyProvider {
    private String body;

    public StringBodyProvider(String body) {
        this.body = body;
    }

    @Override
    public String getBody() {
        return body;
    }
}
