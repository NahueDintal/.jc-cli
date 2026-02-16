package com.javacli.commands;

import java.nio.file.*;

public class RunJarCommand {
  public static void execute() {
    try {
      Path distDir = Paths.get("dist");
      if (!Files.exists(distDir)) {
        System.err.println("No existe el directorio 'dist/'. Ejecuta 'jc new jar' primero.");
        return;
      }

      Path jarFile = Files.list(distDir)
          .filter(p -> p.toString().endsWith(".jar"))
          .findFirst()
          .orElse(null);

      if (jarFile == null) {
        return;
      }

      System.out.println("Ejecutando: " + jarFile.getFileName());
      ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarFile.toString());
      pb.inheritIO();
      Process process = pb.start();
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        System.err.println("El JAR terminó con código: " + exitCode);
      }
    } catch (Exception e) {
      System.err.println("Error ejecutando JAR: " + e.getMessage());
    }
  }
}
