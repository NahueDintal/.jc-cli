package com.javacli.commands;

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
}
