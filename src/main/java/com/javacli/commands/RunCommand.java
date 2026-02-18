package com.javacli.commands;

import com.javacli.config.ProjectConfig;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;

public class RunCommand {
  public static void execute() {
    try {
      if (!Files.exists(Paths.get("src"))) {
        System.err.println("No se encuentra el directorio 'src/'. Ejecuta 'jc new' primero.");
        return;
      }
      if (needsRecompile()) {
        if (!BuildCommand.compileProject()) {
          return;
        }
      }

      runProject();

    } catch (Exception e) {
      System.err.println("Error durante ejecución: " + e.getMessage());
    }
  }

  private static boolean needsRecompile() throws IOException {
    ProjectConfig config = ProjectConfig.load(Paths.get(""));
    String mainClass = config.getMainClass();
    String classFileName = mainClass.replace('.', '/') + ".class";
    Path mainClassPath = Paths.get("bin", classFileName);

    if (!Files.exists(mainClassPath)) {
      return true;
    }

    FileTime lastCompiled = Files.getLastModifiedTime(mainClassPath);

    for (String srcDir : config.getSourceDirectories()) {
      Path srcPath = Paths.get(srcDir);
      if (Files.exists(srcPath)) {
        boolean anyNewer = Files.walk(srcPath)
            .filter(p -> p.toString().endsWith(".java"))
            .anyMatch(javaFile -> {
              try {
                return Files.getLastModifiedTime(javaFile).compareTo(lastCompiled) > 0;
              } catch (IOException e) {
                return true;
              }
            });
        if (anyNewer) {
          return true;
        }
      }
    }
    return false;
  }

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
