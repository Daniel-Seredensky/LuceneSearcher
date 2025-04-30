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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TextFileIndexerPBatch mirrors the functionality of TextFileIndexer utilizes Executor Service for parallel indexing 
 * Each t
 * Batch size is fixed at 30 files per task.
 *
 * @see {@link TextFileIndexer}
 */
public class TextFileIndexerPBatch {

    private static IndexingResult mostRecentIndexingResult;

    public static IndexingResult  getMostRecentIndexingResult() {
        return mostRecentIndexingResult == null ? new IndexingResult(-1, -1, -1) : mostRecentIndexingResult;
    }

    /**
     * Entry point: same signature as TextFileIndexer.
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
            TextFileIndexer.IndexingResult result = indexTextFilesBatch(dataDirPath, indexDirPath, option, isGutenberg);
            long elapsedTime = System.currentTimeMillis() - startTime;
            result.addElapsedTime(elapsedTime);
            mostRecentIndexingResult = result;

            if (verbose) {
                System.out.println("Batch-based parallel indexing completed.");
                System.out.println("Documents added: " + result.added);
                System.out.println("Documents changed: " + result.changed);
                System.out.println("Documents removed: " + result.removed);
                System.out.println("Indexing time: " + elapsedTime + " ms");
            }
            return elapsedTime;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Indexes files in batches of BATCH_SIZE using a fixed thread pool.
     */
    public static TextFileIndexer.IndexingResult indexTextFilesBatch(String dataDirPath,
                                                   String indexDirPath,
                                                   String option,
                                                   boolean isGutenberg) throws IOException, InterruptedException {
        Directory indexDir = FSDirectory.open(Paths.get(indexDirPath));

        // Use the shared analyzer from TextIndexingHelper
        IndexWriterConfig config = new IndexWriterConfig(TextIndexingHelper.THREAD_LOCAL_ANALYZER.get());
        config.setSimilarity(new ClassicSimilarity());
        IndexWriter writer = new IndexWriter(indexDir, config);

        // Build indexDocs map (already indexed docs)
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
        
        final int COMMIT_BATCH_SIZE = isGutenberg ? 300 : 1400;
        final int BATCH_SIZE = isGutenberg ? 25 : 80;


        final Object commitLock = new Object();
        final AtomicInteger commitCounter = new AtomicInteger(0);

        // List files and partition into batches
        File dataDir = new File(dataDirPath);
        File[] files = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        if (files != null && files.length > 0) {
            List<File> fileList = Arrays.asList(files);
            List<List<File>> batches = new ArrayList<>();
            for (int i = 0; i < fileList.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, fileList.size());
                batches.add(fileList.subList(i, end));
            }

            // Create a thread pool based on available processors
            int threads = Runtime.getRuntime().availableProcessors();
            ExecutorService pool = Executors.newFixedThreadPool(threads);

            // Submit batch tasks
            for (List<File> batch : batches) {
                pool.submit(() -> {
                    for (File file : batch) {
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
                                    int count = commitCounter.incrementAndGet();
                                    if (count >= COMMIT_BATCH_SIZE) {
                                        try {
                                            writer.commit();
                                            commitCounter.set(0);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            // Shutdown pool and wait
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.HOURS);
        }

        // Handle "missing" option after all batches finish
        if (option != null && option.equals("missing")) {
            Set<String> present = Set.copyOf(currentFilePaths);
            AtomicInteger deleteCounter = new AtomicInteger(0);
            final int DELETE_BATCH_SIZE = COMMIT_BATCH_SIZE;
            
            indexDocs.keySet().parallelStream()
                .filter(fp -> !present.contains(fp))
                .forEach(fp -> {
                    try {
                        writer.deleteDocuments(new Term("filepath", fp));
                        removed.incrementAndGet();
                        
                        // Handle batch commits for deletions
                        synchronized (commitLock) {
                            int count = deleteCounter.incrementAndGet();
                            if (count >= DELETE_BATCH_SIZE) {
                                try {
                                    writer.commit();
                                    deleteCounter.set(0);
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
        return new TextFileIndexer.IndexingResult(added.get(), changed.get(), removed.get());
    }

    public static void main(String[] args) {
        String indexDir = "indexCranfield";
        String dataDir = "cranfieldSeparated";
        boolean isGutenberg = true; 
        String mode = null;
        double time = TextFileIndexerPBatch.run(dataDir,indexDir,mode,isGutenberg,false);
        System.out.println(""+time + "\n");
        System.gc();
    }
}
