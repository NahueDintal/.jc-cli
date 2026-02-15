#!/bin/bash
echo "Actualizando Java Commander..."
# Compilar todos los .java
javac --release 25 -d bin $(find src/main/java -name "*.java")
# Crear el JAR (opcional, si quieres actualizar jc.jar)
jar cfm jc.jar MANIFEST.MF -C bin .
echo "Compilación completada."
