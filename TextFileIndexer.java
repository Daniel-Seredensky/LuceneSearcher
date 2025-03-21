import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.StringReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TextFileIndexer.java creates a Lucene index in the specified index directory for text files.
 * 
 * This version uses TF-IDF scoring (via ClassicSimilarity) and indexes the following fields:
 * <ul>
 *   <li>content: Full text (or, for Gutenberg files, only the text between
 *       *** START OF THE PROJECT GUTENBERG EBOOK and *** END OF THE PROJECT GUTENBERG EBOOK).</li>
 *   <li>stemcontent: The text after applying Porter stemming.</li>
 *   <li>stopcontent: The text after removing stop words.</li>
 *   <li>author: The author’s name (extracted from a line containing "Author:").</li>
 *   <li>title: The document title (from a line containing "Title:"; if absent, the filename is used).</li>
 *   <li>filename: The name of the .txt file.</li>
 *   <li>filepath: The full path to the .txt file.</li>
 *   <li>modified: The file’s last modified timestamp.</li>
 * </ul>
 * 
 * The run method inherets params from 
 * <pre>
 *   TextFileIndexer.run(dataDirPath, indexDirPath, mode, isGutenberg);
 * </pre>
 * 
 * @version March 2025
 * @author A cs Professor
 * @author adapted by Daniel Seredensky, Oliwia, Ojo
 */
public class TextFileIndexer {

    // Helper classes to store indexing results and document info from the index.
    static class IndexingResult {
        int added;
        int changed;
        int removed;

        public IndexingResult(int added, int changed, int removed) {
            this.added = added;
            this.changed = changed;
            this.removed = removed;
        }
    }

    static class DocumentInfo {
        long modifiedTime;
        Document doc;

        public DocumentInfo(long modifiedTime, Document doc) {
            this.modifiedTime = modifiedTime;
            this.doc = doc;
        }
    }

    /**
     * Replaces the main method. Expects the following parameters:
     * @param dataDirPath  The directory containing .txt files.
     * @param indexDirPath The directory where the index will be stored.
     * @param option       Optional indexing mode: "new", "changed", or "missing".
     * @param isGutenberg  True if files are Gutenberg-formatted.
     */
    public static void run(String dataDirPath, String indexDirPath, String option, boolean isGutenberg) {
        if (dataDirPath == null || indexDirPath == null) {
            System.err.println("Usage: run <dataDirPath> <indexDirPath> [new|changed|missing]");
            return;
        }
        
        // Validate optional indexing mode, if provided.
        if (option != null) {
            option = option.toLowerCase();
            if (!option.equals("new") && !option.equals("changed") && !option.equals("missing")) {
                System.err.println("Error: Invalid indexing option specified. Must be one of: new, changed, missing");
                return;
            }
        }
        
        try {
            long startTime = System.currentTimeMillis();
            IndexingResult result = indexTextFiles(dataDirPath, indexDirPath, option, isGutenberg);
            long elapsedTime = System.currentTimeMillis() - startTime;

            System.out.println("Indexing completed.");
            System.out.println("Documents added: " + result.added);
            System.out.println("Documents changed: " + result.changed);
            System.out.println("Documents removed: " + result.removed);
            System.out.println("Indexing time: " + elapsedTime + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * indexTextFiles indexes files from the given data directory according to the provided option.
     *
     * @param dataDirPath  Path to the directory containing .txt files.
     * @param indexDirPath Path to the directory to write the index.
     * @param option       Optional index mode: "new", "changed", or "missing" (null means index all files).
     * @param isGutenberg  If true, only index content between the Gutenberg markers.
     * @return An IndexingResult with counts for added, changed, and removed documents.
     * @throws IOException
     */
    public static IndexingResult indexTextFiles(String dataDirPath, String indexDirPath, String option, boolean isGutenberg) throws IOException {
        Directory indexDir = FSDirectory.open(Paths.get(indexDirPath));
        StandardAnalyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        // Set TF-IDF scoring via ClassicSimilarity.
        config.setSimilarity(new ClassicSimilarity());
        IndexWriter writer = new IndexWriter(indexDir, config);

        int added = 0;
        int changed = 0;
        int removed = 0;

        // Build a map of already indexed documents (by filepath)
        Map<String, DocumentInfo> indexDocs = new HashMap<>();
        if (DirectoryReader.indexExists(indexDir)) {
            DirectoryReader reader = DirectoryReader.open(indexDir);
            for (LeafReaderContext leaf : reader.leaves()) {
                LeafReader leafReader = leaf.reader();
                Bits liveDocs = leafReader.getLiveDocs();
                int maxDoc = leafReader.maxDoc();
                for (int i = 0; i < maxDoc; i++) {
                    if (liveDocs != null && !liveDocs.get(i)) continue;
                    Document doc = leafReader.document(i);
                    String filepath = doc.get("filepath");
                    String modifiedStr = doc.get("modified");
                    long modifiedTime = Long.parseLong(modifiedStr);
                    indexDocs.put(filepath, new DocumentInfo(modifiedTime, doc));
                }
            }
            reader.close();
        }

        // List all .txt files in the data directory.
        File dataDir = new File(dataDirPath);
        File[] files = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        // Build a set of file paths present in the directory for "missing" processing.
        Set<String> currentFilePaths = new HashSet<>();
        if (files != null) {
            for (File file : files) {
                currentFilePaths.add(file.getAbsolutePath());
                DocumentInfo info = indexDocs.get(file.getAbsolutePath());

                if (option == null) {
                    // Default: index all files – add if new or update if modified.
                    if (info == null) {
                        writer.addDocument(createDocument(file, isGutenberg));
                        added++;
                    } else if (file.lastModified() > info.modifiedTime) {
                        writer.updateDocument(new Term("filepath", file.getAbsolutePath()), createDocument(file, isGutenberg));
                        changed++;
                    }
                } else if (option.equals("new")) {
                    // Only add files not already in the index.
                    if (info == null) {
                        writer.addDocument(createDocument(file, isGutenberg));
                        added++;
                    }
                } else if (option.equals("changed")) {
                    // Only update files that are already indexed and have been modified.
                    if (info != null && file.lastModified() > info.modifiedTime) {
                        writer.updateDocument(new Term("filepath", file.getAbsolutePath()), createDocument(file, isGutenberg));
                        changed++;
                    }
                }
            }
        }

        // Option "missing": remove indexed documents whose files no longer exist.
        if (option != null && option.equals("missing")) {
            for (String filepath : indexDocs.keySet()) {
                if (!currentFilePaths.contains(filepath)) {
                    writer.deleteDocuments(new Term("filepath", filepath));
                    removed++;
                }
            }
        }

        writer.commit();
        writer.close();
        return new IndexingResult(added, changed, removed);
    }

    /**
     * createDocument reads the file and creates a Document with all required fields.
     * If isGutenberg is true, only the text between the Gutenberg start and end markers is indexed as content.
     *
     * @param file        The text file to index.
     * @param isGutenberg If true, extract content only between the Gutenberg markers.
     * @return A Lucene Document.
     * @throws IOException
     */
    private static Document createDocument(File file, boolean isGutenberg) throws IOException {
        Document document = new Document();
        StringBuilder contentBuilder = new StringBuilder();
        String author = "";
        String title = "";
        boolean titleFound = false;
        boolean inGutenbergBlock = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Extract author and title from any line in the file.
                if (line.contains("Author:") && author.isEmpty()) {
                    author = line.substring(line.indexOf("Author:") + "Author:".length()).trim();
                }
                if (!titleFound && line.contains("Title:")) {
                    title = line.substring(line.indexOf("Title:") + "Title:".length()).trim();
                    titleFound = true;
                }
                // If Gutenberg extraction is enabled, collect only the text between the markers.
                if (isGutenberg) {
                    if (line.contains("*** START OF THE PROJECT GUTENBERG EBOOK")) {
                        inGutenbergBlock = true;
                        continue;
                    }
                    if (line.contains("*** END OF THE PROJECT GUTENBERG EBOOK")) {
                        inGutenbergBlock = false;
                        break; // End of content block.
                    }
                    if (inGutenbergBlock) {
                        contentBuilder.append(line).append("\n");
                    }
                } else {
                    // For non-Gutenberg files, index the entire content.
                    contentBuilder.append(line).append("\n");
                }
            }
        }
        String content = contentBuilder.toString();

        // Process content for stemmed and stop-word removed versions.
        String stemcontent = applyStemming(content);
        String stopcontent = removeStopWords(content);

        // Add required fields.
        document.add(new TextField("content", content, Field.Store.YES));
        document.add(new TextField("stemcontent", stemcontent, Field.Store.YES));
        document.add(new TextField("stopcontent", stopcontent, Field.Store.YES));
        document.add(new StringField("author", author, Field.Store.YES));
        // Use title if found; otherwise default to filename.
        document.add(new StringField("title", title.isEmpty() ? file.getName() : title, Field.Store.YES));
        document.add(new StringField("filename", file.getName(), Field.Store.YES));
        document.add(new StringField("filepath", file.getAbsolutePath(), Field.Store.YES));
        document.add(new StringField("modified", Long.toString(file.lastModified()), Field.Store.YES));

        return document;
    }

    /**
     * applyStemming tokenizes the text and applies Porter stemming.
     *
     * @param text Input text.
     * @return A string with stemmed tokens.
     */
    private static String applyStemming(String text) throws IOException {
        // Create a StandardTokenizer reading from the text.
        StandardTokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(new StringReader(text));
        
        // Build a token stream with lowercasing and stemming.
        TokenStream tokenStream = new LowerCaseFilter(tokenizer);
        tokenStream = new PorterStemFilter(tokenStream);
        
        // Process the token stream.
        tokenStream.reset();
        StringBuilder sb = new StringBuilder();
        CharTermAttribute charTermAttr = tokenStream.addAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {
            sb.append(charTermAttr.toString()).append(" ");
        }
        tokenStream.end();
        tokenStream.close();
        
        return sb.toString().trim();
    }

    /**
     * removeStopWords removes stop words from the text using the analyzer's default stop set.
     *
     * @param text Input text.
     * @return A string with stop words removed.
     */
    private static String removeStopWords(String text) {
        // Obtain the default stop words from a new StandardAnalyzer.
        CharArraySet stopWords = (CharArraySet) new StandardAnalyzer().getStopwordSet();
        StringBuilder sb = new StringBuilder();
        String[] tokens = text.split("\\W+");
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            if (!stopWords.contains(token.toLowerCase())) {
                sb.append(token).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
