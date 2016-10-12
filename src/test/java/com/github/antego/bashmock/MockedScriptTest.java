package com.github.antego.bashmock;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MockedScriptTest {
    @Test
    public void testExecute() throws IOException, InterruptedException {
        MockedScript script = new MockedScript();
        script.setScriptPath("/home/anton/projects/bashmock/src/test/resources/testScript1");

        CommandMock mock = new CommandMock();
        mock.setCommand("command1");
        mock.setExitStatus(0);
        script.addMock(mock);

        CommandMock catMock = new CommandMock();
        catMock.setCommand("cat");
        catMock.setExitStatus(1);
        //script.addMock(catMock);

        ExecuteResult result = script.execute(null);
        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().contains("testScript1"));
        assertEquals(0, result.getExitStatus());
    }

}
