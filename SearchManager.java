import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Set;

/**
 * SearchManager performs searches on a Lucene index using the custom query parsing and
 * results formatting components.
 *
 * <p>
 * It uses QueryManager to parse and process the user's query via the MyQueryParser
 * (which in turn uses a PerFieldAnalyzerWrapper). It then creates a LuceneSearcher to execute the query
 * and passes the TopDocs result to Results for formatting. The Results formatter always includes the mandatory
 * fields (title, author, filename) and for any extra requested fields uses the Explanation API to decide which
 * field contributed most to the match, highlighting its best fragment when available.
 * </p>
 *
 * @version March 2025
 * @author Daniel Seredensky, Oliwia, Ojo
 */
public class SearchManager {
    private String indexDirPath;
    private boolean explain;
    private int MAX_RESULTS;
    private boolean isLenientQuery;
    private QueryManager queryManager;
    private LuceneSearcher luceneSearcher;

    /**
     * Constructs a SearchManager.
     *
     * @param indexDirPath The path to the Lucene index directory.
     * @param explain      If true, explanations will be included in the results.
     * @param maxResults   Maximum number of search results to return.
     */
    public SearchManager(String indexDirPath, boolean explain, int maxResults) throws IOException {
        this.indexDirPath = indexDirPath;
        this.explain = explain;
        this.MAX_RESULTS = maxResults;
        //singleton instances for QueryManager and LuceneSearcher
        this.queryManager = QueryManager.shared;
        this.luceneSearcher = LuceneSearcher.shared;
        // luceneSearcher init steps
        this.luceneSearcher.setIndexDirPath(indexDirPath);
        this.luceneSearcher.setMaxResults(maxResults);
        this.isLenientQuery = false;
    }

    // Setters for instance variables.
    public void setIndexDirPath(String indexDirPath) throws IOException {
        this.indexDirPath = indexDirPath;
        this.luceneSearcher.setIndexDirPath(indexDirPath);
    }

    public void setExplain(boolean explain) {
        this.explain = explain;
    }

    public void setMaxResults(int maxResults) {
        this.MAX_RESULTS = maxResults;
        this.luceneSearcher.setMaxResults(maxResults);
    }

    /**
     * Implements a console-based search interface.
     * Repeatedly prompts the user for a search query, performs the search,
     * and prints formatted results to the console.
     */
    public void runText() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your query (fielded searches like author:Smith supported).");
        System.out.println("Press enter with an empty query to exit.");
        System.out.println("-------------------------------------------------");

        while (true) {
            System.out.println("Enter *help* for query syntax guide");
            System.out.println("Enter *ChangeMode* to enable wildcard search for non analyzed fields");
            System.out.println("-------------------------------------------------");
            System.out.print("Enter your query: ");
            String searchQuery = scanner.nextLine();
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                break;
            }
            try {
                String result = searchIndex(searchQuery);
                System.out.println(result);
            } catch (IOException | ParseException | InvalidTokenOffsetsException e) {
                e.printStackTrace();
            }
        }
        scanner.close();
    }

    /**
     * Searches the index for the given query string.
     *
     * <p>
     * This method uses QueryManager to process the user query, which leverages the custom
     * MultiFieldQueryParser (with a PerFieldAnalyzerWrapper) to build a Lucene Query and to identify
     * the requested fields. The built query is then executed by LuceneSearcher, and the TopDocs result is
     * processed by Results to produce a formatted output string.
     * </p>
     *
     * @param searchQuery The search query string.
     * @return A formatted String with search results.
     * @throws IOException
     * @throws ParseException
     * @throws InvalidTokenOffsetsException
     */
    public String searchIndex(String searchQuery) throws IOException, ParseException, InvalidTokenOffsetsException {
        // Initialize QueryManager with the user's query.
        if (searchQuery.equals("*help*")) {
            return "Valid queries are of the form: <field>:<value>, <field>:<value> AND <field>:<value>, \"<value>\" for literal searches, or <value> (default search includes all fields).";
        }
        if (searchQuery.equals("*ChangeMode*")) {
            isLenientQuery = !isLenientQuery;
            return isLenientQuery ? "Wild card search mode enabled for non analyzed fields." : "Wild card search mode disabled.";
        }

        luceneSearcher.open();
        queryManager.setQueryMode(!isLenientQuery);
        queryManager.setSearchQuery(searchQuery);
        queryManager.process();

        System.out.println("");// new line because it looks better
        System.out.println("Query field's used: "+ queryManager.findRequestedFields());
        if (!queryManager.isValid()) {
            return "Invalid query. Enter *help* for guidance on valid queries.";
        }
        // Retrieve the built Lucene Query.
        Query query = queryManager.getBuiltQuery();
        
        // Get the set of requested fields (either detected from fielded syntax or default to all fields).
        Set<String> requestedFields = queryManager.findRequestedFields();

        // Create a LuceneSearcher instance.
        TopDocs topDocs = luceneSearcher.search(query);

        // Process the TopDocs and build a formatted results output.
        Results resultsOutput = Results.fromTopDocs(luceneSearcher.getIndexSearcher(), query, topDocs, queryManager.getPerFieldAnalyzer(), requestedFields, explain);
        
        luceneSearcher.close();
        return resultsOutput.getTotalHits() != 0 ? resultsOutput.toString() : "No results found.";
    }
}
