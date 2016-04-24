import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.jroush.profiling.SizeOf;

public class AlgorithmTests {

    protected final Map<String, ClusteringAlgo> algos = new HashMap<>();

    protected Random                            rand  = new Random();

    protected Path                              outputDir;

    public AlgorithmTests(String outputDir) throws IOException {
        this.outputDir = Paths.get(outputDir);
        Files.createDirectories(this.outputDir);
    }

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

    public void perfTests(double maxtime) throws IOException {
        for (Map.Entry<String, ClusteringAlgo> t : algos.entrySet()) {
            String name = t.getKey();
            ClusteringAlgo algo = t.getValue();
            tuplePerf(name, algo, 4, 100000, 10, 1000, maxtime);
            attrPerf(name, algo, 100, 1000, 10, 1000, maxtime);
            clusPerf(name, algo, 4, 1000, 100, 1000, maxtime);
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
        File file = outputDir.resolve(name + ".tupleperf").toFile();
        PrintStream p = new PrintStream(file);
        p.println("#\tPerformance Testing\t" + name);
        p.println("Tuple Count\tRepeats\tTime(ns)\tTime Per Rep(ns)\tMem(bytes)");

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
        File file = outputDir.resolve(name + ".attrperf").toFile();
        PrintStream p = new PrintStream(file);
        p.println("#\tPerformance Testing\t" + name);
        p.println("Attribute Count\tRepeats\tTime(ns)\tTime Per Rep(ns)\tMem(bytes)");

        // repeat testing
        for (int attrcnt = 1; attrcnt <= maxAttr; // 
        attrcnt = (int) Math.ceil(attrcnt * 1.1)) {
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
        File file = outputDir.resolve(name + ".clusperf").toFile();
        PrintStream p = new PrintStream(file);
        p.println("#\tPerformance Testing\t" + name);
        p.println("Cluster Count\tRepeats\tTime(ns)\tTime Per Rep(ns)\tMem(bytes)");

        // repeat testing
        for (int cluscnt = 1; cluscnt <= maxclus; //
        cluscnt = (int) Math.ceil(cluscnt * 1.1)) {
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
            quality(name, algo, 8, 2000, 20);
        }
    }

    public void quality(String name, ClusteringAlgo algo, int maxattr,
            int maxtup, int maxclus) throws IOException {

        // description and header
        File file = outputDir.resolve(name + ".quality").toFile();
        PrintStream p = new PrintStream(file);
        p.println("#\tQuality Testing\t" + name);
        p.print("Attribute Count\tTuple Count\tCluster Count\t");
        p.print("Found Clusters\t");
        p.print("B3 Precision\tB3 Recall\t");
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

    public void commonDataTest(int tupleCount, int clusterCnt)
            throws IOException {
        DataGenerator g = new DataGenerator(rand, 2);
        g.add(g.whiteNoiseBox().density(0.1));
        g.addGroup(
                clusterCnt, //
                g.normalSphere().density(1), //
                g.whiteNoiseBox().size(0.8),
                g.normalSphere().pos(0.2 / clusterCnt).size(0.05 / clusterCnt));
        DataGenerator.GeneratedData gc = g.generate(tupleCount);

        gc.data.print(bufferedFileout("testdata.dataset"));
        gc.print(bufferedFileout("testdata.truth"));

        for (Map.Entry<String, ClusteringAlgo> t : algos.entrySet()) {
            Clustering cl = t.getValue().apply(gc.data, rand, clusterCnt);
            cl.print(bufferedFileout("testdata." + t.getKey()));
        }
    }

    public void basicDataTest() throws IOException {
        DataGenerator g = new DataGenerator(rand, 2);
        g.add(g.uniformSphere().pos(-0.5, 0.5).size(0.4));
        g.add(g.uniformSphere().pos(0.5, 0.5).size(0.4));
        g.add(g.uniformSphere().pos(0.5, -0.5).size(0.4));
        g.add(g.uniformSphere().pos(-0.5, -0.5).size(0.4));
        DataGenerator.GeneratedData gc = g.generate(1000);

        gc.data.print(bufferedFileout("basicdata.dataset"));
        gc.print(bufferedFileout("basicdata.truth"));

        for (Map.Entry<String, ClusteringAlgo> t : algos.entrySet()) {
            Clustering cl = t.getValue().apply(gc.data, rand, 4);
            cl.print(bufferedFileout("basicdata." + t.getKey()));
        }
    }

    public void basicNoiseTest() throws IOException {
        DataGenerator g = new DataGenerator(rand, 2);
        g.add(g.uniformSphere().pos(-0.5, 0.5).size(0.25));
        g.add(g.uniformSphere().pos(0.5, 0.5).size(0.25));
        g.add(g.uniformSphere().pos(0.5, -0.5).size(0.25));
        g.add(g.uniformSphere().pos(-0.5, -0.5).size(0.25));
        g.add(g.whiteNoiseBox().size(1).density(0.1));
        DataGenerator.GeneratedData gc = g.generate(1500);

        gc.data.print(bufferedFileout("basicnoise.dataset"));
        gc.print(bufferedFileout("basicnoise.truth"));

        for (Map.Entry<String, ClusteringAlgo> t : algos.entrySet()) {
            Clustering cl = t.getValue().apply(gc.data, rand, 4);
            cl.print(bufferedFileout("basicnoise." + t.getKey()));
        }
    }

    public void outlierTest() throws IOException {
        DataGenerator g = new DataGenerator(rand, 2);
        g.add(g.uniformSphere().pos(0.8, 0.13).size(0.19, 0.08));
        g.add(g.uniformSphere().pos(0.8, -0.13).size(0.19, 0.08));
        g.add(g.whiteNoiseBox().pos(-0.95, 0.8).size(0.04, 0.1).density(0.15));
        g.add(g.whiteNoiseBox().pos(-0.95, -0.8).size(0.04, 0.1).density(0.15));
        DataGenerator.GeneratedData gc = g.generate(1000);

        gc.data.print(bufferedFileout("outliers.dataset"));
        gc.print(bufferedFileout("outliers.truth"));

        for (Map.Entry<String, ClusteringAlgo> t : algos.entrySet()) {
            Clustering cl = t.getValue().apply(gc.data, rand, 2);
            cl.print(bufferedFileout("outliers." + t.getKey()));
        }
    }

    public void oblongTest() throws IOException {
        DataGenerator g = new DataGenerator(rand, 2);
        g.add(g.uniformBox().pos(0, 0.3).size(0.8, 0.2));
        g.add(g.uniformBox().pos(0, -0.3).size(0.8, 0.2));
        DataGenerator.GeneratedData gc = g.generate(1000);

        gc.data.print(bufferedFileout("oblong.dataset"));
        gc.print(bufferedFileout("oblong.truth"));

        for (Map.Entry<String, ClusteringAlgo> t : algos.entrySet()) {
            Clustering cl = t.getValue().apply(gc.data, rand, 2);
            cl.print(bufferedFileout("oblong." + t.getKey()));
        }
    }

    public void concaveTest() throws IOException {
        DataGenerator g = new DataGenerator(rand, 2);
        g.add(g.uniformBox().pos(0, 0.5).size(0.7, 0.2));
        g.add(g.uniformBox().pos(0, -0.5).size(0.7, 0.2));
        g.add(g.uniformBox().pos(-0.5, 0).size(0.2, 0.3));
        g.add(g.uniformBox().pos(0.3, 0).size(0.4, 0.1).density(1.2));
        DataGenerator.GeneratedData gc = g.generate(1000);

        gc.data.print(bufferedFileout("concave.dataset"));
        gc.print(bufferedFileout("concave.truth"));

        for (Map.Entry<String, ClusteringAlgo> t : algos.entrySet()) {
            Clustering cl = t.getValue().apply(gc.data, rand, 2);
            cl.print(bufferedFileout("concave." + t.getKey()));
        }
    }

    private PrintStream bufferedFileout(String name) throws IOException {
        File file = outputDir.resolve(name).toFile();
        OutputStream fout = new FileOutputStream(file);
        return new PrintStream(new BufferedOutputStream(fout), false);
    }
}
