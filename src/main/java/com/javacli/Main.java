package com.javacli;

import com.javacli.commands.NewCommand;
import com.javacli.commands.RunCommand;

public class Main {
  public static void main(String[] args) {
    if (args.length == 0) {
      printUsage();
      return;
    }

    String command = args[0];
    switch (command) {
      case "new":
        NewCommand.execute();
        break;
      case "run":
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
      default:
        System.out.println("Comando no reconocido: " + command);
        printUsage();
    }
  }

  private static void printUsage() {
    System.out.println("Uso: jc <comando>");
    System.out.println("Comandos:");
    System.out.println("  new        - Crear nuevo proyecto Java");
    System.out.println("  run        - Compilar y ejecutar proyecto actual");
    System.out.println("  --version, -v - Mostrar versión");
    System.out.println("  --help, -h     - Mostrar ayuda");
  }
}
