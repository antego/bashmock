package com.github.antego.bashmock.bodygenerators;


public class DumpArgsBodyGenerator extends ExitStatusAwareBodyGenerator {
    @Override
    public String generate(int exitStatus) {
        //language=Bash
        return "#!/bin/bash\n" +
                "echo \"$@\" >> \"${0%/*}/args\"\n" +
                "exit " + exitStatus;
    }
}
