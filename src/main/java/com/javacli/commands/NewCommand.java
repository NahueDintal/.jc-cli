package com.javacli.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NewCommand {
    public static void execute() {
        try {
            String projectName = getProjectName();
            createProjectStructure(projectName);
            createMainJavaFile(projectName);
            System.out.println("✅ Proyecto Java 25 '" + projectName + "' creado exitosamente!");
            System.out.println("📝 Usa: 'jc run' para compilar y ejecutar");
        } catch (Exception e) {
            System.err.println("❌ Error al crear proyecto: " + e.getMessage());
        }
    }

    private static String getProjectName() {
        Path currentPath = Paths.get("").toAbsolutePath();
        return currentPath.getFileName().toString();
    }

    private static void createProjectStructure(String projectName) throws IOException {
        Files.createDirectories(Paths.get("src/main/java"));
        Files.createDirectories(Paths.get("src/test/java"));
        Files.createDirectories(Paths.get("lib"));
    }

    private static void createMainJavaFile(String projectName) throws IOException {
        String mainJavaContent = """
            /**
             * Proyecto: %s
             * Generado automáticamente con jc
             */
            public class Main {
                public static void main(String[] args) {
                    System.out.println("¡Hola desde %s con Java 25!");
                    System.out.println("Directorio: " + System.getProperty("user.dir"));
                    
                    // Demo de características Java 25
                    demoJavaFeatures();
                }
                
                private static void demoJavaFeatures() {
                    var message = "Java 25 funcionando correctamente!";
                    System.out.println(message);
                    
                    // Pattern matching instanceOf
                    Object obj = "Texto de ejemplo";
                    if (obj instanceof String s) {
                        System.out.println("Longitud del string: " + s.length());
                    }
                    
                    // Switch expressions (Java 14+)
                    int day = 3;
                    String dayType = switch (day) {
                        case 1, 2, 3, 4, 5 -> "Día laboral";
                        case 6, 7 -> "Fin de semana";
                        default -> "Día inválido";
                    };
                    System.out.println("Tipo de día: " + dayType);
                }
            }
            """.formatted(projectName, projectName);

        Files.writeString(Paths.get("src/main/java/Main.java"), mainJavaContent);
    }
}
