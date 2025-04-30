package src;
/**
 * ResultItem represents a single search result.
 * 
 * @author Daniel, Oliwia, Ojo, William
 */
class ResultItem {
    private String filename;
    private String title;
    private String author;
    private String output; // Formatted output string including mandatory fields and best fragment.
    private float score;
    private boolean hasExplanation;
    private String explanation;

    public ResultItem(String filename, String title, String author, String output, float score) {
        this.filename = filename;
        this.title = title;
        this.author = author;
        this.output = output;
        this.score = score;
    }
    public ResultItem(String filename, String title, String author, String output, float score, String explanation) {
        this.filename = filename;
        this.title = title;
        this.author = author;
        this.output = output;
        this.score = score;
        this.hasExplanation = true;
        this.explanation = explanation;
    }

    public boolean hasExplanation() {
        return hasExplanation;
    }

    public String getExplanation() {
        return explanation;
    }
    
    public String getFilename() {
        return filename;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getOutput() {
        return output;
    }

    public float getScore() {
        return score;
    }

    @Override
    public String toString() {
        return output + "Score: " + score;
    }
}
