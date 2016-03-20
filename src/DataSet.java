/**
 * A wrapper class for data sets. Each data set is a (possibly large) sequence
 * of tuples, each tuple having some (small) number of attributes.
 * <p>
 * To allow for optimizations the format is deliberately simple. The data is
 * stored in a flat array, which is directly exposed for efficient access.
 * <p>
 * There are some convenience methods for calculating distances and extracting
 * attribute values.
 */
public final class DataSet {

    /** The number of tuples in the data set. Strictly non-negative. */
    public final int      tupleCnt;

    /** The number of attributes per tuple. Strictly positive. */
    public final int      attrCnt;

    /**
     * The data set as a flat array of tuples, laid out sequentially:
     * <p>
     * {@code [tup1.attr1, tup1.attr2, ..., tup2.attr1, tup2.attr2, ...]}
     * <p>
     * The array is guaranteed to be large enough to hold all declared tuples:
     * 
     * <pre>
     * flatData.length &gt;= tupleCnt * attrCnt
     * </pre>
     * 
     * This array is exposed for efficient access, but users <b>should not
     * modify</b> it.
     */
    public final double[] flatData;

    public DataSet(int tupleCnt, int attrCnt, double[] flatData) {
        assert (flatData.length >= tupleCnt * attrCnt);
        this.tupleCnt = tupleCnt;
        this.attrCnt = attrCnt;
        this.flatData = flatData;
    }

    /** Convenience method to lookup attributes from the data set */
    public double getAttr(int tup, int attr) {
        assert tup < tupleCnt && attr < attrCnt;
        return flatData[tup * attrCnt + attr];
    }

    /**
     * Calculate the (euclidean) distance^2 between two tuples.
     * 
     * @param tupA The index of the first tuple
     * @param tupB The index of the second tuple
     */
    public double distSq(int tupA, int tupB) {
        assert tupA < tupleCnt && tupB < tupleCnt;
        final int offA = tupA * attrCnt, offB = tupB * attrCnt;
        double distSq = 0;
        for (int k = 0; k < attrCnt; k++) {
            double d = flatData[offA + k] - flatData[offB + k];
            distSq += d * d;
        }
        return distSq;
    }

    /**
     * Calculate the (euclidean) distance^2 between two tuples, where one tuple
     * is stored in an external array.
     * 
     * @param tupA The index of the first tuple, in the data set
     * @param dataB The array containing the second tuple
     * @param offB The starting offset of the second tuple in its array
     */
    public double distSq(int tupA, double[] dataB, int offB) {
        assert tupA < tupleCnt && offB < dataB.length - attrCnt;
        final int offA = tupA * attrCnt;
        double distSq = 0;
        for (int k = 0; k < attrCnt; k++) {
            double d = flatData[offA + k] - dataB[offB + k];
            distSq += d * d;
        }
        return distSq;
    }

    /**
     * Calculate the (euclidean) distance^2 between two tuples stored in
     * arbitrary arrays.
     * 
     * @param dataA An array containing the first tuple
     * @param idxA The starting offset of the first tuple in its array
     * @param dataB An array containing the second tuple
     * @param idxB The starting offset of the second tuple in its array
     * @param attrc The number of attributes in each tuple
     */
    public static double distSq(double[] dataA, int idxA, double[] dataB,
            int idxB, int attrc) {
        double distSq = 0;
        while (--attrc >= 0) {
            double d = dataA[idxA + attrc] - dataB[idxB + attrc];
            distSq += d * d;
        }
        return distSq;
    }
}
