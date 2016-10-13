package com.github.antego.bashmock.bodygenerators;



public class ExitStatusAwareBodyGenerator implements BodyGenerator {
    @Override
    public String generate(int exitStatus) {
        //language=Bash
        return "#!/bin/bash\n" +
                "\n" +
                "exit " + exitStatus;
    }
}
