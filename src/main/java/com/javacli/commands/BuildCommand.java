package com.javacli.commands;

import java.util.stream.Collectors;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class BuildCommand {
  public static void execute() {
    try {
      if (!Files.exists(Paths.get("src"))) {
        System.err.println("No se encuentra el directorio 'src/'. Ejecuta 'jc new' primero.");
        return;
      }

      System.out.println("Compilando proyecto...");

      if (compileProject()) {
        System.out.println("Compilación exitosa! Archivos en: bin/");
      } else {
        System.err.println("Error en la compilación");
      }

    } catch (Exception e) {
      System.err.println("Error durante build: " + e.getMessage());
    }
  }

  public static boolean compileProject() {
    try {
      List<Path> javaFiles = Files.walk(Paths.get("src"))
          .filter(path -> path.toString().endsWith(".java"))
          .toList();

      if (javaFiles.isEmpty()) {
        System.err.println("No se encontraron archivos .java en 'src/'");
        return false;
      }

      // Crear directorio bin si no existe
      Files.createDirectories(Paths.get("bin"));

      List<String> compileCommand = new ArrayList<>();
      compileCommand.add("javac");
      compileCommand.add("--release");
      compileCommand.add("25");
      compileCommand.add("-d");
      compileCommand.add("bin");

      // Agregar todos los archivos .java
      for (Path javaFile : javaFiles) {
        compileCommand.add(javaFile.toString());
      }

      // Ejecutar compilación
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
      // Buscar archivos .java en src/test/java
      Path testSrc = Paths.get("src/test/java");
      if (!Files.exists(testSrc)) {
        System.err.println("No existe el directorio src/test/java");
        return false;
      }

      List<Path> testFiles = Files.walk(testSrc)
          .filter(p -> p.toString().endsWith(".java"))
          .toList();

      if (testFiles.isEmpty()) {
        System.err.println("No hay archivos de test");
        return false;
      }

      // Crear directorio bin-test
      Files.createDirectories(Paths.get("bin-test"));

      // Classpath: bin/ (producción) + todos los JARs de lib/
      String cp = "bin" + File.pathSeparator + buildClasspath();

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
