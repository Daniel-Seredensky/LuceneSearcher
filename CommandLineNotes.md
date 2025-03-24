# Compilation 
## note
- I use zsh

``` java
javac -cp "jars/*:." *.java  
```

# Run 
## **KWARGS**
- ***explain*** boolean flag for the lucene explanation to be shown in the result (no value needed)
- ***text*** boolean flag for the CLI to be shown instead of the GUI (no value needed)
- ***index*** String for the specific index directory to be used
- ***data***  String for the specific data directory to be used
- ***parallel*** boolean flag for the indexing and search parseing to be run in parallel (no value needed)

``` java
java -cp "jars/*:." FinalProjMain -kwarg1 value1 -kwarg2 value2
```
