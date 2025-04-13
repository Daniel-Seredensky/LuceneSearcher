package src;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * CranfieldCleaner.java reads a single file called cranfieldData.txt and splits it
 * into individual .txt files in the folder 
 * <blockquote>
 * <b>"./cranfieldSeparated"</b>
 * </blockquote>
 * 
 * The splitting rules are as follows:
 * <ul>
 *   <li>Lines starting with ".I" indicate a new document. The number following ".I" becomes the filename (e.g., "1.txt").</li>
 *   <li>A line starting with ".T" indicates that subsequent lines (until ".A") form the title.
 *       The title is stored as a single line (concatenating multiple lines with a space).</li>
 *   <li>A line starting with ".A" indicates that the next line contains the author's name.</li>
 *   <li>Ignore the ".B" line and the line immediately following it.</li>
 *   <li>A line starting with ".W" indicates that subsequent lines form the document's content.
 *       The content continues until the next ".I". Additionally, any line in the content that does not end
 *       with a period is concatenated with the following line, and if a line ends with a period, a newline is attached.</li>
 * </ul>
 * 
 * Usage:
 *   {@link CranfieldCleaner#clean()}
 * 
 * @version March 2025
 * @author Daniel, Oliwia, Ojo, William
 */
public class CranfieldCleaner {

    public static void main(String[] args) {
        try {
            clean();
            System.out.println("Cranfield data cleaned successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clean() throws IOException {
        File inputFile = new File("./cranfield/cranfieldData.txt");
        if (!inputFile.exists()) {
            System.err.println("cranfieldData.txt not found.");
            return;
        }
        
        // Create output directory if it doesn't exist.
        File outputDir = new File("./cranfieldSeparated/");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        } else {
            // Delete all files in the output directory because if clean is called assume we want to start fresh
            for (File file : outputDir.listFiles()) {
                file.delete();
            }
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            String currentFileName = null;
            StringBuilder titleBuilder = new StringBuilder();
            String author = "";
            StringBuilder contentBuilder = new StringBuilder();
            boolean readingTitle = false;
            boolean readingContent = false;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(".I")) {
                    if (currentFileName != null) {
                        writeDocument(outputDir, currentFileName, titleBuilder.toString(), author, processContent(contentBuilder.toString()));
                    }
                    // Start a new document.
                    currentFileName = line.substring(2).trim();  
                    titleBuilder = new StringBuilder();
                    author = "";
                    contentBuilder = new StringBuilder();
                    readingTitle = false;
                    readingContent = false;
                    continue;
                }
                if (line.startsWith(".T")) {
                    readingTitle = true;
                    readingContent = false;
                    continue;
                }
                if (line.startsWith(".A")) {
                    readingTitle = false;
                    readingContent = false;
                    // Next line has the author.
                    line = reader.readLine();
                    if (line != null) {
                        author = line.trim();
                    }
                    continue;
                }
                if (line.startsWith(".B")) {
                    // Ignore .B and the following line.
                    reader.readLine();
                    continue;
                }
                if (line.startsWith(".W")) {
                    readingContent = true;
                    readingTitle = false;
                    continue;
                }
                
                if (readingTitle) {
                    // Append title lines (concatenate into one line).
                    titleBuilder.append(line).append(" ");
                }
                if (readingContent) {
                    contentBuilder.append(line).append("\n");
                }
            }
            // Write out the last document if present.
            if (currentFileName != null) {
                writeDocument(outputDir, currentFileName, titleBuilder.toString(), author, processContent(contentBuilder.toString()));
            }
        }
    }

    /**
     * processContent processes the raw content so that any line that does not end with a period
     * is concatenated with the following line. Additionally, if the concatenated line ends with a period,
     * a newline is attached.
     *
     * @param content Raw content text.
     * @return Processed content as a single string.
     */
    private static String processContent(String content) {
        String[] lines = content.split("\n");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String currentLine = lines[i].trim();
            if (currentLine.isEmpty()) continue;
            // Concatenate subsequent lines if current line does not end with a period.
            while (!currentLine.endsWith(".") && i + 1 < lines.length) {
                currentLine += " " + lines[++i].trim();
            }
            // If the line ends with a period, append a newline.
            if (currentLine.endsWith(".")) {
                result.append(currentLine).append("\n");
            } else {
                result.append(currentLine).append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * writeDocument writes the separated document to a file in the output directory.
     * The file is named with the document number (e.g., "1.txt").
     * The output file now starts with "Title: " followed by the title and "Author: " followed by the author.
     *
     * @param outputDir    The directory where files will be written.
     * @param fileName     The base name for the file.
     * @param title        The title (as a single line).
     * @param author       The author name.
     * @param content      The processed content.
     * @throws IOException
     */
    private static void writeDocument(File outputDir, String fileName, String title, String author, String content) throws IOException {
        String cleanTitle = title.replaceAll("\\s+", " ").trim();
        File outFile = new File(outputDir, fileName + ".txt");
        try (PrintWriter out = new PrintWriter(new FileWriter(outFile))) {
            out.println("Title: " + cleanTitle);
            out.println("Author: " + author);
            out.println(content);
        }
    }
}
