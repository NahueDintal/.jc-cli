package com.javacli.commands;

import java.util.stream.Collectors;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class BuildCommand {

  public static void execute() {
    try {
      if (!Files.exists(Paths.get("src/main/java"))) {
        System.err.println("No se encuentra 'src/main/java'. Ejecutá 'jc new' primero.");
        return;
      }

      if (compileProject()) {
      } else {
        System.err.println("Error en la compilación");
      }

    } catch (Exception e) {
      System.err.println("Error durante build: " + e.getMessage());
    }
  }

  public static boolean compileProject() {
    try {
      Path mainSrc = Paths.get("src/main/java");
      if (!Files.exists(mainSrc)) {
        System.err.println("No existe src/main/java");
        return false;
      }

      List<Path> javaFiles = Files.walk(mainSrc)
          .filter(path -> path.toString().endsWith(".java"))
          .toList();

      if (javaFiles.isEmpty()) {
        System.err.println("No se encontraron archivos .java en src/main/java");
        return false;
      }

      Files.createDirectories(Paths.get("bin"));

      List<String> compileCommand = new ArrayList<>();
      compileCommand.add("javac");
      compileCommand.add("--release");
      compileCommand.add("25");
      compileCommand.add("-d");
      compileCommand.add("bin");

      String cp = buildClasspath();
      if (!cp.isEmpty()) {
        compileCommand.add("-cp");
        compileCommand.add(cp);
      }

      for (Path javaFile : javaFiles) {
        compileCommand.add(javaFile.toString());
      }

      ProcessBuilder pb = new ProcessBuilder(compileCommand);
      pb.inheritIO();
      Process process = pb.start();
      int exitCode = process.waitFor();
      return exitCode == 0;

    } catch (Exception e) {
      System.err.println("Error durante compilación: " + e.getMessage());
      return false;
    }
  }

  public static boolean compileTestProject() {
    try {
      Path testSrc = Paths.get("src/test/java");
      if (!Files.exists(testSrc)) {
        System.err.println("No existe src/test/java");
        return false;
      }

      List<Path> testFiles = Files.walk(testSrc)
          .filter(p -> p.toString().endsWith(".java"))
          .toList();

      if (testFiles.isEmpty()) {
        System.err.println("No hay archivos de test en src/test/java");
        return false;
      }

      Files.createDirectories(Paths.get("bin-test"));

      String cp = "bin" + File.pathSeparator + buildClasspath();
      if (cp.trim().isEmpty()) {
        System.err.println("Classpath vacío. ¿Hay JARs en lib/?");
        return false;
      }

      List<String> compileCommand = new ArrayList<>();
      compileCommand.add("javac");
      compileCommand.add("--release");
      compileCommand.add("25");
      compileCommand.add("-d");
      compileCommand.add("bin-test");
      compileCommand.add("-cp");
      compileCommand.add(cp);
      testFiles.forEach(f -> compileCommand.add(f.toString()));

      ProcessBuilder pb = new ProcessBuilder(compileCommand);
      pb.inheritIO();
      Process process = pb.start();
      return process.waitFor() == 0;

    } catch (Exception e) {
      System.err.println("Error compilando tests: " + e.getMessage());
      return false;
    }
  }

  public static String buildClasspath() {
    Path libDir = Paths.get("lib");
    if (!Files.exists(libDir))
      return "";
    try {
      return Files.walk(libDir)
          .filter(p -> p.toString().endsWith(".jar"))
          .map(Path::toString)
          .collect(Collectors.joining(File.pathSeparator));
    } catch (IOException e) {
      return "";
    }
  }
}
