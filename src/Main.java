package src;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import javax.swing.SwingUtilities;
import GUI.GUIProgression.SearcherUI;
import src.Indexers.TextFileIndexer;
import src.Indexers.TextFileIndexerPBatch;
import src.Indexers.TextFileIndexerParallel;
import src.Indexers.TextFileIndexer.IndexingResult;
import src.Indexers.TextIndexingHelper;

/**
 * Main Controller for the Final Project
 * @author Daniel Seredensky, Oliwia, Ojo
 * @version March 25
 */
public class Main {

    /**
     * Main method for Final Project accepts command-line arguments.
     * <p>
     * Boolean flags require no subsequent values
     * 
     * @param args Command-line arguments
     * <ul>
     *   <li>-text: Boolean to launch GUI or not</li>
     *   <li>-data: Flag followed by string path to data directory </li>
     *   <li>-index: Flag followed by string path to index directory</li>
     *   <li>-new: Boolean to index only new</li>
     *   <li>-missing: Boolean to index only missing documents</li>
     *   <li>-changed: Boolean to index only changed documents</li>
     *   <li>-explain: Boolean to include explanations with search results</li>
     *   <li>-parallel: Boolean to enable parallel processing during the indexing process</li>
     *   <li>-batch: Boolean to enable batch parallel processing during the indexing process</li>
     *   <li>-CleanCranfield: Boolean Cleans the cranfield.txt into <i> ./cranfieldSeparated/</i> then returns</li>
     * </ul>
     * 
     * @author Daniel, Oliwia, Ojo, William
     * @version March 2025
     */
    public static void main(String[] args) {
        String dataPath = "./data";
        String indexPath = "./indexData";
        String mode = null; // optional indexing mode: new, changed, or missing
        boolean launchGUI = true;
        boolean explain = false;
        boolean parallel = false;
        boolean batch = false;
        boolean shouldClean = false;
        int maxResults = 5;
        String err = null;

        
        // Parse command-line arguments.
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-text":
                    launchGUI = false;
                    break;  
                case "-data":
                    if (i + 1 < args.length) dataPath = args[++i];
                    break;  
                case "-index":
                    if (i + 1 < args.length) indexPath = args[++i];
                    break;
                case "-new":
                    err = mode == null ? null: "Invalid multiple modes specified choose one of (new | changed | missing)";
                    mode = "new";
                    break;
                case "-changed":
                    err = mode == null ? null: "Invalid multiple modes specified choose one of (new | changed | missing)";
                    mode = "changed";
                    break;
                case "-missing":
                    err = mode == null ? null: "Invalid multiple modes specified choose one of (new | changed | missing)";
                    mode = "missing";
                    break;
                case  "-explain":
                    explain = true;
                    break;
                case "-parallel":
                    parallel = true;
                    err = batch ? "Invalid multiple indexing modes specified choose one of (parallel | batch | <no arg for default>)" : null;
                    break;
                case "-batch":
                    batch = true;
                    err = parallel ? "Invalid multiple indexing modes specified choose one of (parallel | batch | <no arg for default>)" : null;
                    break;
                case "-CleanCranfield":
                    shouldClean = true;
                    break; 
            }
        }

        // Clean Cranfield data if requested and return 
        if (shouldClean) {
            try{
                CranfieldCleaner.clean();
                System.out.println("Cranfield data cleaning complete.");
                return;
            } catch (Exception e) {
                System.err.println("Error cleaning Cranfield data: " + e.getMessage());
                return;
            }
        }

        // Validate command-line arguments.
        if (err != null) {
            System.err.println(err);
            return;
        }

        // Validate the data directory
        Boolean isGutenberg = isDataGutenberg(dataPath);
        if (isGutenberg == null) {
            System.err.println("Invalid data directory: " + dataPath);
            return;
        }

        // Validate index dir / create if needed
        if (indexPath == null || indexPath.isEmpty()) {
            System.err.println("Invalid index directory: " + indexPath);
            return;
        }
        File indexDir = new File(indexPath);
        if (!indexDir.exists()) {
            indexDir.mkdir();
        }

        // Summarize parsed arguments
        System.out.println(launchGUI ? "Launching GUI" : "Launching CLI");
        explain = explain || launchGUI;
        System.out.println("Data path: " + dataPath);
        System.out.println("Index path: " + indexPath);
        System.out.println(mode != null ? ("Indexing mode: " + mode): ("Indexing mode: default"));
        System.out.println(explain? "Explain: true" : "Explain: false");
        if (parallel) System.out.println("Parallel indexing: true");
        if (batch) System.out.println("Batch indexing: true");
        if (! (batch || parallel)) System.out.println("Default Indexing: true");
        System.out.println(isGutenberg ? "Gutenberg data" : "Cranfield data");
        
        IndexingResult indexingResults;
        if (! (batch || parallel)) {
            TextFileIndexer.run(dataPath, indexPath, mode, isGutenberg,true);
            indexingResults = TextFileIndexer.getMostRecentIndexingResult();
        }
        else if (batch && ! parallel) {
            TextFileIndexerPBatch.run(dataPath, indexPath, mode, isGutenberg,true);
            indexingResults = TextFileIndexerPBatch.getMostRecentIndexingResult();
        }
        else if (parallel && !batch) {
            TextFileIndexerParallel.run(dataPath, indexPath, mode, isGutenberg,true);
            indexingResults = TextFileIndexerParallel.getMostRecentIndexingResult();
        } else {
            // We should never get here, but if we do, we'll print an error message and return
            System.out.println("Indexer arguments are mutually exclusive,\n please choose one of the following: -batch | -parallel | <no arg for default>");
            return;
        }
        
        if (launchGUI) {
            final String indexPathFinal = indexPath; // Java wants indexPath and explan to be final
            final boolean explainFinal = explain;
            SwingUtilities.invokeLater(() -> {
                SearcherUI ui = new SearcherUI("Lucene Searcher",indexPathFinal, explainFinal, maxResults,indexingResults);
                ui.setVisible(true);
            });
        } else {
            try {
                SearchManager sm = new SearchManager(indexPath, explain, maxResults);
                sm.runText();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Validates the data directory, and returns a boolean indicating whether the data is Gutenberg or not.
     * 
     * @param dataPath The path to the directory containing text files
     * @return Boolean.TRUE if a Gutenberg file is detected, Boolean.FALSE if not Gutenberg,
     *         or null if the directory is empty, invalid, or contains non-txt files
     */
    public static Boolean isDataGutenberg(String dataPath) {
        // Validating path
        if (dataPath == null || dataPath.trim().isEmpty()) {
            return null;
        }
        File directory = new File(dataPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }
        
        // Validating dir content
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        for (File file : files) {
            if (file.isFile() && !file.getName().toLowerCase().endsWith(".txt")) {
                // Found a non-txt file
                return null;
            }
        }
        
        // Now sample one text file to check for Gutenberg format
        for (File file : files) {
            try {
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                
                // Check if the content contains the Gutenberg marker
                if (content.contains(TextIndexingHelper.GUTENBERG_START)) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
                
            } catch (IOException e) {
                // If some error occurs, skip this file and continue
                continue;
            }
        }
        
        // Something bizarre has happened if we made it this far
        return null;
    }
}
