package com.javacli.commands;

import com.javacli.config.ProjectConfig;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BuildCommand {

  public static void execute() {
    try {
      ProjectConfig config = ProjectConfig.load(Paths.get(""));
      Path srcDir = Paths.get(config.getSourceDirectories().get(0)); // tomamos el primer source
      if (!Files.exists(srcDir)) {
        System.err.println("No se encuentra '" + srcDir + "'. Ejecutá 'jc new' primero.");
        return;
      }

      if (compileProject(config)) {
        System.out.println("Compilación exitosa.");
      } else {
        System.err.println("Error en la compilación");
      }
    } catch (Exception e) {
      System.err.println("Error durante build: " + e.getMessage());
    }
  }

  // Usa el config cargado (ya se pasa desde execute o se puede cargar de nuevo)
  public static boolean compileProject(ProjectConfig config) {
    try {
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
      if (javaFiles.isEmpty()) {
        System.err.println("No se encontraron archivos .java en " + config.getSourceDirectories());
        return false;
      }

      String outputDir = config.getOutputDirectory();
      Files.createDirectories(Paths.get(outputDir));

      List<String> compileCommand = new ArrayList<>();
      compileCommand.add("javac");
      compileCommand.add("--release");
      compileCommand.add(String.valueOf(config.getJavaVersion()));
      compileCommand.add("-d");
      compileCommand.add(outputDir);

      String cp = buildClasspath(config);
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

  // Compilación de tests (llamado también desde TestCommand)
  public static boolean compileTestProject(ProjectConfig config) {
    try {
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

      // Classpath: salida de producción + libs (y JUnit si está)
      String cp = config.getOutputDirectory() + File.pathSeparator + buildClasspath(config);

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

    } catch (Exception e) {
      System.err.println("Error compilando tests: " + e.getMessage());
      return false;
    }
  }

  // Classpath que incluye el directorio de salida de producción y las
  // dependencias
  public static String buildClasspath(ProjectConfig config) {
    List<String> cpEntries = new ArrayList<>();

    // directorio de compilados principal
    String outputDir = config.getOutputDirectory();
    if (Files.exists(Paths.get(outputDir))) {
      cpEntries.add(outputDir);
    }

    // dependencias (JARs de lib/ y las listadas en jc.json)
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

  // Compila el proyecto actual (carga automáticamente el config)
  public static boolean compileProject() {
    try {
      ProjectConfig config = ProjectConfig.load(Paths.get(""));
      return compileProject(config);
    } catch (IOException e) {
      System.err.println("Error cargando configuración: " + e.getMessage());
      return false;
    }
  }

  // Compila los tests del proyecto actual (carga automáticamente el config)
  public static boolean compileTestProject() {
    try {
      ProjectConfig config = ProjectConfig.load(Paths.get(""));
      return compileTestProject(config);
    } catch (IOException e) {
      System.err.println("Error cargando configuración: " + e.getMessage());
      return false;
    }
  }

  // Classpath solo con los JARs de lib/ (usado en compilación de tests si no se
  // tiene config)
  public static String buildClasspath() {
    Path libDir = Paths.get("lib");
    if (!Files.exists(libDir))
      return "";
    try (var stream = Files.walk(libDir)) {
      return stream
          .filter(p -> p.toString().endsWith(".jar"))
          .map(Path::toString)
          .collect(java.util.stream.Collectors.joining(File.pathSeparator));
    } catch (IOException e) {
      return "";
    }
  }
}
