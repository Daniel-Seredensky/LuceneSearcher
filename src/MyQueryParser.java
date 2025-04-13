package src;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import java.util.Set;

/** 
 * My custom query parser that extends MultiFieldQueryParser. 
 * It provides custom query generation logic. For analyzed fields it uses the standard implementation of getFieldQuery, 
 * while for non-analyzed fields it uses a Term Query or (if specified) a Wildcard Query.
 * 
 * @author Daniel, Oliwia, Ojo, William
 */

public class MyQueryParser extends MultiFieldQueryParser {
    private boolean isFirstQuery;
    private final Set<String> nonAnalyzedFields;

    public MyQueryParser(String[] fields, Analyzer analyzer, boolean isFirstQuery, Set<String> nonAnalyzedFields) {
        super(fields, analyzer);
        this.isFirstQuery = isFirstQuery;
        this.nonAnalyzedFields = nonAnalyzedFields;
    }

    public void setIsFirstQuery(boolean isFirstQuery) {
        this.isFirstQuery = isFirstQuery;
    }

    @Override
    protected Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
        // For non-analyzed fields, build an exact match (first pass) or a wildcard query (fallback)
        if (nonAnalyzedFields.contains(field)) {
            // For the first query, use an exact match; for subsequent queries, use a wildcard query.
            return isFirstQuery ? new TermQuery(new Term(field, queryText)) : new WildcardQuery(new Term(field, "*" + queryText + "*"));
        }
        // For analyzed fields, let the default implementation handle it.
        return super.getFieldQuery(field, queryText, quoted);
    }
}
