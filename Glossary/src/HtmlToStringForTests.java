import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Elijah Paulman.
 *
 */
public final class HtmlToStringForTests {

    /**
     * Converts html file into string (used for junit tests).
     */
    private HtmlToStringForTests() {

    }

    /**
     * @param outputFile
     * @return file as string
     */
    public static String htmlConvert(File outputFile) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new FileReader(outputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
