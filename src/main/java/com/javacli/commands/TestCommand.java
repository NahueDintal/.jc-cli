package com.javacli.commands;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestCommand {

  public static void execute() {
    try {
      System.out.println("🔨 Compilando tests...");
      if (!BuildCommand.compileTestProject()) {
        System.err.println("❌ Falló la compilación de tests");
        return;
      }

      System.out.println("🚀 Ejecutando tests...");
      runTests();

    } catch (Exception e) {
      System.err.println("❌ Error ejecutando tests: " + e.getMessage());
    }
  }

  private static void runTests() throws IOException, InterruptedException {
    Path junitJar = findJUnitJar();
    if (junitJar == null) {
      System.err.println("❌ No se encontró JUnit en lib/. Descargalo con 'jc add test'");
      return;
    }

    String cp = "bin-test" + File.pathSeparator +
        "bin" + File.pathSeparator +
        BuildCommand.buildClasspath();

    // Usar 'execute' en lugar de --scan-classpath para evitar warning
    List<String> cmd = List.of(
        "java", "-cp", cp,
        "org.junit.platform.console.ConsoleLauncher",
        "execute",
        "--scan-classpath",
        "--details=tree");

    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.inheritIO();
    Process process = pb.start();
    int exitCode = process.waitFor();

    if (exitCode == 0) {
      System.out.println("✅ Tests exitosos");
    } else {
      System.err.println("⚠️ Algunos tests fallaron");
    }
  }

  private static Path findJUnitJar() throws IOException {
    Path libDir = Paths.get("lib");
    if (!Files.exists(libDir))
      return null;
    return Files.walk(libDir)
        .filter(p -> p.toString().toLowerCase().contains("junit"))
        .filter(p -> p.toString().endsWith(".jar"))
        .findFirst()
        .orElse(null);
  }
}
