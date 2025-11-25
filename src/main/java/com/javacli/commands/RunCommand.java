package com.javacli.commands;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

public class RunCommand {
  public static void execute() {
    try {
      String projectName = getProjectName();
      System.out.println("🏗️  Building and running " + projectName + "...");

      if (!Files.exists(Paths.get("src"))) {
        System.err.println("❌ No se encuentra el directorio 'src/'. Ejecuta 'jc new' primero.");
        return;
      }

      // Verificar si necesita recompilar
      if (needsRecompile()) {
        if (!compileProject()) {
          return;
        }
      } else {
        System.out.println("✅ Usando clases compiladas existentes");
      }

      runProject();

    } catch (Exception e) {
      System.err.println("❌ Error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static String getProjectName() {
    Path currentPath = Paths.get("").toAbsolutePath();
    return currentPath.getFileName().toString();
  }

  private static boolean needsRecompile() throws IOException {
    File binDir = new File("bin");
    if (!binDir.exists() || !new File("bin/Main.class").exists()) {
      return true;
    }

    FileTime lastCompiled = Files.getLastModifiedTime(Paths.get("bin/Main.class"));
    return Files.walk(Paths.get("src"))
        .filter(path -> path.toString().endsWith(".java"))
        .anyMatch(javaFile -> {
          try {
            return Files.getLastModifiedTime(javaFile).compareTo(lastCompiled) > 0;
          } catch (IOException e) {
            return true;
          }
        });
  }

  private static boolean compileProject() {
    try {
      List<Path> javaFiles = Files.walk(Paths.get("src"))
          .filter(path -> path.toString().endsWith(".java"))
          .toList();

      if (javaFiles.isEmpty()) {
        System.err.println("❌ No se encontraron archivos .java en 'src/'");
        return false;
      }

      System.out.println("🔨 Compilando con Java 25...");
      Files.createDirectories(Paths.get("bin"));

      List<String> compileCommand = new ArrayList<>();
      compileCommand.add("javac");
      compileCommand.add("--release");
      compileCommand.add("25");
      compileCommand.add("-d");
      compileCommand.add("bin");

      for (Path javaFile : javaFiles) {
        compileCommand.add(javaFile.toString());
      }

      ProcessBuilder pb = new ProcessBuilder(compileCommand);
      pb.inheritIO();
      Process process = pb.start();

      int exitCode = process.waitFor();
      if (exitCode == 0) {
        System.out.println("✅ Compilación exitosa");
        return true;
      } else {
        System.err.println("❌ Error en compilación");
        return false;
      }

    } catch (Exception e) {
      System.err.println("❌ Error durante compilación: " + e.getMessage());
      return false;
    }
  }

  private static void runProject() {
    try {
      System.out.println("🚀 Ejecutando...");
      ProcessBuilder pb = new ProcessBuilder("java", "-cp", "bin", "Main");
      pb.inheritIO();
      Process process = pb.start();
      process.waitFor();
    } catch (Exception e) {
      System.err.println("❌ Error durante ejecución: " + e.getMessage());
    }
  }
}
