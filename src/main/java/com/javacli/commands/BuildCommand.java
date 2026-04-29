package com.javacli.commands;

import com.javacli.config.ProjectConfig;
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
      ProjectConfig config = ProjectConfig.load(Paths.get(""));
      List<Path> javaFiles = new ArrayList<>();
      for (String srcDir : config.getSourceDirectories()) {
        Path srcPath = Paths.get(srcDir);
        if (Files.exists(srcPath)) {
          Files.walk(srcPath)
              .filter(p -> p.toString().endsWith(".java"))
              .forEach(javaFiles::add);
        }
      }

      System.out.println("Construyendo proyecto...");
      // System.out.println("Archivos Java encontrados: " + javaFiles.size());
      if (javaFiles.isEmpty()) {
        System.err.println("No se encontraron archivos .java en " + config.getSourceDirectories());
        return false;
      }

      Files.createDirectories(Paths.get("bin"));

      List<String> compileCommand = new ArrayList<>();
      compileCommand.add("javac");
      compileCommand.add("--release");
      compileCommand.add(String.valueOf(config.getJavaVersion())); // antes era "25"
      compileCommand.add("-d");
      compileCommand.add("bin");

      String cp = BuildCommand.buildClasspath(config);
      if (!cp.isEmpty()) {
        compileCommand.add("-cp");
        compileCommand.add(cp);
      }

      for (Path javaFile : javaFiles) {
        compileCommand.add(javaFile.toString());
      }

      // System.out.println("Ejecutando: " + String.join(" ", compileCommand));
      ProcessBuilder pb = new ProcessBuilder(compileCommand);
      pb.inheritIO();
      Process process = pb.start();
      int exitCode = process.waitFor();
      // System.out.println("Código de salida de javac: " + exitCode);
      return exitCode == 0;

    } catch (Exception e) {
      System.err.println("Error durante compilación: " + e.getMessage());
      return false;
    }
  }

  public static String buildClasspath(ProjectConfig config) {
    List<String> cpEntries = new ArrayList<>();
    if (Files.exists(Paths.get("bin"))) {
      cpEntries.add("bin");
    }

    for (String dep : config.getDependencies()) {
      Path depPath = Paths.get(dep);
      if (Files.exists(depPath)) {
        cpEntries.add(depPath.toString());
      } else {
        System.err.println("Advertencia: dependencia no encontrada: " + dep);
      }
    }

    return String.join(File.pathSeparator, cpEntries);
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

      Files.createDirectories(Paths.get(config.getOutputDirectory()))

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
