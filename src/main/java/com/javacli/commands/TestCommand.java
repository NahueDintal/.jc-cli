package com.javacli.commands;

import com.javacli.config.ProjectConfig;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestCommand {

  public static void execute() {
    try {
      ProjectConfig config = ProjectConfig.load(Paths.get(""));
      if (!compileTests(config)) {
        System.err.println("Falló la compilación de tests");
        return;
      }
      runTests(config);
    } catch (Exception e) {
      System.err.println("Error ejecutando tests: " + e.getMessage());
    }
  }

  private static boolean compileTests(ProjectConfig config) throws IOException, InterruptedException {
    List<Path> testFiles = new ArrayList<>();
    for (String testDir : config.getTestDirectories()) {
      Path testPath = Paths.get(testDir);
      if (Files.exists(testPath)) {
        Files.walk(testPath)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(testFiles::add);
      }
    }
    if (testFiles.isEmpty()) {
      System.err.println("No hay archivos de test en " + config.getTestDirectories());
      return false;
    }

    String testOutputDir = config.getTestOutputDirectory();
    Files.createDirectories(Paths.get(testOutputDir));

    String cp = config.getOutputDirectory() + File.pathSeparator + BuildCommand.buildClasspath(config);

    List<String> compileCommand = new ArrayList<>();
    compileCommand.add("javac");
    compileCommand.add("--release");
    compileCommand.add(String.valueOf(config.getJavaVersion()));
    compileCommand.add("-d");
    compileCommand.add(testOutputDir);
    compileCommand.add("-cp");
    compileCommand.add(cp);
    testFiles.forEach(f -> compileCommand.add(f.toString()));

    ProcessBuilder pb = new ProcessBuilder(compileCommand);
    pb.inheritIO();
    Process process = pb.start();
    return process.waitFor() == 0;
  }

  private static void runTests(ProjectConfig config) throws IOException, InterruptedException {
    String testFramework = config.getTestFramework();
    if (!"junit5".equals(testFramework)) {
      System.err.println("Framework de tests no soportado: " + testFramework);
      return;
    }

    String junitConsoleJar = findJUnitConsoleJar(config);
    if (junitConsoleJar == null) {
      System.err.println(
          "No se encontró JUnit Platform Console. Agregá el JAR y la ruta en jc.json (dependencies).");
      return;
    }

    String cp = config.getTestOutputDirectory() + File.pathSeparator
        + config.getOutputDirectory() + File.pathSeparator
        + BuildCommand.buildClasspath(config);

    List<String> cmd = List.of(
        "java", "-cp", cp,
        "org.junit.platform.console.ConsoleLauncher",
        "execute",
        "--scan-classpath",
        "--details=tree");

    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.inheritIO();
    Process process = pb.start();
    int exitCode = process.waitFor();
  }

  private static String findJUnitConsoleJar(ProjectConfig config) {
    for (String dep : config.getDependencies()) {
      if (dep.toLowerCase().contains("junit-platform-console")) {
        return dep;
      }
    }
    return null;
  }
}
