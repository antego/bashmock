package com.github.antego.bashmock.lexer;

import com.github.antego.bashmock.BashVersion;
import com.intellij.psi.tree.IElementType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BashLexerTest {
    BashLexer lexer = new BashLexer(BashVersion.Bash_v4);

    @Test
    public void shouldLexBashFileTest() {
        String script = "#!/bin/bash\n" +
                "cat /root/oolo";
        List<IElementType> elements = new ArrayList<>();

        lexer.start(script);
//        elements.add(lexer.getTokenType());
//        lexer.advance();
        System.out.println(lexer.getTokenText());
        System.out.println(lexer.getTokenSequence());
        System.out.println(lexer.getTokenType());


    }
}
