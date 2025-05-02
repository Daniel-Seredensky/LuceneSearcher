package src.Indexers;

import org.apache.lucene.document.Document;
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

import src.Indexers.TextFileIndexer.DocumentInfo;
import src.Indexers.TextFileIndexer.IndexingResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TextFileIndexerParallel mirrors the functionality of TextFileIndexer but uses parallel streams for indexing.
 * @see {@link TextFileIndexer}
 */
public class TextFileIndexerParallel {
    private static IndexingResult mostRecentIndexingResult;

    public static IndexingResult  getMostRecentIndexingResult() {
        return mostRecentIndexingResult == null ? new IndexingResult(-1, -1, -1) : mostRecentIndexingResult;
    }

    /**
     * Replaces the main method for parallel indexing. Usage is the same as the base class.
     */
    public static double run(String dataDirPath, String indexDirPath, String option, boolean isGutenberg, boolean verbose) {
        if (dataDirPath == null || indexDirPath == null) {
            System.err.println("Usage: run <dataDirPath> <indexDirPath> [new|changed|missing]");
            return -1;
        }
        if (option != null) {
            option = option.toLowerCase();
            if (!option.equals("new") && !option.equals("changed") && !option.equals("missing")) {
                System.err.println("Error: Invalid indexing option specified. Must be one of: new, changed, missing");
                return -1;
            }
        }
        try {
            long startTime = System.currentTimeMillis();
            IndexingResult result = indexTextFilesParallel(dataDirPath, indexDirPath, option, isGutenberg);
            long elapsedTime = System.currentTimeMillis() - startTime;
            result.addElapsedTime(elapsedTime);
            mostRecentIndexingResult = result;

            if (verbose){
                System.out.println("Parallel indexing completed.");
                System.out.println("Documents added: " + result.added);
                System.out.println("Documents changed: " + result.changed);
                System.out.println("Documents removed: " + result.removed);
                System.out.println("Indexing time: " + elapsedTime + " ms");
            }
            return elapsedTime;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Performs parallel indexing of text files, mirroring the base class logic but using parallel streams.
     */
    public static IndexingResult indexTextFilesParallel(String dataDirPath, String indexDirPath, String option, boolean isGutenberg) throws IOException {
        Directory indexDir = FSDirectory.open(Paths.get(indexDirPath));
        
        // Use the shared analyzer from TextIndexingHelper
        IndexWriterConfig config = new IndexWriterConfig(TextIndexingHelper.THREAD_LOCAL_ANALYZER.get());
        config.setSimilarity(new ClassicSimilarity());
        IndexWriter writer = new IndexWriter(indexDir, config);

        // Build a map of already indexed documents (by filepath) in parallel using parallelStream
        ConcurrentHashMap<String, DocumentInfo> indexDocs = new ConcurrentHashMap<>();
        if (DirectoryReader.indexExists(indexDir)) {
            DirectoryReader reader = DirectoryReader.open(indexDir);
            reader.leaves().parallelStream().forEach(leaf -> {
                LeafReader leafReader = leaf.reader();
                Bits liveDocs = leafReader.getLiveDocs();
                for (int i = 0; i < leafReader.maxDoc(); i++) {
                    if (liveDocs != null && !liveDocs.get(i)) continue;
                    try {
                        Document doc = leafReader.document(i);
                        String filepath = doc.get("filepath");
                        long modifiedTime = Long.parseLong(doc.get("modified"));
                        indexDocs.put(filepath, new DocumentInfo(modifiedTime));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            reader.close();
        }

        AtomicInteger added = new AtomicInteger();
        AtomicInteger changed = new AtomicInteger();
        AtomicInteger removed = new AtomicInteger();
        ConcurrentLinkedQueue<String> currentFilePaths = new ConcurrentLinkedQueue<>();
        
        final int BATCH_SIZE = isGutenberg ? 300 : 1400;
        
        // Commit lock and Synchronized counter for batch commits
        final Object commitLock = new Object(); 
        final AtomicInteger batchCounter = new AtomicInteger(0);

        File dataDir = new File(dataDirPath);
        File[] files = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        double length = files.length;

        if (files != null) {
            Arrays.stream(files).parallel().forEach(file -> {
                String path = file.getAbsolutePath();
                currentFilePaths.add(path);
                DocumentInfo info = indexDocs.get(path);
                boolean documentProcessed = false;

                try {
                    if (option == null) {
                        if (info == null) {
                            writer.addDocument(TextIndexingHelper.createDocument(file, isGutenberg));
                            added.incrementAndGet();
                            documentProcessed = true;
                        } else if (file.lastModified() > info.modifiedTime) {
                            writer.updateDocument(new Term("filepath", path), 
                                                TextIndexingHelper.createDocument(file, isGutenberg));
                            changed.incrementAndGet();
                            documentProcessed = true;
                        }
                    } else if (option.equals("new")) {
                        if (info == null) {
                            writer.addDocument(TextIndexingHelper.createDocument(file, isGutenberg));
                            added.incrementAndGet();
                            documentProcessed = true;
                        }
                    } else if (option.equals("changed")) {
                        if (info != null && file.lastModified() > info.modifiedTime) {
                            writer.updateDocument(new Term("filepath", path), 
                                                TextIndexingHelper.createDocument(file, isGutenberg));
                            changed.incrementAndGet();
                            documentProcessed = true;
                        }
                    }
                    
                    // Handle batch commits in a thread-safe way
                    if (documentProcessed) {
                        synchronized (commitLock) {
                            int count = batchCounter.incrementAndGet();
                            if (count >= BATCH_SIZE) {
                                try {
                                    writer.commit();
                                    batchCounter.set(0);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        // Handle "missing" option: remove documents whose source files no longer exist.
        if (option != null && option.equals("missing")) {
            Set<String> present = Set.copyOf(currentFilePaths);
            AtomicInteger deleteBatchCounter = new AtomicInteger(0);
            
            indexDocs.keySet().parallelStream()
                .filter(fp -> !present.contains(fp))
                .forEach(fp -> {
                    try {
                        writer.deleteDocuments(new Term("filepath", fp));
                        removed.incrementAndGet();
                        
                        // Handle batch commits for deletions
                        synchronized (commitLock) {
                            int count = deleteBatchCounter.incrementAndGet();
                            if (count >= BATCH_SIZE) {
                                try {
                                    writer.commit();
                                    deleteBatchCounter.set(0);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }

        // Final commit for any remaining documents
        writer.commit();
        writer.close();
        return new IndexingResult(added.get(), changed.get(), removed.get());
    }
    
    public static void main(String[] args) {
        String indexDir = "indexData";
        String dataDir = "data";
        boolean isGutenberg = true; 
        String mode = null;
        double time = TextFileIndexerParallel.run(dataDir,indexDir,mode,isGutenberg,false);
        System.out.println(""+time + "\n");
        System.gc();
    }
}
