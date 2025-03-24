import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.ClassicSimilarity;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * LuceneSearcher initializes the Lucene search components and executes a query.
 * <p>
 * Singleton pattern is used to ensure a single instance of LuceneSearcher
 * @version March 2025
 * @author Daniel Seredensky, Oliwia, Ojo
 */
public class LuceneSearcher {
    private Directory indexDirectory;
    private DirectoryReader indexReader;
    private IndexSearcher searcher;
    private int maxResults;
    private String indexDirPath;
    public static final LuceneSearcher shared = new LuceneSearcher();

    /**
     * Constructs a LuceneSearcher.
     */
    public LuceneSearcher(){}
    
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
    
    public void setIndexDirPath(String indexDirPath) throws  IOException {
        this.indexDirPath = indexDirPath;
    }

    /**
     * Executes the provided query against the index.
     *
     * @param query The Lucene Query to execute.
     * @return A TopDocs object containing the search results.
     * @throws IOException If an error occurs during the search.
     */
    public TopDocs search(Query query) throws IOException {
        // Execute the query using the maximum number of results specified.
        return searcher.search(query, maxResults);
    }

    /**
     * Closes the index reader and directory.
     *
     * @throws IOException If an error occurs during closing.
     */
    public void close() throws IOException {
        if (indexReader != null) {
            indexReader.close();
        }
        if (indexDirectory != null) {
            indexDirectory.close();
        }
    }

    public void open() throws IOException {
        this.indexDirectory = FSDirectory.open(Paths.get(this.indexDirPath));
        this.indexReader = DirectoryReader.open(indexDirectory);
        this.searcher = new IndexSearcher(indexReader);
        searcher.setSimilarity(new ClassicSimilarity()); // TF-IDF scoring instead of BM25 
    }

    /**
     * Returns the underlying IndexSearcher.
     *
     * @return the IndexSearcher.
     */
    public IndexSearcher getIndexSearcher() {
        return searcher;
    }
}
