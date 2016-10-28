package com.github.antego.bashmock.lexer;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;


public class SimpleLexerTest {
    @Test
    public void testLexer() throws IOException {
        SimpleLexer lexer = new SimpleLexer(new FileReader("/home/anton/projects/bashmock/src/test/resources/properties.simple"));
        Integer res = lexer.yylex();
        int i = 0;
        while (res != null && i != 100) {
            System.out.println(res + ": " + lexer.yylength() + " : " + lexer.yytext());

            res = lexer.yylex();
            i++;
//            lexer.
        }
    }
}