package com.javacli.commands;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CleanCommand {
  public static void execute() {
    try {
      System.out.println("Limpiando archivos compilados...");

      boolean deletedSomething = false;

      if (Files.exists(Paths.get("bin"))) {
        deleteDirectoryRecursively(Paths.get("bin"));
        System.out.println("Directorio 'bin/' eliminado");
        deletedSomething = true;
      }

      if (!deletedSomething) {
        System.out.println("No hay nada que limpiar");
      } else {
        System.out.println("Limpieza completada!");
      }

    } catch (Exception e) {
      System.err.println("Error durante clean: " + e.getMessage());
    }
  }

  private static void deleteDirectoryRecursively(Path path) throws IOException {
    if (Files.isDirectory(path)) {
      Files.walk(path)
          .sorted((a, b) -> -a.compareTo(b))
          .forEach(p -> {
            try {
              Files.delete(p);
            } catch (IOException e) {
              System.err.println("No se pudo eliminar: " + p);
            }
          });
    }
  }
}
