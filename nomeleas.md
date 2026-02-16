Detalles del Java Commander!

# Ejecuta para actualizar el cli

javac --release 25 -d bin src/main/java/com/javacli/Main.java src/main/java/com/javacli/commands/*.java

# Para recrear el JAR

jar cfm jc.jar MANIFEST.MF -C bin .

# o poder usar el ./update.sh para hacer estos pasos.

# copiar el script jc en la carpeta bin,
# en caso de omarchy está en 
# .local/share/omarchy/bin

para usar la herramienta con el mismo jc en .jc hay que usar 

./jc o java -cp bin com.javacli.Main.

ya que te va a decir que falta el jc.json.
