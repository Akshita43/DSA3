public class Suggestion implements Comparable<Suggestion> {
    public final String word;
    public final int distance;

    public Suggestion(String word, int distance) {
        this.word = word;
        this.distance = distance;
    }

    @Override
    public int compareTo(Suggestion other) {
        if (this.distance != other.distance) {
            return Integer.compare(this.distance, other.distance);
        }
        return this.word.compareTo(other.word);
    }

    @Override
    public String toString() {
        return word + "  (edit distance = " + distance + ")";
    }
}