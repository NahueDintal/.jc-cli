package com.javacli;

import com.javacli.commands.*;
import com.javacli.commands.TestCommand;

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
          RunCommand.execute();
        }
        break;

      case "build":
        if (args.length > 1 && args[1].equals("test")) {
          if (BuildCommand.compileTestProject()) {
          } else {
            System.err.println("Falló la compilación de tests");
          }
        } else {
          BuildCommand.execute();
        }
        break;

      case "test":
        TestCommand.execute();
        break;

      case "clean":
        CleanCommand.execute();
        break;

      case "--version":
      case "-v":
        System.out.println("Java Commander (jc) v1.0 - Java 25");
        break;

      case "--help":
      case "-h":
        printUsage();
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
    System.out.println("  build             - Compilar código en src/main/java.");
    System.out.println("  build test        - Compilar tests en src/test/java.");
    System.out.println("  run               - Compilar y ejecutar proyecto.");
    System.out.println("  run jar           - Ejecutar JAR generado.");
    System.out.println("  test              - Compilar y ejecutar tests.");
    System.out.println("  clean             - Limpiar archivos compilados.");
    System.out.println("  --version, -v     - Mostrar versión.");
    System.out.println("  --help, -h        - Mostrar ayuda.");
  }
}
