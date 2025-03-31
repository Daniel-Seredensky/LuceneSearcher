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

``` mermaid 
flowchart TD
    SM[SearchManager\n(Coordinates overall search logic)]
    QM[QueryManager]
    MP[MyQueryParser\n(Custom parser that creates field-specific queries)]
    LS[LuceneSearcher\n(Executes search using built query)]
    R[Results\n(Extracts and formats key fields and best-match fragments)]

    SM -->|Calls| QM
    QM -->|Utilizes| MP
    QM -->|Passes built query| LS
    LS -->|Generates| R
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

``` mermaid
flowchart TD
    A[GUI/]
    B[GUI.java\n(Main JFrame, not implemented)]
    C[backend/]
    D[AllenIverson.java\n(Coordinates search operations and file I/O, not implemented)]
    E[Utilities/]
    F[ScalingUtil.java\n(Scales components for dynamic geometry)]
    G[DrawingUtils.java\n(Draws shadows and hover effects)]
    H[components/]
    I[SearchBar.java\n(Custom search bar component)]
    J[SearchButton.java\n(Search button component)]
    K[CustomTextField.java\n(Styled text field for SearchBar)]
    L[ModernButton.java\n(SwiftUI-like rounded button)]
    M[Menu.java\n(Settings menu, not implemented)]
    N[Title.java\n(Custom formatted title component)]
    O[ResultsComponent.java\n(Displays search results in a scrollable panel, not implemented)]
    P[GUIProgression/]
    Q[BaseGUI.java\n(Abstract class for main JFrame setup)]
    R[jars/]
    S[flatlaf.jar]
    T[Lucene jars]

    A --> B
    A --> C
    C --> D
    C --> E
    E --> F
    E --> G
    A --> H
    H --> I
    H --> J
    H --> K
    H --> L
    H --> M
    H --> N
    H --> O
    A --> P
    P --> Q
    A --> R
    R --> S
    R --> T
```
