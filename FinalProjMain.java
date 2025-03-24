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
     * @param text Boolean to launch GUI or not
     * @param data String path to data directory
     * @param index String path to index directory 
     * @param mode String optional indexing mode: new, changed, or missing
     * @param Explain Boolean does something but I haven't got there yet
     * @param parallel Boolean does something but I haven't got there yet
     * @author Daniel Seredensky, Oliwia, Ojo
     * @version March 25
     */
    public static void main(String[] args) {
        String dataPath = "./data";
        String indexPath = "./indexData";
        String mode = null; // optional indexing mode: new, changed, or missing
        boolean launchGUI = true;
        boolean explain = false;
        int maxResults = 5;
        
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
                case "-mode":
                    if (i + 1 < args.length) mode = args[++i].toLowerCase();
                    break;
                case  "-explain":
                    explain = true;
                    break;
            }
        }
        
        boolean isGutenberg = false;
        
        // Determine which dataset we are processing.
        if (dataPath.contains("data")) {
            // Gutenberg data.
            isGutenberg = true;
            indexPath = "./indexData";
        } else if (dataPath.contains("cranfieldSeparated")) {
            // Already cleaned cranfield data.
            isGutenberg = false;
            indexPath = "./indexCranfield";
        } else if (dataPath.contains("cranfield")) {
            // Uncleaned cranfield data. Run the cleaner first.
            System.out.println("Running CranfieldCleaner on cranfield data...");
            try {
                CranfieldCleaner.clean();
                // After cleaning, update dataPath to the separated folder.
                dataPath = "./cranfieldSeparated";
                indexPath = "./indexCranfield";
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
        }else {
            try {
                SearchManager sm = new SearchManager(indexPath, explain,maxResults);
                sm.runText();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }
}
