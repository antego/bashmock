package com.github.antego.bashmock;


import com.github.antego.bashmock.bodygenerators.AnswerableBodyGenerator;
import com.github.antego.bashmock.bodyproviders.MockedScriptBodyProvider;
import com.github.antego.bashmock.bodyproviders.PathBodyProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleScriptExecutor implements ScriptExecutor {
    private static final String MOCK_SCRIPT_PREFIX = "mockScript";
    private static final String MOCKED_SCRIPT_PREFIX = "mockedScript";

    private AnswerableBodyGenerator bodyGenerator = new AnswerableBodyGenerator();


    @Override
    public ExecuteResult execute(MockedScript mockedScript, List<String> args) throws IOException, InterruptedException {
        // 1. Create temp dir
        Path tempDir = createTempDir();

        // 2. Load mocking script fixme mock source scripts
        String script = mockedScript.getBodyProvider().getBody();

        // 3. Generate stub scripts
        int i = 0;
        for (CommandMock mock : mockedScript.getMocks()) {
            String mockScriptName = MOCK_SCRIPT_PREFIX + i++;
            String mockScriptBody = bodyGenerator.generate(mock.getExitStatus(), mock.getStatusesForArgs());
            File mockScriptFile = Paths.get(tempDir.toString(), mockScriptName).toFile();
            try (PrintWriter writer = new PrintWriter(mockScriptFile)) {
                writer.write(mockScriptBody);
            }
            if (!mockScriptFile.setExecutable(true)) {
                throw new RuntimeException();
            }
            mock.setMockScriptFile(mockScriptFile);
            script = script.replace(mock.getCommand(), mockScriptName);
        }

        // 4. Replace
        String scriptName = MOCKED_SCRIPT_PREFIX;
        File scriptTempFile = Paths.get(tempDir.toString(), scriptName).toFile();
        try (PrintWriter writer = new PrintWriter(scriptTempFile)) {
            writer.write(script);
        }
        if (!scriptTempFile.setExecutable(true)) {
            throw new RuntimeException("Ololo");
        }

        // 5. Execute with set path
        List<String> argsInt = new ArrayList<>();
        argsInt.add(scriptTempFile.toString());
        if (args != null) {
            argsInt.addAll(args);
        }
        ProcessBuilder pb = new ProcessBuilder(argsInt);
        Map<String, String> env = pb.environment();
        env.put("PATH", tempDir.toString());
        pb.directory(tempDir.toFile());
        Process p = pb.start();
        int statusCode = p.waitFor();

        for (CommandMock mock : mockedScript.getMocks()) {
            mock.setCalledArgs(getArgs(mock.getMockScriptFile()));
        }
        String scriptOutput = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
//                System.out.println(inputLine);
                scriptOutput += inputLine;
            }
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
//                System.out.println(inputLine);
                scriptOutput += inputLine;
            }
        }
        return new ExecuteResult(statusCode, scriptOutput);
    }

    private Path createTempDir() throws IOException {
        Path tempDir = Files.createTempDirectory("bashMockExecutingDir");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "Bash Mock Temp Directory Remover")); // todo temp dir name conv
        return tempDir;
    }

    private List<String> getArgs(File file) throws IOException {
        List<String> args = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(file.getParent() + "/args")))) {
            //fixme args with new line characters
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
//                    System.out.println(inputLine);
                args.add(inputLine);
            }
        }
        return args;
    }
}
