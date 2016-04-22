import java.io.PrintStream;

/**
 * Base class for clustering algorithms.
 */
public abstract class Clustering {

    /* 
     * Store all heap-allocated objects in member variables so we can 
     * measure the memory usage of the algorithm.
     */

    /** Compute the clusters for a given data set. */
    protected Clustering(DataSet data) {
        /*
         * Subclasses should have a constructor like this one that actually
         * implements their clustering algorithm.  It should take a DataSet
         * as the first parameter, but it can take other parameters as well.
         * 
         * The end result of the constructor should be a mapping of
         * data tuples to cluster IDs (see clusterID()).
         * 
         * If you allocate memory from the heap (i.e. with "new") then you
         * should store it in a member variable.  For example, don't 
         * do this:
         *      int[] mapping = new int[data.tupleCnt];
         * instead do this:
         *      private final int[] mapping;  // a class member variable
         *      ...
         *      mapping = new int[data.tupleCnt]; // initialize in your constructor
         * Then we can use some JVM hacks to automatically track how much memory
         * your algorithm used.
         */
    }

    /** Return the total number of tuples */
    public abstract int tupleCount();

    /**
     * Return the total number of non-noise clusters found which contain at
     * least one tuple.
     */
    public abstract int clusterCount();

    /**
     * Return which cluster a tuple belongs to.
     * 
     * @param tuple The tuple index: {@code 0 <= tuple < tupleCount}
     * 
     * @return The cluster ID : {@code 0 <= clusterID <= clusterCount}
     *         <p>
     *         IDs can be assigned to clusters in arbitrary order so long as
     *         each cluster has a unique ID in the range [1,cluster_count] and
     *         the tuples in each same cluster all have the same ID.
     *         <p>
     *         The cluster ID zero is reserved for "unclustered" or "noise"
     *         tuples, which the algorithm has failed to associate with any
     *         cluster.
     */
    public abstract int clusterID(int tuple);

    /**
     * Return a human-readable string description of the clustering, including
     * the algorithm and a summary of the parameters used.
     * <p>
     * May (but does not have to) be detailed enough to reproduce the clustering
     * from scratch. Should fit on one line.
     */
    @Override public abstract String toString();

    /**
     * Print a multi-line textual representation of the clustering.
     */
    public final void print(PrintStream p) {
        // description
        p.print("# ");
        p.println(toString());

        // header row
        p.print("TupleID");
        p.print('\t');
        p.print("ClusterID(");
        p.print(clusterCount());
        p.println(')');

        // tuples
        for (int t = 0; t < tupleCount(); t++) {
            p.print(t + 1);
            p.print('\t');
            p.print(clusterID(t));
            p.println();
        }
        p.flush();
    }
}
