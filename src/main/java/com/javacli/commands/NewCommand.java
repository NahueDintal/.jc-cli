package com.javacli.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NewCommand {

  private static final int DEFAULT_JAVA_VERSION = 25;

  // Sin argumentos → versión por defecto
  public static void execute() {
    execute(DEFAULT_JAVA_VERSION);
  }

  // Con versión explícita
  public static void execute(int javaVersion) {
    execute(Paths.get(System.getProperty("user.dir")), javaVersion);
  }

  // Lógica real
  private static void execute(Path baseDir, int javaVersion) {
    try {
      String projectName = baseDir.getFileName().toString();
      createProjectStructure(baseDir, projectName);
      createMainJavaFile(baseDir, projectName, javaVersion);
      System.out.println("Proyecto Java " + javaVersion + " (estilo IntelliJ) creado en: " + baseDir);
      System.out.println("   Abrí la carpeta con IntelliJ o usá 'jc run'");
    } catch (Exception e) {
      System.err.println("Error al crear proyecto: " + e.getMessage());
    }
  }

  private static void createProjectStructure(Path baseDir, String projectName) throws IOException {
    Files.createDirectories(baseDir.resolve("src")); // fuente directo
    Files.createDirectories(baseDir.resolve("lib")); // dependencias
    // out/production/<projectName> se crea al compilar
  }

  private static void createMainJavaFile(Path baseDir, String projectName, int javaVersion) throws IOException {
    String mainJavaContent = """
        public class Main {
            public static void main(String[] args) {
                System.out.println("¡Hola Wachin! Desde '%s' con Java %d!");
            }
        }
        """.formatted(projectName, javaVersion);
    Files.writeString(baseDir.resolve("src/Main.java"), mainJavaContent);

    generateJcJson(baseDir, projectName, javaVersion);
    generateIntelliJFiles(baseDir, projectName, javaVersion);
    generateGitignore(baseDir);
  }

  private static void generateJcJson(Path baseDir, String projectName, int javaVersion) throws IOException {
    String outputDir = "out/production/" + projectName;
    String jsonContent = """
        {
          "name": "%s",
          "version": "1.0.0",
          "mainClass": "Main",
          "sourceDirectories": ["src"],
          "testDirectories": [],
          "dependencies": [],
          "outputDirectory": "%s",
          "javaVersion": %d
        }
        """.formatted(projectName, outputDir, javaVersion);
    Files.writeString(baseDir.resolve("jc.json"), jsonContent);
  }

  private static void generateIntelliJFiles(Path baseDir, String projectName, int javaVersion) throws IOException {
    Path ideaDir = baseDir.resolve(".idea");
    Files.createDirectories(ideaDir);

    // modules.xml
    String modulesXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <project version="4">
          <component name="ProjectModuleManager">
            <modules>
              <module fileurl="file://$PROJECT_DIR$/%s.iml" filepath="$PROJECT_DIR$/%s.iml" />
            </modules>
          </component>
        </project>
        """.formatted(projectName, projectName);
    Files.writeString(ideaDir.resolve("modules.xml"), modulesXml);

    // .iml
    String imlContent = """
        <?xml version="1.0" encoding="UTF-8"?>
        <module type="JAVA_MODULE" version="4">
          <component name="NewModuleRootManager" inherit-compiler-output="false">
            <output url="file://$MODULE_DIR$/out/production/%s" />
            <output-test url="file://$MODULE_DIR$/out/test/%s" />
            <exclude-output />
            <content url="file://$MODULE_DIR$">
              <sourceFolder url="file://$MODULE_DIR$/src" isTestSource="false" />
            </content>
            <orderEntry type="inheritedJdk" />
            <orderEntry type="sourceFolder" forTests="false" />
          </component>
        </module>
        """.formatted(projectName, projectName);
    Files.writeString(baseDir.resolve(projectName + ".iml"), imlContent);

    // misc.xml (JDK version)
    String miscXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <project version="4">
          <component name="ProjectRootManager" version="2" languageLevel="JDK_%d" project-jdk-name="%d" project-jdk-type="JavaSDK">
            <output url="file://$PROJECT_DIR$/out/production/%s" />
          </component>
        </project>
        """
        .formatted(javaVersion, javaVersion, projectName);
    Files.writeString(ideaDir.resolve("misc.xml"), miscXml);
  }

  private static void generateGitignore(Path baseDir) throws IOException {
    String gitignore = """
        out/
        .idea/
        *.iml
        dist/
        """;
    Files.writeString(baseDir.resolve(".gitignore"), gitignore);
  }
}
