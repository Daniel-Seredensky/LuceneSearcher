import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.TokenSources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Results processes TopDocs returned by the searcher and formats the output.
 *
 * <p>
 * It always extracts the key fields ("filename", "title", "author").
 * For any extra requested fields (passed in as requestedFields),
 * it uses the Explanation API to determine which field contributed most to the match,
 * then uses a Highlighter to get the best fragment for that field.
 * Additionally, if shouldExplain is true, the full explanation for the document's score is added.
 * </p>
 *
 * @version March 2025
 * @author Daniel Seredensky, Oliwia, Ojo
 */
public class Results {
    private List<ResultItem> items;
    private long totalHits;
    // Mandatory fields always included.
    private static final Set<String> MANDATORY_FIELDS = Set.of("filename", "title", "author");

    public Results() {
        this.items = new ArrayList<>();
    }

    public void addResult(ResultItem item) {
        items.add(item);
    }

    public List<ResultItem> getItems() {
        return items;
    }

    public long getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(long totalHits) {
        this.totalHits = totalHits;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ResultItem item : items) {
            sb.append(item.toString()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * Processes the TopDocs returned by the searcher and builds a Results object.
     *
     * @param searcher        The IndexSearcher used for the query.
     * @param query           The Query that was executed.
     * @param topDocs         The TopDocs result.
     * @param analyzer        The Analyzer used for indexing/highlighting.
     * @param requestedFields The set of fields requested by the user.
     * @param shouldExplain   If true, the full explanation for each document is appended.
     * @return a Results object containing formatted result items.
     * @throws IOException if an I/O error occurs.
     * @throws InvalidTokenOffsetsException if the highlighter fails.
     */
    public static Results fromTopDocs(IndexSearcher searcher, Query query, TopDocs topDocs,
                                        Analyzer analyzer, Set<String> requestedFields, boolean shouldExplain)
            throws IOException, InvalidTokenOffsetsException {
        Results results = new Results();
        results.setTotalHits(topDocs.totalHits.value);

        // Set up a highlighter 
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b>", "</b>");
        QueryScorer scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, scorer);

        int i = 0;
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            String filename = doc.get("filename");
            String title = doc.get("title");
            String author = doc.get("author");
            float score = scoreDoc.score;

            // Build base output string with mandatory fields.
            StringBuilder output = new StringBuilder();
            output.append("Result Number: ").append(i + 1).append("\n");
            output.append("Filename: ").append(filename).append("\n")
                  .append("Title: ").append(title).append("\n")
                  .append("Author: ").append(author).append("\n");

            // Determine extra fields (requested minus mandatory).
            Set<String> extraFields = new java.util.HashSet<>(requestedFields);
            extraFields.removeAll(MANDATORY_FIELDS);

            // Use the Explanation API to determine which extra field contributed most.
            Explanation explanation = searcher.explain(query, scoreDoc.doc);
            String bestField = null;
            float bestContribution = 0.0f;
            for (String field : extraFields) {
                float contribution = getFieldContribution(explanation, field);
                if (contribution > bestContribution) {
                    bestContribution = contribution;
                    bestField = field;
                }
            }

            // If a best extra field is found, use the highlighter to get its best fragment.
            if (bestField != null) {
                String fieldContent = doc.get(bestField);
                String bestFragment = "";
                if (fieldContent != null && !fieldContent.isEmpty()) {
                    bestFragment = highlighter.getBestFragment(analyzer, bestField, fieldContent);
                }
                if (bestFragment != null && !bestFragment.isEmpty()) {
                    output.append("Best Fragment from ").append(bestField)
                          .append(": ").append(bestFragment).append("\n");
                }
            }

            // If shouldExplain is true, add the full explanation.
            if (shouldExplain) {
                output.append("Full Explanation:\n")
                      .append(explanation.toString());
            }

            ResultItem item = new ResultItem(filename, title, author, output.toString(), score);
            results.addResult(item);
            i++;
        }
        return results;
    }

    /**
     * Recursively searches the Explanation tree for contributions associated with a given field.
     * This is a simplified heuristic: it checks if the explanation's description contains the field name,
     * and sums up the values from matching sub-explanations.
     *
     * @param explanation The Explanation object.
     * @param field       The field name to look for.
     * @return a float representing the total contribution for that field.
     */
    private static float getFieldContribution(Explanation explanation, String field) {
        float contribution = 0.0f;
        if (explanation.getDescription().toLowerCase().contains(field.toLowerCase())) {
            contribution += (float) explanation.getValue();
        }
        for (Explanation detail : explanation.getDetails()) {
            contribution += getFieldContribution(detail, field);
        }
        return contribution;
    }
}
