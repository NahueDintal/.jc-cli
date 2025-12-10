package com.javacli.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NewCommand {
  public static void execute() {
    try {
      String projectName = getProjectName();
      createProjectStructure(projectName);
      createMainJavaFile(projectName);
      System.out.println("Proyecto Java 25 creado exitosamente!");
      System.out.println("Usa: 'jc run' para compilar y ejecutar");
    } catch (Exception e) {
      System.err.println("Error al crear proyecto: " + e.getMessage());
    }
  }

  private static String getProjectName() {
    Path currentPath = Paths.get("").toAbsolutePath();
    return currentPath.getFileName().toString();
  }

  private static void createProjectStructure(String projectName) throws IOException {
    Files.createDirectories(Paths.get("src/main/java"));
    // Files.createDirectories(Paths.get("src/test/java"));
    Files.createDirectories(Paths.get("lib"));
  }

  private static void createMainJavaFile(String projectName) throws IOException {
    String mainJavaContent = """
        public class Main {
            public static void main(String[] args) {
                System.out.println("¡Hola Wachin! ¡Desde el directorio '%s' con Java 25!");
            }
        }
        """.formatted(projectName, projectName);

    Files.writeString(Paths.get("src/main/java/Main.java"), mainJavaContent);
  }
}
