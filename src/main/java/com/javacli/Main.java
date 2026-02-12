package com.javacli;

import com.javacli.commands.*;

public class Main {
  public static void main(String[] args) {
    if (args.length == 0) {
      printUsage();
      return;
    }

    String command = args[0];
    switch (command) {
      case "new":
        if (args.length > 1 && args[1].equals("jar")) {
          NewJarCommand.execute();
        } else {
          NewCommand.execute();
        }
        break;
      case "run":
        if (args.length > 1 && args[1].equals("jar")) {
          RunJarCommand.execute();
        } else {
          NewCommand.execute();
        }
        RunCommand.execute();
        break;
      case "--version":
      case "-v":
        System.out.println("Java Commander (jc) v1.0 - Java 25");
        break;
      case "--help":
      case "-h":
        printUsage();
        break;
      case "build":
        BuildCommand.execute();
        break;
      case "clean":
        CleanCommand.execute();
        break;
      default:
        System.out.println("Comando no reconocido: " + command);
        printUsage();
    }
  }

  private static void printUsage() {
    System.out.println("Uso: jc <comando>");
    System.out.println("Comandos:");
    System.out.println("  new               - Crear nuevo proyecto Java.");
    System.out.println("  new jar           - Empaquetar proyecto en JAR ejecutable.");
    System.out.println("  build             - Construye el proyecto Java.");
    System.out.println("  run               - Compilar y ejecutar proyecto actual.");
    System.out.println("  run jar           - Ejecutar el JAR generado (desde dist/).");
    System.out.println("  clean             - Limpia los archivos temporales.");
    System.out.println("  --version, -v     - Mostrar versión.");
    System.out.println("  --help, -h        - Mostrar ayuda.");
  }
}
