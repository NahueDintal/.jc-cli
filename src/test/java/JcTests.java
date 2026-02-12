import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

public class JcTests {

  @TempDir
  Path tempDir;

  @Test
  public void testNewCommandCreaMainJava() throws Exception {
    // Ejecutar NewCommand en el directorio temporal
    com.javacli.commands.NewCommand.execute(tempDir);

    Path mainJava = tempDir.resolve("src/main/java/Main.java");
    assertTrue(Files.exists(mainJava), "Main.java no fue creado");
  }

  @Test
  public void testNewCommandCreaDirectorioLib() throws Exception {
    com.javacli.commands.NewCommand.execute(tempDir);

    Path libDir = tempDir.resolve("lib");
    assertTrue(Files.exists(libDir) && Files.isDirectory(libDir));
  }
}
