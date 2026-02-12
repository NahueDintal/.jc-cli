package com.javacli.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NewCommand {

  // Versión original: usa el directorio de trabajo actual
  public static void execute() {
    execute(Paths.get(System.getProperty("user.dir")));
  }

  // Nueva versión: permite especificar el directorio base (ideal para tests)
  public static void execute(Path baseDir) {
    try {
      String projectName = baseDir.getFileName().toString();
      createProjectStructure(baseDir);
      createMainJavaFile(baseDir, projectName);
      System.out.println("✅ Proyecto Java 25 creado exitosamente en: " + baseDir);
      System.out.println("   Usa: 'jc run' para compilar y ejecutar");
    } catch (Exception e) {
      System.err.println("❌ Error al crear proyecto: " + e.getMessage());
    }
  }

  private static void createProjectStructure(Path baseDir) throws IOException {
    Files.createDirectories(baseDir.resolve("src/main/java"));
    Files.createDirectories(baseDir.resolve("src/test/java")); // ← ahora se crea siempre
    Files.createDirectories(baseDir.resolve("lib"));
  }

  private static void createMainJavaFile(Path baseDir, String projectName) throws IOException {
    String mainJavaContent = """
        public class Main {
            public static void main(String[] args) {
                System.out.println("¡Hola Wachin! ¡Desde el directorio '%s' con Java 25!");
            }
        }
        """.formatted(projectName);
    Files.writeString(baseDir.resolve("src/main/java/Main.java"), mainJavaContent);
  }
}
