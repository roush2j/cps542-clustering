/**
 * Base class for clustering algorithms.
 */
public abstract class Clustering {
    
    /* 
     * Store all heap-allocated objects in member variables so we can 
     * measure the memory usage of the algorithm.
     */
    
    /** Compute the clusters for a given data set. */
    public Clustering(DataSet data) {
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

    /** Return the total number of distinct clusters */
    public abstract int clusterCount();

    /**
     * Return which cluster a tuple belongs to.
     * 
     * @param tuple The tuple index: {@code 0 <= tuple < tuple_count}
     * 
     * @return The cluster ID.
     *         <p>
     *         Cluster IDs can be arbitrary integers so long as each cluster has
     *         a unique ID and the tuples in each same cluster all have the same
     *         ID.
     */
    public abstract int clusterID(int tuple);
}
