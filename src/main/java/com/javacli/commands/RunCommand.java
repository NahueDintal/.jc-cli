package com.javacli.commands;

import com.javacli.config.ProjectConfig;
import java.nio.file.*;

public class RunCommand {
  public static void execute() {
    try {
      if (!Files.exists(Paths.get("src"))) {
        System.err.println("No se encuentra el directorio 'src/'. Ejecuta 'jc new' primero.");
        return;
      }

      System.out.println("Ejecutando proyecto...");
      runProject();

    } catch (Exception e) {
      System.err.println("Error durante ejecución: " + e.getMessage());
    }
  }

  // private static boolean needsRecompile() throws IOException {
  // File binDir = new File("bin");
  // if (!binDir.exists() || !new File("bin/Main.class").exists()) {
  // return true;
  // }
  //
  // FileTime lastCompiled =
  // Files.getLastModifiedTime(Paths.get("bin/Main.class"));
  // return Files.walk(Paths.get("src"))
  // .filter(path -> path.toString().endsWith(".java"))
  // .anyMatch(javaFile -> {
  // try {
  // return Files.getLastModifiedTime(javaFile).compareTo(lastCompiled) > 0;
  // } catch (IOException e) {
  // return true;
  // }
  // });
  // }

  private static void runProject() {
    try {
      ProjectConfig config = ProjectConfig.load(Paths.get(""));
      String mainClass = config.getMainClass();
      ProcessBuilder pb = new ProcessBuilder("java", "-cp", "bin", mainClass);
      pb.inheritIO();
      Process process = pb.start();
      process.waitFor();
    } catch (Exception e) {
      System.err.println("Error durante ejecución: " + e.getMessage());
    }
  }
}
