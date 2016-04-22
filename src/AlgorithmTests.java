import java.io.*;
import java.util.*;
import net.jroush.profiling.SizeOf;

public class AlgorithmTests {

    protected final Map<String, ClusteringAlgo> algos = new HashMap<>();

    protected Random                            rand  = new Random();

    public AlgorithmTests() {}

    @FunctionalInterface public static interface ClusteringAlgo {

        Clustering apply(DataSet data, Random rng, int clCnt);
    }

    public void addAlgorithm(String name, ClusteringAlgo algo) {
        algos.put(name, algo);
    }

    public DataGenerator.GeneratedData genData(int attrCnt, int tupCnt,
            int clCnt, double clRad) {
        DataGenerator g = new DataGenerator(rand, attrCnt);
        g.add(g.whiteNoiseBox().density(0.05));
        g.addGroup(clCnt, g.normalSphere(), g.whiteNoiseBox().size(1 - clRad),
                g.normalSphere().pos(clRad).size(clRad / 5));
        return g.generate(tupCnt);
    }

    public void perfTests() throws IOException {
        for (Map.Entry<String, ClusteringAlgo> t : algos.entrySet()) {
            String name = t.getKey();
            ClusteringAlgo algo = t.getValue();
            tuplePerf(name, algo, 4, 10000, 10, 10000, 1.0);
            attrPerf(name, algo, 25, 1000, 10, 10000, 1.0);
            clusPerf(name, algo, 4, 1000, 100, 10000, 1.0);
        }
    }

    public void tuplePerf(String name, ClusteringAlgo algo, int attrCnt,
            int maxtup, int clCnt, int maxrep, double maxtime)
            throws IOException {

        // for efficiency, we pre-generate one huge data set
        double clrad = 0.5 / clCnt;
        DataGenerator.GeneratedData g = genData(attrCnt, maxtup, clCnt, clrad);

        // utility for memory useage analysis
        SizeOf sizeof = new SizeOf();
        // ignore generated data set and any objects that it owns
        sizeof.ignoredObjects.addAll(sizeof.deepSizeMap(g).entrySet());

        // description and header
        PrintStream p = new PrintStream(name + ".tupleperf");
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
                cl = algo.apply(dataSlice, rand, clCnt);
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
        p.close();
    }

    public void attrPerf(String name, ClusteringAlgo algo, int maxAttr,
            int tupCnt, int clCnt, int maxrep, double maxtime)
            throws IOException {

        double clrad = 0.5 / clCnt;

        // description and header
        PrintStream p = new PrintStream(name + ".attrperf");
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
                cl = algo.apply(g.data, rand, clCnt);
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
        p.close();
    }

    public void clusPerf(String name, ClusteringAlgo algo, int attrCnt,
            int tupCnt, int maxclus, int maxrep, double maxtime)
            throws IOException {

        // description and header
        PrintStream p = new PrintStream(name + ".clusperf");
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
                cl = algo.apply(g.data, rand, cluscnt);
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
        p.close();
    }

    public void qualityTests() throws IOException {
        for (Map.Entry<String, ClusteringAlgo> t : algos.entrySet()) {
            String name = t.getKey();
            ClusteringAlgo algo = t.getValue();
            quality(name, algo, 6, 1000, 20);
        }
    }

    public void quality(String name, ClusteringAlgo algo, int maxattr,
            int maxtup, int maxclus) throws IOException {

        // description and header
        PrintStream p = new PrintStream(name + ".quality");
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
                    Clustering cl = algo.apply(dataSlice, rand, cluscnt);
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
        p.close();
    }

    public void commonDataTest() throws IOException {
        final int npts = 10000, nclus = 10;
        DataGenerator g = new DataGenerator(rand, 2);
        g.add(g.whiteNoiseBox().density(0.1));
        g.addGroup(nclus, g.normalSphere().density(1),
                g.whiteNoiseBox().size(0.8),
                g.normalSphere().pos(0.2 / nclus).size(0.05 / nclus));
        DataGenerator.GeneratedData gc = g.generate(npts);

        gc.data.print(bufferedFileout("testdata.dataset"));
        gc.print(bufferedFileout("testdata.truth"));

        for (Map.Entry<String, ClusteringAlgo> t : algos.entrySet()) {
            Clustering cl = t.getValue().apply(gc.data, rand, nclus);
            cl.print(bufferedFileout("testdata." + t.getKey()));
        }
    }

    private static PrintStream bufferedFileout(String name) throws IOException {
        return new PrintStream(new BufferedOutputStream(new FileOutputStream(
                name)), false);
    }
}
