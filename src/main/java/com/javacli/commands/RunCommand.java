package com.javacli.commands;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

public class RunCommand {
  public static void execute() {
    try {
      if (!Files.exists(Paths.get("src"))) {
        System.err.println("❌ No se encuentra el directorio 'src/'. Ejecuta 'jc new' primero.");
        return;
      }

      // Verificar si necesitamos recompilar (tu lógica actual)
      if (needsRecompile()) {
        System.out.println("🔨 Cambios detectados, recompilando...");
        if (!BuildCommand.compileProject()) {
          return;
        }
      } else {
        System.out.println("Usando compilación existente...");
      }

      System.out.println("Ejecutando proyecto...");
      runProject();

    } catch (Exception e) {
      System.err.println("Error durante ejecución: " + e.getMessage());
    }
  }

  // Mover el método compileProject a BuildCommand
  // Y mantener solo needsRecompile y runProject aquí
  private static boolean needsRecompile() throws IOException {
    // Tu lógica actual de detección de cambios
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

  private static void runProject() {
    try {
      ProcessBuilder pb = new ProcessBuilder("java", "-cp", "bin", "Main");
      pb.inheritIO();
      Process process = pb.start();
      process.waitFor();
    } catch (Exception e) {
      System.err.println("Error durante ejecución: " + e.getMessage());
    }
  }
}
