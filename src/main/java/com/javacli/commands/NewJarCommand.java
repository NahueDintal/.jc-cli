package com.javacli.commands;

import java.io.*;
import java.nio.file.*;
import java.util.jar.*;

public class NewJarCommand {
  public static void execute() {
    try {
      // Verificar que existe compilación
      if (!Files.exists(Paths.get("bin"))) {
        System.out.println("No hay compilación previa. Ejecutando 'jc build'...");
        if (!BuildCommand.compileProject()) {
          System.err.println("❌ Compilación fallida. No se puede empaquetar.");
          return;
        }
      }

      // Crear directorio dist/
      Files.createDirectories(Paths.get("dist"));

      // Nombre del JAR (usa el nombre del directorio)
      String projectName = Paths.get("").toAbsolutePath().getFileName().toString();
      Path jarPath = Paths.get("dist", projectName + ".jar");

      // Crear manifiesto
      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
      manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "Main"); // Ajustar si usas paquetes

      // Empaquetar
      try (JarOutputStream jarOut = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
        // Agregar archivos .class
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
                System.err.println("⚠️ No se pudo agregar: " + entryName);
              }
            });

        // Opcional: incluir dependencias (fat jar)
        // includeDependencies(jarOut);
      }

      System.out.println("✅ JAR creado: " + jarPath.toAbsolutePath());
      System.out.println("   Ejecuta: jc run jar  o  java -jar " + jarPath);

    } catch (Exception e) {
      System.err.println("❌ Error al empaquetar: " + e.getMessage());
    }
  }

  // Método para crear fat-jar (incluye dependencias)
  @SuppressWarnings("unused")
  private static void includeDependencies(JarOutputStream jarOut) throws IOException {
    Path libDir = Paths.get("lib");
    if (!Files.exists(libDir))
      return;

    Files.walk(libDir)
        .filter(p -> p.toString().endsWith(".jar"))
        .forEach(jarFile -> {
          try (JarInputStream jis = new JarInputStream(Files.newInputStream(jarFile))) {
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
              // Ignorar manifiestos y firmas
              if (entry.getName().startsWith("META-INF/") ||
                  entry.getName().endsWith(".SF") ||
                  entry.getName().endsWith(".DSA") ||
                  entry.getName().endsWith(".RSA"))
                continue;

              jarOut.putNextEntry(new JarEntry("lib/" + entry.getName()));
              byte[] buffer = new byte[8192];
              int read;
              while ((read = jis.read(buffer)) != -1) {
                jarOut.write(buffer, 0, read);
              }
              jarOut.closeEntry();
            }
          } catch (IOException e) {
            System.err.println("⚠️ Error incluyendo " + jarFile);
          }
        });
  }
}
