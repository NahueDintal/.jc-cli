#!/bin/bash
echo "Actualizando Java Commander..."
cd ~/.jc-cli

java -jar ~/.jc-cli/jc.jar "$@"

echo "🔨 Compilando..."
javac --release 25 -d bin src/main/java/com/javacli/Main.java src/main/java/com/javacli/commands/*.java

if [ $? -eq 0 ]; then
  jar cfm jc.jar MANIFEST.MF -C bin .
  echo "Jc actualizado correctamente"
else
  echo "Error en compilación"
fi
