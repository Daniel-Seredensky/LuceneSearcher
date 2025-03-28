# Names: Daniel, Oliwia, Ojo, William 

## Directory Description
- *cranfield* directory contains the Cranfield data set
- *data* directory contains the Project Gutenberg files.  They are numbered pgxxx.txt and pgxxxx.txt.
- *jars* directory contains the Lucene 8.8.2 jar files
- *indexData* directory contains the indexed data 
- *indexCranfield* directory contains the indexed cranfield data 
- *cranfieldSeparated* directory contains the cranfield data separated into individual files

# How to run

## Compilation 

- ***note*** I use zsh for my terminal

``` zsh
# compiles the java files in the main dir, and the GUI files 
javac -cp "jars/*:." *.java GUI/*.java GUI/*/*.java
```

## Run 

### **KWARGS**
- ***explain*** boolean flag for the lucene explanation to be shown in the result (no value needed)
- ***text*** boolean flag for the CLI to be shown instead of the GUI (no value needed) [**GUI not implemented**]
- ***index*** String for the specific index directory to be used
- ***data***  String for the specific data directory to be used
- ***parallel*** boolean flag for the indexing and search parseing to be run in parallel (no value needed) [**not implemented**]
- ***new*** boolean flag to only index new documents
- ***changed*** boolean flag to only index changed documents
- ***missing*** boolean flag to only index missing documents

``` zsh
java -cp "jars/*:." FinalProjMain -kwarg1 value1 -kwarg2 value2
# test the GUI
java -cp "jars/*:." GUI/ComponentTest.java
```

# Search Logic 

``` plaintext
+---------------------+
|    SearchManager    |  <-- Coordinates overall search logic
+---------------------+
           |  First calls the QueryManager
           v
+---------------------+     +---------------------+
|    QueryManager     | <-- |    MyQueryParser    |  <-- Custom parser that creates field-specific queries
+---------------------+     +---------------------+
           |  After Query is build it is passed to the LuceneSearcher
           v
+---------------------+
|   LuceneSearcher    |  <-- Executes the search using the built query from Query Manger (using search(query))
+---------------------+
           | Results object created by Results.fromTopDoc(topDocs)
           v
+---------------------+
|      Results        |  <-- Extracts and formats key fields and best-match fragments 
+---------------------+
```

# GUI

``` plaintext
GUI/
├── GUI.java                     // Main JFrame (**not implemented**)
├── backend/                     // Backend classes for GUI
│   └── AllenIverson.java        // Coordinates search operations and file I/O (**not implemented**)
│   Utilities/                   // Utility classes for GUI
│   └── ScalingUtil.java         // Utility class for scaling components to get a basic version of Swifts dynamic geometry
│   └── DrawingUtils.java        // Utility class for drawing shadows and hover effects
├── components/                  // Custom components for GUI
│   ├── SearchBar.java           // Custom search bar component 
│   ├── SearchButton.java        // Search button component
│   ├── CustomTextField.java     // Text field with custom styling to blend into the JPanel (SearchBar.java)
│   ├── ModernButton.java        // Button designed to look like a SwiftUI rounded button
│   ├── Menu.java                // Settings menu (**not implemented**)
│   ├── Title.java               // Custom formatted title component
│   └── ResultsComponent.java    // Displays search results in a scrollable panel (**not implemented**)
│   GUIProgression/              // Progression classes for GUI
│       └── BaseGUI.java         // Base abstract class for main JFrame handles setup
├── jars/                        // External libraries 
│   └── flatlaf.jar
│   └── **Lucene jars**
├── **Rest of files**
```
