import java.io.PrintStream;
import java.util.Random;
import net.jroush.profiling.SizeOf;

public abstract class AlgorithmTests {

    protected final Random rand = new Random();

    public abstract Clustering getClustering(DataSet data, int clCnt,
            double clRad);

    public DataGenerator.GeneratedData genData(int attrCnt, int tupCnt,
            int clCnt, double clRad) {
        DataGenerator g = new DataGenerator(rand, attrCnt);
        g.add(g.whiteNoiseBox().density(0.05));
        g.addGroup(clCnt, g.normalSphere(), g.whiteNoiseBox().size(1 - clRad),
                g.normalSphere().pos(clRad).size(clRad / 5));
        return g.generate(tupCnt);
    }

    public void tuplePerf(PrintStream p, int attrCnt, int maxtup, int clCnt,
            int maxrep, double maxtime) {

        // for efficiency, we pre-generate one huge data set
        double clrad = 0.5 / clCnt;
        DataGenerator.GeneratedData g = genData(attrCnt, maxtup, clCnt, clrad);

        // utility for memory useage analysis
        SizeOf sizeof = new SizeOf();
        // ignore generated data set and any objects that it owns
        sizeof.ignoredObjects.addAll(sizeof.deepSizeMap(g).entrySet());

        // description and header
        p.println("# Performance Testing - Tuple Count");
        p.println("TupleCount\tRepeats\tTime(ns)\tTimePerRep(ns)\tMem(bytes)");

        // repeat testing
        for (int tupcnt = 100; tupcnt <= maxtup; //
        tupcnt = (int) Math.ceil(tupcnt * 1.2)) {
            // slice the data set down to the appropriate tuple count
            DataSet dataSlice = new DataSet(tupcnt, attrCnt, g.data.flatData);
            p.print(tupcnt);
            p.print('\t');

            // repeatedly cluster the data to get an average runtime
            Clustering cl = null;
            long begin = System.nanoTime();
            long end = begin + (long) (maxtime * 1e9 + 0.5);
            int rep = 0;
            for (; rep < maxrep && System.nanoTime() < end; rep++) {
                cl = getClustering(dataSlice, clCnt, clrad);
            }
            long dur = System.nanoTime() - begin;
            p.print(rep);
            p.print('\t');
            p.print(dur);
            p.print('\t');
            p.format("%.0f", dur / (double) rep);
            p.print('\t');

            // measure memory usage
            p.print(sizeof.deepSize(cl));

            p.println();
            if (rep == 1) break;
        }
    }

    public void attrPerf(PrintStream p, int maxAttr, int tupCnt, int clCnt,
            int maxrep, double maxtime) {

        double clrad = 0.5 / clCnt;

        // description and header
        p.println("# Performance Testing - Attribute Count");
        p.println("AttrCount\tRepeats\tTime(ns)\tTimePerRep(ns)\tMem(bytes)");

        // repeat testing
        for (int attrcnt = 1; attrcnt <= maxAttr; attrcnt++) {
            // generate a new random data set
            DataGenerator.GeneratedData g = genData(attrcnt, tupCnt, clCnt,
                    clrad);
            p.print(attrcnt);
            p.print('\t');

            // utility for memory useage analysis
            SizeOf sizeof = new SizeOf();
            sizeof.ignoredObjects.addAll(sizeof.deepSizeMap(g).keySet());

            // repeatedly cluster the data to get an average runtime
            Clustering cl = null;
            long begin = System.nanoTime();
            long end = begin + (long) (maxtime * 1e9 + 0.5);
            int rep = 0;
            for (; rep < maxrep && System.nanoTime() < end; rep++) {
                cl = getClustering(g.data, clCnt, clrad);
            }
            long dur = System.nanoTime() - begin;
            p.print(rep);
            p.print('\t');
            p.print(dur);
            p.print('\t');
            p.format("%.0f", dur / (double) rep);
            p.print('\t');

            // measure memory usage
            p.print(sizeof.deepSize(cl));

            p.println();
            if (rep == 1) break;
        }
    }

    public void clusPerf(PrintStream p, int attrCnt, int tupCnt, int maxclus,
            int maxrep, double maxtime) {

        // description and header
        p.println("# Performance Testing - Cluster Count");
        p.println("ClusterCount\tRepeats\tTime(ns)\tTimePerRep(ns)\tMem(bytes)");

        // repeat testing
        for (int cluscnt = 1; cluscnt <= maxclus; //
        cluscnt = (int) Math.ceil(cluscnt * 1.2)) {
            // generate a new random data set
            double clrad = 0.5 / cluscnt;
            DataGenerator.GeneratedData g = genData(attrCnt, tupCnt, cluscnt,
                    clrad);
            p.print(cluscnt);
            p.print('\t');

            // utility for memory useage analysis
            SizeOf sizeof = new SizeOf();
            sizeof.ignoredObjects.addAll(sizeof.deepSizeMap(g).keySet());

            // repeatedly cluster the data to get an average runtime
            Clustering cl = null;
            long begin = System.nanoTime();
            long end = begin + (long) (maxtime * 1e9 + 0.5);
            int rep = 0;
            for (; rep < maxrep && System.nanoTime() < end; rep++) {
                cl = getClustering(g.data, cluscnt, clrad);
            }
            long dur = System.nanoTime() - begin;
            p.print(rep);
            p.print('\t');
            p.print(dur);
            p.print('\t');
            p.format("%.0f", dur / (double) rep);
            p.print('\t');

            // measure memory usage
            p.print(sizeof.deepSize(cl));

            p.println();
            if (rep == 1) break;
        }
    }

    public void quality(PrintStream p, int maxattr, int maxtup, int maxclus) {

        // description and header
        p.println("# Quality Testing");
        p.print("AttrCount\tTupleCount\tClusterCount\t");
        p.print("FoundClusters\t");
        p.print("B3Precision\tB3Recall\t");
        p.println("Noise\tCompactness\tSeparation\tSilhouette");

        for (int attrcnt = 1; attrcnt <= maxattr; attrcnt++) {
            for (int cluscnt = 1; cluscnt <= maxclus; //
            cluscnt = (int) Math.ceil(cluscnt * 1.5)) {

                // generate a new random data set
                double clrad = 0.5 / cluscnt;
                DataGenerator.GeneratedData g = genData(attrcnt, maxtup,
                        cluscnt, clrad);

                for (int tupcnt = 100; tupcnt <= maxtup; //
                tupcnt = (int) Math.ceil(tupcnt * 1.5)) {
                    // slice the data set down to the appropriate tuple count
                    DataSet dataSlice = new DataSet(tupcnt, attrcnt,
                            g.data.flatData);
                    p.print(attrcnt);
                    p.print('\t');
                    p.print(tupcnt);
                    p.print('\t');
                    p.print(cluscnt);
                    p.print('\t');

                    // analysis
                    Clustering cl = getClustering(dataSlice, cluscnt, clrad);
                    p.print(cl.clusterCount());
                    p.print('\t');
                    BCubed bc = new BCubed(g, cl); // automatically slices
                    p.format("%.5f\t%.5f\t", bc.precision, bc.recall);
                    Silhouette sil = new Silhouette(g.data, cl);
                    p.format("%.5f\t%.5f\t", sil.noise, sil.compactness);
                    p.format("%.5f\t%.5f\t", sil.separation, sil.silhouette);

                    p.println();
                }
            }
        }
    }
}
