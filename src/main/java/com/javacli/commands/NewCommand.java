package com.javacli.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NewCommand {

  private static final int DEFAULT_JAVA_VERSION = 25;

  public static void execute() {
    execute(Paths.get(System.getProperty("user.dir")));
  }

  public static void execute(Path baseDir) {
    try {
      String projectName = baseDir.getFileName().toString();
      createProjectStructure(baseDir);
      createMainJavaFile(baseDir, projectName);
      System.out.println("Proyecto Java 25 creado exitosamente en: " + baseDir);
      System.out.println("   Usa: 'jc run' para compilar y ejecutar");
    } catch (Exception e) {
      System.err.println("Error al crear proyecto: " + e.getMessage());
    }
  }

  private static void createProjectStructure(Path baseDir) throws IOException {
    Files.createDirectories(baseDir.resolve("src/main/java"));
    Files.createDirectories(baseDir.resolve("src/test/java"));
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
    generateJcJson(baseDir, projectName);
    generateClasspath(baseDir, projectName);
    generateProject(baseDir, projectName);
  }

  private static void generateJcJson(Path baseDir, String projectName, int javaVersion) throws IOException {
    String jsonContent = """
        {
          "name": "%s",
          "version": "1.0.0",
          "mainClass": "Main",
          "sourceDirectories": ["src/main/java"],
          "testDirectories": ["src/test/java"],
          "dependencies": [],
          "javaVersion": %d
        }
        """.formatted(projectName, javaVersion);
    Files.writeString(baseDir.resolve("jc.json"), jsonContent);
  }

  private static void generateClasspath(Path baseDir, String projectName) throws IOException {
    // Por ahora, como no hay dependencias, generamos un .classpath básico
    String classpathContent = """
        <?xml version="1.0" encoding="UTF-8"?>
        <classpath>
            <classpathentry kind="src" path="src/main/java"/>
            <classpathentry kind="src" path="src/test/java"/>
            <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
            <classpathentry kind="output" path="bin"/>
        </classpath>
        """;
    Files.writeString(baseDir.resolve(".classpath"), classpathContent);
  }

  private static void generateProject(Path baseDir, String projectName) throws IOException {
    String projectContent = """
        <?xml version="1.0" encoding="UTF-8"?>
        <projectDescription>
            <name>%s</name>
            <comment></comment>
            <projects></projects>
            <buildSpec>
                <buildCommand>
                    <name>org.eclipse.jdt.core.javabuilder</name>
                </buildCommand>
            </buildSpec>
            <natures>
                <nature>org.eclipse.jdt.core.javanature</nature>
            </natures>
        </projectDescription>
        """.formatted(projectName);
    Files.writeString(baseDir.resolve(".project"), projectContent);
  }

  public static void execute(int javaVersion) {
    execute(Paths.get(System.getProperty("user.dir")), javaVersion);
  }

  private static void execute(Path baseDir, int javaVersion) {
    try {
      String projectName = baseDir.getFileName().toString();
      createProjectStructure(baseDir);
      createMainJavaFile(baseDir, projectName, javaVersion);
      System.out.println("Proyecto Java " + javaVersion + " creado exitosamente en: " + baseDir);
      System.out.println("   Usa: 'jc run' para compilar y ejecutar");
    } catch (Exception e) {
      System.err.println("Error al crear proyecto: " + e.getMessage());
    }
  }
}
