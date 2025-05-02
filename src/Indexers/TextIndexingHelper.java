package src.Indexers;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.*;
import java.util.Locale;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * <h1>TextIndexingHelper</h1>
 * Utility class housing all low‑level text‑processing routines used by <p>
 * {@link TextFileIndexer}:<p>
 * {@link TextFileIndexerParallel}:<p>
 * {@link TextFileIndexerPBatch}:<p>
 * <p>It exposes the following methods:</p>
 * <ul>
 *   <li>{@link #applyStemming(String)}</li>
 *   <li>{@link #removeStopWords(String)}</li>
 *   <li>{@link #createDocument(File, boolean)}</li>
 * </ul>
 * <p>It also exposes shared resources (global {@link StandardAnalyzer}, cached
 * stop‑word set and the read‑buffer size) so that they can be reused application‑wide.</p>
 */
public final class TextIndexingHelper {

    /** Shared analyzer instance <p> <b>thread safe</b> */
    public static final ThreadLocal<StandardAnalyzer> THREAD_LOCAL_ANALYZER = ThreadLocal.withInitial(() -> new StandardAnalyzer());

    /** Cached, immutable stop‑word set pulled from <p{@link #ANALYZER}. */
    private static final ThreadLocal<CharArraySet> THREAD_LOCAL_STOP_WORDS = ThreadLocal.withInitial(() -> 
    THREAD_LOCAL_ANALYZER.get().getStopwordSet());

    /** public accessor for the stop‑word set <p> <b>thread safe</b>*/
    public static CharArraySet getStopWords() {
        return THREAD_LOCAL_STOP_WORDS.get();
    }

    public static final int BUFFER_SIZE = 65536; // 64 KB

    public static final String GUTENBERG_START = "START OF THE PROJECT GUTENBERG EBOOK";
    public static final String GUTENBERG_END   = "END OF THE PROJECT GUTENBERG EBOOK";

    /**
     * Lower‑cases, stems (Porter) and recombines the input string.
     *
     * @param text raw text
     * @return space‑separated stemmed tokens
     */
    public static String applyStemming(String text) throws IOException {
        StandardAnalyzer ANALYZER = THREAD_LOCAL_ANALYZER.get();
        TokenStream ts = new PorterStemFilter(ANALYZER.tokenStream("", new StringReader(text)));
        StringBuilder sb = new StringBuilder(text.length());
        CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) sb.append(term.toString()).append(' ');
        ts.end();
        ts.close();
        return sb.toString().trim();
    }

    /**
     * Removes default stop words via simple split & membership test.
     */
    public static String removeStopWords(String text) {
        CharArraySet stopWords = getStopWords();
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        for (String tok : text.split("\\W+")) {
            if (!tok.isEmpty() && !stopWords.contains(tok.toLowerCase(Locale.ROOT))) {
                sb.append(tok).append(' ');
            }
        }
        return sb.toString().trim();
    }

    /**
     * Parses a single <code>.txt</code> file, extracts metadata, generates derived text
     * representations and packages everything into a Lucene {@link Document}.
     *
     * @param isGutenberg when <code>true</code> only index the content between Gutenberg markers
     */
    public static Document createDocument(File file, boolean isGutenberg) throws IOException {
        boolean inBlock = false;
        String title = "";
        String author = "";
        StringBuilder content = new StringBuilder(BUFFER_SIZE);
        try (FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis), BUFFER_SIZE)) {
           String line;
           while ((line = br.readLine()) != null) {
               if (author.isEmpty() && line.contains("Author:") ) {
                   author = line.substring(line.indexOf("Author:") + "Author:".length()).trim();
                   continue;
               }
               if (title.isEmpty() && line.contains("Title:")) {
                   title = line.substring(line.indexOf("Title:") + "Title:".length()).trim();
                   continue;
               }

               if (isGutenberg) {
                   if (line.contains("START OF THE PROJECT GUTENBERG EBOOK")) { inBlock = true; continue; }
                   if (line.contains("END OF THE PROJECT GUTENBERG EBOOK"))   { break; }
               }
               if (inBlock || !isGutenberg) content.append(line).append('\n');
           }
       }
        String cont = content.toString();
        String stem = applyStemming(cont);
        String stop = removeStopWords(cont);

        Document d = new Document();
        d.add(new TextField("content",  cont,  Field.Store.YES));
        d.add(new TextField("stem",     stem, Field.Store.YES));
        d.add(new TextField("stop",     stop, Field.Store.YES));
        d.add(new StringField("author",   author,                            Field.Store.YES));
        d.add(new StringField("title",    title.isEmpty() ? file.getName() : title, Field.Store.YES));
        d.add(new StringField("filename", file.getName(),                    Field.Store.YES));
        d.add(new StringField("filepath", file.getAbsolutePath(),            Field.Store.YES));
        d.add(new StringField("modified", Long.toString(file.lastModified()), Field.Store.YES));

        return d;
    }

    // Only for Benchmarking
    public static void testFileReadingOnly(File file) {
        try {
            long startTime = System.currentTimeMillis();
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line).append('\n');
                }
            }
            
            long endTime = System.currentTimeMillis();
            System.out.println("File: " + file.getName() + 
                               ", Size: " + file.length() + " bytes, " +
                               "Read time: " + (endTime - startTime) + "ms");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    // prevents instantiation
    private TextIndexingHelper() {}
}