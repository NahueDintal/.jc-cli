package com.javacli.commands;

import com.javacli.config.ProjectConfig;
import java.io.*;
import java.nio.file.*;
import java.util.jar.*;

public class NewJarCommand {

  public static void execute() {
    try {
      // Verificar si existe compilación previa
      if (!Files.exists(Paths.get("bin"))) {
        System.out.println("No hay compilación previa. Ejecutando 'jc build'...");
        if (!BuildCommand.compileProject()) {
          System.err.println("Compilación fallida. No se puede empaquetar.");
          return;
        }
      }

      ProjectConfig config = null;
      String mainClass = "Main";
      String projectName = Paths.get("").toAbsolutePath().getFileName().toString();

      try {
        config = ProjectConfig.load(Paths.get(""));
        mainClass = config.getMainClass();
        if (config.getName() != null && !config.getName().isEmpty()) {
          projectName = config.getName();
        }
      } catch (Exception e) {
        System.out.println("No se encontró jc.json, usando valores por defecto.");
      }

      Files.createDirectories(Paths.get("dist"));

      Path jarPath = Paths.get("dist", projectName + ".jar");

      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
      manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);

      try (JarOutputStream jarOut = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
        Files.walk(Paths.get("bin"))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".class"))
            .forEach(classFile -> {
              String entryName = Paths.get("bin").relativize(classFile)
                  .toString().replace('\\', '/');
              try {
                jarOut.putNextEntry(new JarEntry(entryName));
                Files.copy(classFile, jarOut);
                jarOut.closeEntry();
              } catch (IOException e) {
                System.err.println("No se pudo agregar: " + entryName);
              }
            });
      }

      System.out.println("JAR creado: " + jarPath.toAbsolutePath());
      System.out.println("Ejecuta: jc run jar  o  java -jar " + jarPath);

    } catch (Exception e) {
      System.err.println("Error al empaquetar: " + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  private static void includeDependencies(JarOutputStream jarOut, ProjectConfig config) throws IOException {
    if (config == null)
      return;

    for (String dep : config.getDependencies()) {
      Path depPath = Paths.get(dep);
      if (!Files.exists(depPath))
        continue;

      try (JarInputStream jis = new JarInputStream(Files.newInputStream(depPath))) {
        JarEntry entry;
        while ((entry = jis.getNextJarEntry()) != null) {
          if (entry.getName().startsWith("META-INF/") &&
              (entry.getName().endsWith(".SF") ||
                  entry.getName().endsWith(".DSA") ||
                  entry.getName().endsWith(".RSA") ||
                  entry.getName().equals("META-INF/MANIFEST.MF"))) {
            continue;
          }

          jarOut.putNextEntry(new JarEntry(entry.getName()));
          byte[] buffer = new byte[8192];
          int read;
          while ((read = jis.read(buffer)) != -1) {
            jarOut.write(buffer, 0, read);
          }
          jarOut.closeEntry();
        }
      } catch (IOException e) {
        System.err.println("Error incluyendo dependencia: " + dep);
      }
    }
  }
}
