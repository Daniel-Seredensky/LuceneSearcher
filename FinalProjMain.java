import java.io.File;

/**
 * Main Controller for the Final Project
 * @author Daniel Seredensky, Oliwia, Ojo
 * @version March 25
 */
public class FinalProjMain {

    /**
     * Main method for Final Project accepts command-line arguments.
     * <p>
     * Prefix args with - because that is how Goldstein has it in the directions
     * 
     * @param args Command-line arguments
     * <ul>
     *   <li>-text: Boolean to launch GUI or not</li>
     *   <li>-data: String path to data directory</li>
     *   <li>-index: String path to index directory</li>
     *   <li>-mode: String optional indexing mode: new, changed, or missing</li>
     *   <li>-explain: Boolean to include explanations with search results</li>
     *   <li>-parallel: Boolean to enable parallel processing during the indexing process</li>
     *   <li>-cran: Boolean to enable Bonus <b> Haven't gotten there yet </b> </li>
     * </ul>
     * 
     * @author Daniel Seredensky, Oliwia, Ojo
     * @version March 2025
     */

    public static void main(String[] args) {
        String dataPath = "./data";
        String indexPath = "./indexData";
        String mode = null; // optional indexing mode: new, changed, or missing
        boolean launchGUI = true;
        boolean explain = false;
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
            }
        }

        // Validate command-line arguments.
        if (err != null) {
            System.err.println(err);
            return;
        }

        // Validate the index dir or create it if it doesn't exist.
        File testIndexDir = new File(indexPath);
        if (!testIndexDir.exists()) testIndexDir.mkdirs();
        
        boolean isGutenberg = false;
        // Validate the data dir.
        if (dataPath.contains("data")) {
            // Gutenberg data.
            isGutenberg = true;
        } else if (dataPath.contains("cranfieldSeparated")) {
            // Already cleaned cranfield data.
            isGutenberg = false;
        } else if (dataPath.contains("cranfield")) {
            // Uncleaned cranfield data. Run the cleaner first.
            System.out.println("Running CranfieldCleaner on cranfield data...");
            try {
                CranfieldCleaner.clean();
                // After cleaning, update dataPath to the separated folder.
                dataPath = "./cranfieldSeparated";
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            isGutenberg = false;
        } else {
            System.err.println("Error: Invalid data path specified.");
            return;
        }

        System.out.println(launchGUI ? "Launching GUI" : "Launching CLI");
        if (dataPath != null) System.out.println("Data path: " + dataPath);
        if (indexPath != null) System.out.println("Index path: " + indexPath);
        if (mode != null) System.out.println("Indexing mode:mode " + mode);
        System.out.println(isGutenberg ? "Gutenberg data" : "Cranfield data");
        
        TextFileIndexer.run(dataPath, indexPath, mode, isGutenberg);
        
        // Launch GUI 
        if (launchGUI) {
            System.out.println("Launching GUI... (not implemented)");
        } else {
            try {
                SearchManager sm = new SearchManager(indexPath, explain, maxResults);
                sm.runText();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
