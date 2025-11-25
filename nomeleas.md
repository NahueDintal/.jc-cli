# Ejecuta para actualizar el cli

javac --release 25 -d bin src/main/java/com/javacli/Main.java src/main/java/com/javacli/commands/*.java

# Para recrear el JAR

jar cfm jc.jar MANIFEST.MF -C bin .
