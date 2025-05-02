#!/usr/bin/env zsh

CSV_FILE="DataVisualization/GutIndexExistsVSUpdated.csv"
INDEX_DIR="indexData"

echo "Type,IndexingTime" > "$CSV_FILE"

# Helper function: run a class, grab the last numeric line, append to CSV
run_and_record() {
  local TYPE=$1
  local CLASS=$2
  
  local TIME
  TIME=$(
    java -cp "jars/*:." "$CLASS" \
      | grep -E '^[0-9]+' \
      | tail -n1
  )
  echo "${TYPE},${TIME}" >> "$CSV_FILE"  
}

for i in {1..10}; do
  run_and_record default  src.Indexers.TextFileIndexer
done

for i in {1..10}; do
  run_and_record parallel src.Indexers.TextFileIndexerParallel
done

for i in {1..10}; do
  run_and_record batch src.Indexers.TextFileIndexerPBatch
done

echo "Done: Results written to $CSV_FILE"
