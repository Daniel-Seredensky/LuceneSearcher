/**
 * ResultItem represents a single search result.
 */
class ResultItem {
    private String filename;
    private String title;
    private String author;
    private String output; // Formatted output string including mandatory fields and best fragment.
    private float score;

    public ResultItem(String filename, String title, String author, String output, float score) {
        this.filename = filename;
        this.title = title;
        this.author = author;
        this.output = output;
        this.score = score;
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
