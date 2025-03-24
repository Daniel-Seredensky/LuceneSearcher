# Names: Daniel Seredensky, Oliwia Majtyka, Ojo

*cranfield* directory contains the Cranfield data set<p>
*data* directory contains the Project Gutenberg files.  They are numbered pgxxx.txt and pgxxxx.txt.<p>
*jars* directory contains the Lucene 8.8.2 jar files<p>
*indexData* directory contains the indexed data <p>
*indexCranfield* directory contains the indexed cranfield data <p>
*cranfieldSeparated* directory contains the cranfield data separated into individual files<p>

## How to run
Check out **CommandLineNotes.md** for a basic guide on how to run the program


## Search Logic 
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