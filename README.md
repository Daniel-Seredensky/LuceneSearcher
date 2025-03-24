# Names: Daniel Seredensky, Oliwia Majtyka, Ojo

*cranfield* directory contains the Cranfield data set<p>
*data* directory contains the Project Gutenberg files.  They are numbered pgxxx.txt and pgxxxx.txt.<p>
*jars* directory contains the Lucene 8.8.2 jar files<p>
*indexData* directory contains the indexed data <p>
*indexCranfield* directory contains the indexed cranfield data <p>
*cranfieldSeparated* directory contains the cranfield data separated into individual files<p>

# How to run

## Compilation 

***note*** I use zsh for my terminal

``` zsh
javac -cp "jars/*:." *.java  
```

## Run 

### **KWARGS**
- ***explain*** boolean flag for the lucene explanation to be shown in the result (no value needed)
- ***text*** boolean flag for the CLI to be shown instead of the GUI (no value needed) *GUI not implemented*
- ***index*** String for the specific index directory to be used
- ***data***  String for the specific data directory to be used
- ***parallel*** boolean flag for the indexing and search parseing to be run in parallel (no value needed) *not implemented*

``` zsh
java -cp "jars/*:." FinalProjMain -kwarg1 value1 -kwarg2 value2
```

# Search Logic 

``` plainttext
+---------------------+
|    SearchManager    |  <-- Coordinates overall search logic
+---------------------+
           |
           v
+---------------------+     +---------------------+
|    QueryManager     | <-- |    MyQueryParser    |  <-- Custom parser that creates field-specific queries
+---------------------+     +---------------------+
           |
           v
+---------------------+
|      Searcher       |  <-- Executes the Lucene Query (using IndexSearcher)
+---------------------+
           |
           v
+---------------------+
| ResultsFormatter    |  <-- Extracts and formats key fields and best-match fragments
+---------------------+
```