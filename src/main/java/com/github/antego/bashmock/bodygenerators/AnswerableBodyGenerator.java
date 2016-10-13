package com.github.antego.bashmock.bodygenerators;


import java.util.Map;

public class AnswerableBodyGenerator {
    public String generate(int exitStatus, Map<String, Integer> statusesForArgs) {
        StringBuilder argsMatcherBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : statusesForArgs.entrySet()) {
            argsMatcherBuilder.append("if [ \"$*\" = \"" + entry.getKey() + "\" ]; then \n");
            argsMatcherBuilder.append("   exit " + entry.getValue() + "\n");
            argsMatcherBuilder.append("fi\n");
        }
        //language=Bash
        return "#!/bin/bash\n" +
                "echo \"$@\" >> \"${0%/*}/args\"\n" +
                argsMatcherBuilder.toString() +
                "exit " + exitStatus;
    }
}
