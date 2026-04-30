package com.javacli.commands;

import com.javacli.config.ProjectConfig;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;

public class RunCommand {

  public static void execute() {
    try {
      ProjectConfig config = ProjectConfig.load(Paths.get(""));
      Path srcDir = Paths.get(config.getSourceDirectories().get(0));
      if (!Files.exists(srcDir)) {
        System.err.println("No se encuentra '" + srcDir + "'. Ejecuta 'jc new' primero.");
        return;
      }

      if (needsRecompile(config)) {
        if (!BuildCommand.compileProject(config)) {
          return;
        }
      }

      runProject(config);

    } catch (Exception e) {
      System.err.println("Error durante ejecución: " + e.getMessage());
    }
  }

  private static boolean needsRecompile(ProjectConfig config) throws IOException {
    String mainClass = config.getMainClass();
    String classFileName = mainClass.replace('.', '/') + ".class";
    Path mainClassPath = Paths.get(config.getOutputDirectory(), classFileName);

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
        if (anyNewer)
          return true;
      }
    }
    return false;
  }

  private static void runProject(ProjectConfig config) {
    try {
      String mainClass = config.getMainClass();
      String cp = config.getOutputDirectory();
      ProcessBuilder pb = new ProcessBuilder("java", "-cp", cp, mainClass);
      pb.inheritIO();
      Process process = pb.start();
      process.waitFor();
    } catch (Exception e) {
      System.err.println("Error durante ejecución: " + e.getMessage());
    }
  }
}
