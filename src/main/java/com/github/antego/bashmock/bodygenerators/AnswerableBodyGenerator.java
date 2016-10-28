package com.github.antego.bashmock.bodygenerators;


import java.util.Map;

public class AnswerableBodyGenerator {
    public String generate(int exitStatus, Map<String, Integer> statusesForArgs) {
        StringBuilder argMatchersBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : statusesForArgs.entrySet()) {
            argMatchersBuilder.append("if [ \"$*\" = \"" + entry.getKey() + "\" ]; then \n");
            argMatchersBuilder.append("   exit " + entry.getValue() + "\n");
            argMatchersBuilder.append("fi\n");
        }
        //language=Bash
        return "#!/bin/bash\n" +
                "echo \"$@\" >> \"${0%/*}/args\"\n" +
                argMatchersBuilder.toString() +
                "exit " + exitStatus;
    }
}
