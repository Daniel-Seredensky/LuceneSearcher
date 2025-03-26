import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * QueryManager processes a Lucene query string and builds a Lucene Query object
 * by delegating parsing to a CustomMultiFieldQueryParser that uses a PerFieldAnalyzerWrapper.
 *
 * <p>
 * The design leverages Lucene's built-in Boolean logic and fielded query parsing.
 * A custom parser is used to generate different query types for analyzed vs. non-analyzed fields.
 * If the initial query (isFirstQuery=true) returns no results, the caller may reinitialize the
 * parser with isFirstQuery=false to generate a more lenient query.
 * </p>
 * 
 * @version March 2025
 * @author Daniel Seredensky
 */
public class QueryManager {
    // Define the fields.

    // Fields that are not tokenized (analyzed)
    private static final Set<String> NON_ANALYZED_FIELDS = new HashSet<>(
            Arrays.asList("filename", "filepath", "modified", "author", "title")
    );
    // All searchable fields (both analyzed and non-analyzed).
    private static final String[] ALL_FIELDS = new String[]{
            "content", "stemcontent", "stopcontent", "author", "title", "filename", "filepath", "modified"
    };
    
    //singleton instance 
    public static QueryManager shared = new QueryManager();

    private String searchQuery;
    private boolean validQuery;
    private Query builtQuery; // The composite Lucene Query built by the parser

    // Flag controlling query-generation behavior.
    // True: first-pass queries (e.g. exact match for non-analyzed fields, case-sensitive prefix for analyzed fields).
    // False: fallback queries (e.g. wildcard for non-analyzed fields, case-insensitive prefix for analyzed fields)
    private boolean isFirstQuery = true;
    // The per-field analyzer 
    private Analyzer perFieldAnalyzer;

    /**
     * Constructs a QueryManager 
     *
     */
    private QueryManager() {
        this.validQuery = false;
        this.searchQuery = null;
        this.builtQuery = null;
        // Build per-field analyzers: use KeywordAnalyzer for non-analyzed fields.
        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        for (String field : NON_ANALYZED_FIELDS) {
            perFieldAnalyzers.put(field, new KeywordAnalyzer());
        }
        // The default analyzer is StandardAnalyzer (for analyzed fields).
        Analyzer defaultAnalyzer = new StandardAnalyzer();
        this.perFieldAnalyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
    }

    /**
     * Setter to update the search query.
     * @param searchQuery the new search query.
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        this.validQuery = false;
        this.builtQuery = null;
    }

    public Analyzer getPerFieldAnalyzer() {
        return this.perFieldAnalyzer;
    }

    /**
     * Processes the search query by delegating to the MyQueryParser.
     * This method leverages the PerFieldAnalyzerWrapper to ensure non-analyzed fields are handled
     * with a KeywordAnalyzer and analyzed fields use the StandardAnalyzer.
     *
     * <p>
     * If parsing succeeds, the query is marked as valid and stored in builtQuery.
     * </p>
     */
    public void process() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            validQuery = false;
            return;
        }
        try {
            // Initialize the custom parser with our list of fields, the per-field analyzer, and the isFirstQuery flag.
            MyQueryParser parser = new MyQueryParser(ALL_FIELDS, perFieldAnalyzer, isFirstQuery, NON_ANALYZED_FIELDS);
            // Parse the query. The parser will handle Boolean operators and fielded syntax.
            builtQuery = parser.parse(searchQuery);
            validQuery = true;
        } catch (ParseException e) {
            validQuery = false;
            builtQuery = null;
        }
    }

    /**
     * Returns the built Lucene Query.
     *
     * @return the Lucene Query if the query is valid; otherwise, null.
     */
    public Query getBuiltQuery() {
        return builtQuery;
    }

    /**
     * Returns whether the processed query is valid.
     *
     * @return true if the query is valid; false otherwise.
     */
    public boolean isValid() {
        return validQuery;
    }

    /**
     * Sets the query mode.
     *
     * <p>
     * This flag controls how the CustomMultiFieldQueryParser builds queries for each field.
     * True uses the first-pass strategy (exact matches for non-analyzed fields, case-sensitive prefix for analyzed fields);
     * false uses a more lenient fallback (wildcard queries for non-analyzed fields, case-insensitive prefix for analyzed fields).
     * </p>
     *
     * @param isFirstQuery the query mode flag.
     */
    public void setQueryMode(boolean isFirstQuery) {
        this.isFirstQuery = isFirstQuery;
        // Invalidate the current built query so that process() will rebuild it.
        this.validQuery = false;
        this.builtQuery = null;
    }

    /**
     * Helper method to determine which fields are requested in the user's query.
     * It iterates over ALL_FIELDS and checks if the query (case-insensitively) contains "field:".
     * If no field-specific tokens are found, it defaults to ALL_FIELDS.
     *
     * @return a Set of field names that were explicitly requested.
     */
    public Set<String> findRequestedFields() {
        Set<String> fieldsFound = new HashSet<>();
        String lowerQuery = searchQuery.toLowerCase();
        for (String field : ALL_FIELDS) {
            if (lowerQuery.contains(field.toLowerCase() + ":")) {
                fieldsFound.add(field);
            }
        }
        if (fieldsFound.isEmpty()) {
            // Default to all fields if no field-specific search was detected.
            fieldsFound.addAll(Arrays.asList(ALL_FIELDS));
        }
        return fieldsFound;
    }
}