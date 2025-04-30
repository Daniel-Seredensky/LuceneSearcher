#!/usr/bin/env zsh

CSV_FILE="CIndexNotExistsVS.csv"
INDEX_DIR="indexCranfield" 

echo "Type,IndexingTime" > "$CSV_FILE"

# Helper: run a class, grab its last numeric line, append to CSV
run_and_record() {
  local TYPE=$1
  local CLASS=$2

  rm -rf "${INDEX_DIR:?}/"*

  local TIME
  TIME=$(
    java -cp "jars/*:." "$CLASS" \
      | grep -E '^[0-9]+' \
      | tail -n1
  )

  echo "${TYPE},${TIME}" >> "$CSV_FILE"
  
  sleep 2 
}

for i in {1..25}; do
  run_and_record default src.Indexers.TextFileIndexer
done

for i in {1..25}; do
  run_and_record parallel src.Indexers.TextFileIndexerParallel
done

for i in {1..25}; do
  run_and_record batch src.Indexers.TextFileIndexerPBatch
done

echo "Done: Results written to $CSV_FILE"
