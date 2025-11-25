#!/bin/bash

echo "Actualizando..."

javac --release 25 -d bin src/main/java/com/javacli/Main.java src/main/java/com/javacli/commands/*.java || jar cfm jc.jar MANIFEST.MF -C bin && echo "Los cambios se realizaron con éxito."
