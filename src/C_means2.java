
public class C_means2 extends Clustering
{
	/*
	 * matrix for the M-step, note all values in the MStep array running
	 * vertically must equal to 1
	 */
	private double[][] MStep;
	// Matrix for the new centers; also used as the working centers array
	private DataSet newCenters;
	/*
	 * Matrix for the previous Centers; note, algorithm runs until
	 * new centers equals previous centers
	 * due to accuracy issues with java decimals, accuracy is determined
	 * to be at the threshold of 0.000001
	 */
	private DataSet previousCenters;
	//orignal data
	private DataSet originalData;
	//need for determining how many clusters to calculate
	private int numClusters;
	//number of tuples
	private int size;
	//number of dimensions
	private int dimensions;
	//used for calculating the distance measure
	double[] tempDist;
	
	public C_means2(DataSet data, int numCluster, double[] initialCenters) 
	{
		super(data);
		originalData = data;
		this.numClusters = numCluster;
		size = data.tupleCnt;
		dimensions = data.attrCnt;
		MStep = new double[numCluster][size];
		initializeMstep(MStep, size);
		//initialize the newCenters to hold the desired values of the initial centers
		newCenters = new DataSet(numCluster, data.attrCnt, initialCenters);
		
		//start the algorithm
		C_Algorithm();		
	}
	
	private void C_Algorithm()
	{
		//threshold for accuracy
		double threshold = 0.000001;
		//flag to be used in determining if newCenters matches previous centers
		boolean match = false;
		//used to determine of new matches old
		int counter = 1;
		//initialize variable to hold distance measure
		tempDist = new double[numClusters];
		//used for calculating the c-means value
		double div;
		//used for keeping track of run iterations
		int runNumber = 0;
		//main component of algorithm
		while(!match)
		{
			//calculate distance for each tuple
			for(int i = 0; i<size; i++)
			{
				//initialize variable
				div = 0;
				//calculate distance from center to each point
				for(int j = 0; j<numClusters; j++)
				{
					//load the tempDist array with euclidean dist values
					for(int x = 0; x<dimensions; x++)
					{
						tempDist[j] += euclideanDist(originalData.getAttr(i, x), newCenters.getAttr(j, x));
					}
					
				}
				/*
				 * calculate divisor in C-means algorithm
				 */
				for(int j = 0; j<numClusters; j++)
				{
					if(tempDist[j] != 0.0)
					{
						div += (1/tempDist[j]);
					}
				}
				/*
				 * fill the MStep array with values
				 * note, values running vertically should sum up to 1
				 */
				for(int j = 0; j<numClusters; j++)
				{
					if(tempDist[j] == 0.0)
					{
						MStep[j][i] = 1;
						for(int x = 0; x<numClusters; x++)
						{
							if(MStep[x][i] != 1.0)
							{
								MStep[x][i] = 0.0;
							}
						}
						break;
					}
					else
						MStep[j][i] = (1/tempDist[j]) / div;
				}
				for(int n = 0; n<tempDist.length; n++)
				{
					tempDist[n] = 0;
				}
			}
			/*
			 * assign new center values to previous centers
			 */
			previousCenters = newCenters;
			///////////////////////////////////
			/*
			 * calculate new centers
			 */
			double[] flatFile = new double[dimensions * numClusters];
			double[] div2 = new double[dimensions * numClusters];
			for(int i = 0; i<numClusters; i++)
			{
				double square = 0;
				for(int j=0; j<size; j++)
				{
					square = MStep[i][j] * MStep[i][j];
					for(int x=0; x<dimensions; x++)
					{
						flatFile[x + dimensions * i] += square * originalData.getAttr(j, x);
						div2[x + dimensions * i] += square;
					}
				}
			}
			for(int i = 0; i<(dimensions * numClusters); i++)
			{
				flatFile[i] = flatFile[i] / div2[i];
			}
			//assign new centers to newCenters variable
			newCenters = new DataSet(numClusters, dimensions, flatFile);
			//check new against previous
			for(int i = 0; i<dimensions * numClusters; i++)
			{
				if(Math.abs(previousCenters.flatData[i] - newCenters.flatData[i]) <= threshold)
				{
					counter++;
				}
				if(counter == numClusters * dimensions)
				{
					match = true;
				}
			}
			/*
			 * if centers don't match previous
			 * reset MStep array
			 */
			if(!match)
			{
				initializeMstep(MStep, size);
				counter = 1;
				runNumber++;
			}
		}
		System.out.println("Completed calculations in " + runNumber + " iterations");
	}
	
	private void initializeMstep(double[][] m, int dataSize)
	{
		for(int i = 0; i<size; i++)
		{
			for(int j = 0; j<m.length; j++)
			{
				m[j][i] = 0;
			}
		}
	}
	
	private double euclideanDist(double x1, double x2)
	{
		return (x1 - x2) * (x1 - x2);
	}

	@Override
	public int clusterCount() {
		// TODO Auto-generated method stub
		return numClusters;
	}

	@Override
	public int clusterID(int tuple) 
	{
		double maxHere = 0;
		int maxIndex = 0;
		for(int i = 0; i<numClusters; i++)
		{
			if(MStep[i][tuple] >= maxHere)
			{
				maxHere = MStep[i][tuple];
				maxIndex = i;
			}
		}
		//returns index +1 to help with human readability
		return maxIndex + 1;
	}
	
	public void printClusterCenters()
	{
		System.out.print("Centers are: ");
		for(int x = 0; x<numClusters * dimensions; x++)
		{
			System.out.print(newCenters.flatData[x] + " ");
			if((x+1)%dimensions == 0 && x != 0)
			{
				System.out.println();
			}
		}
	}

    @Override public int tupleCount() {
        return MStep[0].length;
    }

    @Override public String toString() {
        return "CMeans";
    }
}
