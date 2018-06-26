import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;

public class ExtensionLevelsHandler {
    private static final String levelsPath = "C:\\temp\\ABoffline\\Load custom Angry Birds levels\\Installing custom levels\\Past competition levels";
    private static final String destinationPath = "C:\\temp\\ABoffline\\Load custom Angry Birds levels\\Installing custom levels\\custom_levels\\levels";
    private static String levelName = "Level1-%d.json";

    public static void copy(String[] pLevels) {
        for (int i = 0; i < pLevels.length; i++) {
            try {
                String levelName = String.format(ExtensionLevelsHandler.levelName, i + 1);
                System.out.println(String.format("Copying level %s to %s", pLevels[i], levelName));
                Files.copy(Paths.get(levelsPath, pLevels[i]),
                        Paths.get(destinationPath, levelName),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}