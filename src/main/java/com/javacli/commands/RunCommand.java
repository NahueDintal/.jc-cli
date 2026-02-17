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

      System.out.println("Ejecutando proyecto...");
      runProject();

    } catch (Exception e) {
      System.err.println("Error durante ejecución: " + e.getMessage());
    }
  }

  private static boolean needsRecompile() throws IOException {
    ProjectConfig config = ProjectConfig.load(Paths.get(""));
    String mainClass = config.getMainClass();
    // Convertir el nombre de la clase a ruta de archivo (ej: "com.javacli.Main" ->
    // "com/javacli/Main.class")
    String classFileName = mainClass.replace('.', '/') + ".class";
    Path mainClassPath = Paths.get("bin", classFileName);

    // Si no existe el archivo .class principal, hay que recompilar
    if (!Files.exists(mainClassPath)) {
      return true;
    }

    FileTime lastCompiled = Files.getLastModifiedTime(mainClassPath);

    // Buscar todos los archivos .java en los directorios fuente
    // (config.sourceDirectories)
    // En lugar de asumir "src", usamos las rutas configuradas para mayor precisión.
    for (String srcDir : config.getSourceDirectories()) {
      Path srcPath = Paths.get(srcDir);
      if (Files.exists(srcPath)) {
        boolean anyNewer = Files.walk(srcPath)
            .filter(p -> p.toString().endsWith(".java"))
            .anyMatch(javaFile -> {
              try {
                return Files.getLastModifiedTime(javaFile).compareTo(lastCompiled) > 0;
              } catch (IOException e) {
                return true; // si hay error, asumimos que cambió
              }
            });
        if (anyNewer) {
          return true;
        }
      }
    }

    // Si llegamos acá, ningún fuente es más nuevo que la última compilación
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
