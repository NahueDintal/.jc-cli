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
          System.err.println("❌ Compilación fallida. No se puede empaquetar.");
          return;
        }
      }

      // Cargar configuración del proyecto (jc.json)
      ProjectConfig config = null;
      String mainClass = "Main"; // valor por defecto
      String projectName = Paths.get("").toAbsolutePath().getFileName().toString();

      try {
        config = ProjectConfig.load(Paths.get(""));
        mainClass = config.getMainClass();
        // Si el proyecto tiene nombre en la configuración, usarlo
        if (config.getName() != null && !config.getName().isEmpty()) {
          projectName = config.getName();
        }
      } catch (Exception e) {
        System.out.println("No se encontró jc.json, usando valores por defecto.");
      }

      // Crear directorio dist/
      Files.createDirectories(Paths.get("dist"));

      Path jarPath = Paths.get("dist", projectName + ".jar");

      // Crear manifiesto
      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
      manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);

      // Empaquetar
      try (JarOutputStream jarOut = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
        // Agregar archivos .class del directorio bin
        Files.walk(Paths.get("bin"))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".class"))
            .forEach(classFile -> {
              // Calcular nombre de entrada en el JAR (ruta relativa a bin)
              String entryName = Paths.get("bin").relativize(classFile)
                  .toString().replace('\\', '/');
              try {
                jarOut.putNextEntry(new JarEntry(entryName));
                Files.copy(classFile, jarOut);
                jarOut.closeEntry();
              } catch (IOException e) {
                System.err.println("⚠️ No se pudo agregar: " + entryName);
              }
            });

        // Opcional: incluir dependencias (fat jar) - comentado por ahora
        // includeDependencies(jarOut, config);
      }

      System.out.println("✅ JAR creado: " + jarPath.toAbsolutePath());
      System.out.println("   Ejecuta: jc run jar  o  java -jar " + jarPath);

    } catch (Exception e) {
      System.err.println("❌ Error al empaquetar: " + e.getMessage());
    }
  }

  // Método para crear fat-jar (incluye dependencias) - opcional
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
          // Ignorar archivos de firma y META-INF duplicados
          if (entry.getName().startsWith("META-INF/") &&
              (entry.getName().endsWith(".SF") ||
                  entry.getName().endsWith(".DSA") ||
                  entry.getName().endsWith(".RSA") ||
                  entry.getName().equals("META-INF/MANIFEST.MF"))) {
            continue;
          }

          // Evitar duplicados (opcional, se podría sobrescribir)
          jarOut.putNextEntry(new JarEntry(entry.getName()));
          byte[] buffer = new byte[8192];
          int read;
          while ((read = jis.read(buffer)) != -1) {
            jarOut.write(buffer, 0, read);
          }
          jarOut.closeEntry();
        }
      } catch (IOException e) {
        System.err.println("⚠️ Error incluyendo dependencia: " + dep);
      }
    }
  }
}
