import java.util.Arrays;

public class Silhouette {

    public final int       tupCnt;

    /** Fraction of the tuples that are noise (and thus ignored) */
    public final double    noise;

    /** Average compactness of clusters, weighted by cluster population */
    public final double    compactness;

    /** Average separation of clusters, weighted by cluster population */
    public final double    separation;

    /** Average silhouette coef. of clusters, weighted by cluster population */
    public final double    silhouette;

    private final double[] clCmp;      // per-cluster average compactness 

    private final double[] clSep;      // per-cluster average separation 

    private final double[] clSil;      // per-cluster average silhouette coef

    public Silhouette(DataSet data, Clustering cl) {
        this.tupCnt = Math.min(data.tupleCnt, cl.tupleCount());
        assert (tupCnt > 0);
        final int clcnt = cl.clusterCount();
        assert (clcnt > 1);

        int[] pop = new int[clcnt + 1];
        for (int t = 0; t < tupCnt; t++) {
            pop[cl.clusterID(t)]++;
        }
        this.noise = pop[0] / (double) tupCnt;

        double[] cmp = this.clCmp = new double[clcnt + 1];
        double[] sep = this.clSep = new double[clcnt + 1];
        double[] sil = this.clSil = new double[clcnt + 1];
        double[] acc = new double[clcnt + 1];
        for (int t = 0; t < tupCnt; t++) {
            final int tclus = cl.clusterID(t);
            if (tclus == 0) continue;
            Arrays.fill(acc, 0);

            for (int i = 0; i < tupCnt; i++) {
                int iclus = cl.clusterID(i);
                if (iclus == 0) continue;
                double d = data.dist(t, i);
                acc[iclus] += d;
            }

            double a = pop[tclus] > 1 ? acc[tclus] / (pop[tclus] - 1) : 0;
            cmp[tclus] += a;

            double b = Double.POSITIVE_INFINITY;
            for (int c = 1; c <= clcnt; c++) {
                if (tclus == c || pop[c] == 0) continue;
                double bc = acc[c] / pop[c];
                if (bc < b) b = bc;
            }
            sep[tclus] += b;

            double s = Double.isFinite(b) ? (b - a) / Math.max(a, b) : 1.0;
            sil[tclus] += s;
        }

        double acmp = 0, asep = 0, asil = 0;   // non-noise avg stats
        for (int c = 1; c <= clcnt; c++) {
            acmp += cmp[c];
            asep += sep[c];
            asil += sil[c];
            if (pop[c] <= 1) continue;
            cmp[c] /= pop[c];
            sep[c] /= pop[c];
            sil[c] /= pop[c];
        }
        int notnoisecnt = tupCnt - pop[0];
        this.compactness = acmp = acmp / notnoisecnt;
        this.separation = asep = asep / notnoisecnt;
        this.silhouette = asil = asil / notnoisecnt;
    }

    /** Return the total number of clusters. */
    public int clusterCount() {
        return clSil.length - 1;
    }

    /** Return the compactness of a cluster */
    public double compactness(int clusterID) {
        return clCmp[clusterID];
    }

    /** Return the separation of a cluster */
    public double separation(int clusterID) {
        return clSep[clusterID];
    }

    /** Return the silhouette coef. of a cluster */
    public double silhouette(int clusterID) {
        return clSil[clusterID];
    }
}
