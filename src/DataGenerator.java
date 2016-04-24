import java.util.*;

public class DataGenerator {

    private final Random          rng;

    private final int             attrc;

    private final List<Generator> generators;

    public DataGenerator(Random rng, int attrCnt) {
        this.rng = rng;
        this.attrc = attrCnt;
        this.generators = new ArrayList<>();
    }

    public DataGenerator add(Generator... generators) {
        this.generators.addAll(Arrays.asList(generators));
        return this;
    }

    public DataGenerator addGroup(int count, Generator template,
            Generator centers, Generator sizes) {
        double[] scratch = new double[2 * attrc];
        for (int i = 0; i < count; i++) {
            Generator g = template.clone();
            centers.generate(scratch, 0);
            sizes.generate(scratch, attrc);
            for (int k = 0; k < attrc; k++) {
                g.pos[k] += scratch[k];
                g.size[k] *= scratch[attrc + k];
            }
            generators.add(g);
        }
        return this;
    }

    public GeneratedData generate(int tupleCnt) {
        // gather cluster max densities
        double totGenPop = 0;
        for (Generator g : generators) {
            totGenPop += g.population();
        }

        // build mapping for pulling a random generator
        int gencnt = generators.size();
        int[] genClId = new int[gencnt];
        int totClCnt = 0;
        int[] genRnd = new int[gencnt];
        int totTupCnt = 0;
        for (int g = 0; g < gencnt; g++) {
            genClId[g] = totClCnt;
            totClCnt += generators.get(g).clusterCount();
            double adjPop = generators.get(g).population() / totGenPop;
            totTupCnt += (int) Math.round(tupleCnt * adjPop);
            genRnd[g] = totTupCnt;
        }
        genRnd[gencnt - 1] = tupleCnt;

        // generate data
        double[] flatData = new double[tupleCnt * attrc];
        int[] tupMapping = new int[tupleCnt];
        for (int t = 0, toff = 0; t < tupleCnt; t++, toff += attrc) {
            int g = Arrays.binarySearch(genRnd, rng.nextInt(tupleCnt));
            if (g < 0) g = -g - 1;
            int cl = generators.get(g).generate(flatData, toff);
            tupMapping[t] = cl > 0 ? genClId[g] + cl : 0;
        }
        DataSet data = new DataSet(tupleCnt, attrc, flatData);

        return new GeneratedData(data, tupMapping, totClCnt);
    }

    public class GeneratedData extends Clustering {

        public final DataSet data;

        private final int[]  tupMapping;

        private final int    clCnt;

        private GeneratedData(DataSet data, int[] tupMapping, int clCnt) {
            super(data);
            this.data = data;
            this.tupMapping = tupMapping;
            this.clCnt = clCnt;
        }

        @Override public int tupleCount() {
            return tupMapping.length;
        }

        @Override public int clusterCount() {
            return clCnt;
        }

        @Override public int clusterID(int tuple) {
            return tupMapping[tuple];
        }

        @Override public String toString() {
            StringBuilder desc = new StringBuilder("GeneratedData ");
            desc.append(attrc);
            desc.append("D ");
            for (Generator g : generators) {
                if (desc.length() > 0) desc.append(", ");
                desc.append(g.toString());
            }
            return desc.toString();
        }
    }

    public abstract class Generator implements Cloneable {

        protected double[] pos;

        protected double[] size;

        protected double   density;

        protected Generator() {
            this.pos = new double[attrc];
            this.size = new double[attrc];
            Arrays.fill(size, 1);
            this.density = 1.0;
        }

        @Override protected Generator clone() {
            try {
                Generator g = (Generator) super.clone();
                g.pos = this.pos.clone();
                g.size = this.size.clone();
                return g;
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("Impossible");
            }
        }

        public Generator pos(double... x) {
            if (x.length == 0 || attrc % x.length != 0)
                throw new IllegalArgumentException("Position must have "
                        + attrc + " elements or an integer fraction therof.");
            for (int k = 0; k < attrc;) {
                for (int i = 0; i < x.length && k < attrc; i++, k++) {
                    pos[k] = x[i];
                }
            }
            return this;
        }

        public Generator size(double... s) {
            if (s.length == 0 || attrc % s.length != 0)
                throw new IllegalArgumentException("Size must have " + attrc
                        + " elements or an integer fraction therof.");
            for (int k = 0; k < attrc;) {
                for (int i = 0; i < s.length && k < attrc; i++, k++) {
                    size[k] = s[i];
                }
            }
            return this;
        }

        public Generator density(double d) {
            this.density = d;
            return this;
        }

        protected abstract int generate(double[] data, int doff);

        protected abstract double population();

        protected abstract int clusterCount();

        @Override public String toString() {
            return "(pos=" + Arrays.toString(pos) + ", size="
                    + Arrays.toString(size) + ", den=" + density + ")";
        }
    }

    public Generator whiteNoiseBox() {
        return new Generator() {

            @Override protected int generate(double[] data, int doff) {
                for (int k = 0; k < attrc; k++) {
                    double x = (rng.nextDouble() * 2 - 1) * size[k];
                    data[doff + k] = pos[k] + x;
                }
                return 0;
            }

            @Override protected double population() {
                // Calculate the volume of an N-rectangle
                double V = 1;
                for (int k = 0; k < attrc; k++) {
                    V *= 2 * size[k];
                }
                return density * V;
            }

            @Override protected int clusterCount() {
                return 0;
            }

            @Override public String toString() {
                return "WhiteNoiseBox" + super.toString();
            }
        };
    }
    
    public Generator uniformBox() {
        return new Generator() {

            @Override protected int generate(double[] data, int doff) {
                for (int k = 0; k < attrc; k++) {
                    double x = (rng.nextDouble() * 2 - 1) * size[k];
                    data[doff + k] = pos[k] + x;
                }
                return 1;
            }

            @Override protected double population() {
                // Calculate the volume of an N-rectangle
                double V = 1;
                for (int k = 0; k < attrc; k++) {
                    V *= 2 * size[k];
                }
                return density * V;
            }

            @Override protected int clusterCount() {
                return 1;
            }

            @Override public String toString() {
                return "UniformBox" + super.toString();
            }
        };
    }

    public Generator normalSphere() {
        return new Generator() {

            @Override protected int generate(double[] data, int doff) {
                // We sample each attribute independently from a normal dist.
                // This produces the same result as sampling all attributes
                // together from a multivariate normal distribution.
                for (int k = 0; k < attrc; k++) {
                    data[doff + k] = pos[k] + rng.nextGaussian() * size[k];
                }
                return 1;
            }

            @Override protected double population() {
                double V2 = 1;
                for (int k = 0; k < attrc; k++) {
                    V2 *= 2 * Math.PI * size[k];
                }
                return density * Math.sqrt(V2);
            }

            @Override protected int clusterCount() {
                return 1;
            }

            @Override public String toString() {
                return "NormalSphere" + super.toString();
            }
        };
    }

    public Generator uniformSphere() {
        return new Generator() {

            @Override protected int generate(double[] data, int doff) {
                // We sample each attribute normally (see normalSphere())
                double r2 = 0;
                for (int k = 0; k < attrc; k++) {
                    double x = rng.nextGaussian();
                    data[doff + k] = x;
                    r2 += x * x;
                }
                // We then renormalize to a new radius generated 
                // from a power-law distribution
                double u = Math.pow(rng.nextDouble(), 1.0 / attrc);
                u = (r2 > 0) ? u / Math.sqrt(r2) : 0;
                for (int k = 0; k < attrc; k++) {
                    data[doff + k] = pos[k] + data[doff + k] * u * size[k];
                }
                return 1;
            }

            @Override protected double population() {
                double V = attrc % 2 == 0 ? Math.PI : 2.0;
                for (int k = attrc; k > 2; k -= 2) {
                    V *= 2 * Math.PI / k;
                }
                for (int k = 0; k < attrc; k++) {
                    V *= size[k];
                }
                return density * V;
            }

            @Override protected int clusterCount() {
                return 1;
            }

            @Override public String toString() {
                return "UniformSphere" + super.toString();
            }
        };
    }
}
