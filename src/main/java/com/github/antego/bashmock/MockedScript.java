package com.github.antego.bashmock;


import java.io.BufferedReader;
import java.io.File;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockedScript {
    private Set<CommandMock> mocks = new HashSet<>();

    private String scriptPath;

    private static final String MOCK_SCRIPT_PREFIX = "mockScript";

    public void addMock(CommandMock mock) {
        mocks.add(mock);
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath; // TODO from resources
    }

    public ExecuteResult execute(List<String> args) throws IOException, InterruptedException {
        // 1. Create temp dir
        Path tempDir = createTempDir();

        // 2. Load mocking script fixme mock source scripts
        byte[] byteContent = Files.readAllBytes(Paths.get(scriptPath));
        String script = new String(byteContent, Charset.defaultCharset());

        // 3. Generate stub scripts
        int i = 0;
        for (CommandMock mock : mocks) {
            String mockScriptName = MOCK_SCRIPT_PREFIX + i++;
            String mockScriptBody = generateBody(mock.getExitStatus());
            File mockScriptFile = Paths.get(tempDir.toString(), mockScriptName).toFile();
            try (PrintWriter writer = new PrintWriter(mockScriptFile)) {
                writer.write(mockScriptBody);
            }
            if (!mockScriptFile.setExecutable(true)) {
                throw new RuntimeException();
            }
            script = script.replace(mock.getCommand(), mockScriptName);
        }

        // 4. Replace
        String scriptName = Paths.get(scriptPath).getFileName().toString();
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
        String scriptOutput = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))){
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                System.out.println(inputLine);
                scriptOutput += inputLine;
            }
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()))){
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                System.out.println(inputLine);
                scriptOutput += inputLine;
            }
        }
        return new ExecuteResult(statusCode, scriptOutput);
    }

    private String generateBody(int exitStatus) {
        //language=Bash
        return "#!/bin/bash\n" +
                "\n" +
                "exit " + exitStatus;
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
}
