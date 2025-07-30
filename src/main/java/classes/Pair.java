package classes;

public class Pair {
    public final int r;  // row
    public final int c;  // column

    public Pair(int r, int c) { this.r = r; this.c = c; }

    // Equality check based on row and column values
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair other = (Pair) o;
        return r == other.r && c == other.c;
    }

    // Hashcode used for HashMap and HashSet storage, based on row and column
    @Override public int hashCode() {
        return 31 * r + c;
    }

    // To string for debugging purposes, shows pair as (r,c)
    @Override public String toString() { return "("+r+","+c+")"; }
}
