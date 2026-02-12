import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

public class JcTests {

  @TempDir
  Path tempDir; // directorio temporal para cada test

  @Test
  public void testNewCommandCreaMainJava() throws Exception {
    // Simular que estamos en el directorio temporal
    Path originalPath = Paths.get("").toAbsolutePath();
    System.setProperty("user.dir", tempDir.toString());

    // Ejecutar el comando new
    com.javacli.commands.NewCommand.execute();

    // Verificar que se creó el archivo Main.java
    Path mainJava = tempDir.resolve("src/main/java/Main.java");
    assertTrue(Files.exists(mainJava), "Main.java no fue creado");

    // Restaurar directorio original
    System.setProperty("user.dir", originalPath.toString());
  }

  @Test
  public void testNewCommandCreaDirectorioLib() throws Exception {
    Path originalPath = Paths.get("").toAbsolutePath();
    System.setProperty("user.dir", tempDir.toString());

    com.javacli.commands.NewCommand.execute();

    Path libDir = tempDir.resolve("lib");
    assertTrue(Files.exists(libDir) && Files.isDirectory(libDir));

    System.setProperty("user.dir", originalPath.toString());
  }
}
