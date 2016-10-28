package com.github.antego.bashmock;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MockedScriptTest {
    @Test
    public void testExecute() throws IOException, InterruptedException {
        MockedScript script = new MockedScript("/home/anton/projects/bashmock/src/test/resources/testScript1");

        CommandMock mock = CommandMock.of("command1").withExitStatus(0).build();
        script.addMock(mock);

        CommandMock catMock = CommandMock.of("cat").withExitStatus(0).build();
        catMock.when("olo").then(0);
        catMock.when("ala olo").then(1);
        script.addMock(catMock);

        ExecuteResult result = new SimpleScriptExecutor().execute(script, null);

        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().contains("testScript1"));
        assertEquals(1, result.getExitStatus());
//        assertEquals(2, mock.getCalledArgs().size());
        mock.getCalledArgs().forEach(System.out::println);
    }

}
