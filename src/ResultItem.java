package src;
/**
 * ResultItem represents a single search result.
 * 
 * @author Daniel, Oliwia, Ojo, William
 */
public class ResultItem {
    private String filename;
    private String title;
    private String author;
    private String output; // Formatted output string including mandatory fields and best fragment.
    private float score;
    private boolean hasExplanation;
    private String explanation;
    private int resultNumber;
    private String bestFragment;
    private String bestField;

    public ResultItem(String filename, String title, String author, String bestField, String bestFragment, String output, float score,int resultNumber) {
        this.filename = filename;
        this.title = title;
        this.author = author;
        this.output = output;
        this.score = score;
        this.resultNumber = resultNumber;
        this.bestFragment = bestFragment;
        this.bestField = bestField;
    }

    public ResultItem(String filename, String title, String author, String bestField, String bestFragment, String output, float score, int resultNumber, String explanation) {
        this.filename = filename;
        this.title = title;
        this.author = author;
        this.output = output;
        this.score = score;
        this.hasExplanation = true;
        this.explanation = explanation;
        this.resultNumber = resultNumber;
        this.bestFragment = bestFragment;
        this.bestField = bestField;
    }

    public String getBestFragment() {
        return bestFragment;
    }

    public String getBestField() {
        return bestField;
    }

    public int getResultNumber() {
        return resultNumber;
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
