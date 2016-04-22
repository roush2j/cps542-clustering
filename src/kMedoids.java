

public class kMedoids extends Clustering
{
	int temp;
	int temp1;

	//orignal Data set
	private DataSet originalData;
	//holds values of potential medoids
	private double[] medoids;
	//need for determining how many clusters to calculate
	private int numClusters;
	//need for determining max interations to run
	private int maxiters;
	//required to store the cost of using a point as a medoid
	private double[][] cost;
	/*
	 * required to determine the min cost value of using
	 * a particular point as a medoid
	 */
	private double costValue = Integer.MAX_VALUE;
	//holds the data points of the best centers
	private double[] bestCenters;
	/**
     * the algorithm implements the PAM algorithm
     * it takes in data and a number that describes
     * the number of clusters to find, the max number of interations, and the
     * initial medoids to work with
     * uses the first 'X' number of data points off the dataset variable, where
     * 'X' is equal to the number of clusters desired
     */
	public kMedoids(DataSet data, int numClusters) 
	{
		super(data);
		temp = data.attrCnt;
		temp1 = data.tupleCnt;
		originalData = data;
		this.numClusters = numClusters;
		maxiters = data.tupleCnt;
		//initialize variables
		cost = new double[numClusters][data.tupleCnt];
		bestCenters = new double[numClusters * data.attrCnt];
		//capture points to use as the initial medoid objects
		medoids = new double[numClusters * data.attrCnt];
		for(int i=0; i<numClusters; i++)
		{
			for(int j=0; j<data.attrCnt; j++)
			{
				medoids[i*data.attrCnt+j] = data.getAttr(i, j);
			}
		}
		//run implementation of PAM algorithm
		pamKmedoids(medoids);

	}

	private void pamKmedoids(double[] medoids)
	{
		//current cost to assign a point to a cluster
		double currentPointCost;
		//previous run's value
		double previousRunCost = -1;
		//current run's value
		double currentRunCost;
		/*
		 * tuple pointer, for use with capturing 
		 * next tuple to use as medoid
		 */
		int pointer = 1;
		//run for max iterations
		for(int max=0; max<maxiters; max++)
		{
			currentRunCost = 0;
			//for each cluster point
			for(int i=0; i<numClusters; i++)
			{
				//for each original data point
				for(int j=0; j<originalData.tupleCnt; j++)
				{
					//calculate the cost of using that as a medoid
					for(int x=0; x<originalData.attrCnt; x++)
					{
						cost[i][j] += euclideanDist(originalData.getAttr(j, x), medoids[i*originalData.attrCnt+x]);
					}
				}
			}
			/*
			 * determine the best medoid to use
			 * note: best medoid to assign a point 
			 * to is the one with the least cost 
			 */
			for(int j=0; j<originalData.tupleCnt; j++)
			{
				/*
				 * re-initialize currentRunCost for 
				 * each new column for the cost array
				 */
				currentPointCost = Integer.MAX_VALUE;
				//determine which cluster the points belong to
				for(int i=0; i<numClusters; i++)
				{
					if(cost[i][j] < currentPointCost)
					{
						currentPointCost = cost[i][j];
					}
				}
				//sum the total cost using medoid as cluster center
				currentRunCost += currentPointCost;
			}
			/*
			 * determine if current cost is better, than previous
			 * run's cost
			 * if it is better, assign best centers, the initial medoid values
			 * and assign currentRunCost to previousRunCost
			 */
			if(previousRunCost == -1)
			{
				for(int i=0; i<medoids.length; i++)
				{
					bestCenters[i] = medoids[i];
				}
				previousRunCost = currentRunCost;
			}
			else if(currentRunCost < previousRunCost)
			{
				for(int i=0; i<medoids.length; i++)
				{
					bestCenters[i] = medoids[i];
				}
				previousRunCost = currentRunCost;
			}
			/*
			 * determine next points to use as the medoid
			 */
			if(originalData.tupleCnt - pointer >= numClusters)
			{
				for(int i=0; i<numClusters; i++)
				{
					for(int j=0; j<originalData.attrCnt; j++)
					{
						//medoids now holds the next set of points to test
						medoids[i*originalData.attrCnt+j] = originalData.getAttr(i + pointer, j);
					}
				}
			}
			else
			{
				int tempPointer = pointer;
				int captured = 0;
				for(int i=0; i<numClusters; i++)
				{
					for(int j=0; j<originalData.attrCnt; j++)
					{
						if(i+tempPointer < originalData.tupleCnt)
						{
							medoids[i*originalData.attrCnt+j] = originalData.getAttr(i + tempPointer, j);
						}
						else if(i+tempPointer == originalData.tupleCnt)
						{
							tempPointer = 0 - captured;
							medoids[i*originalData.attrCnt+j] = originalData.getAttr(i + tempPointer, j);
						}
						else
						{
							medoids[i*originalData.attrCnt+j] = originalData.getAttr(i + tempPointer, j);
						}
					}
					captured ++;
				}
			}
			//move the pointer to be ready for the next iteration
			pointer++;			
		}
	}
	
	private double euclideanDist(double x1, double x2)
	{
		return (x1 - x2) * (x1 - x2);
	}
	
	@Override
	public int clusterCount() 
	{
		return numClusters;
	}

	@Override
	public int clusterID(int tuple) 
	{
		double minHere = Integer.MAX_VALUE;
		int ID = -1;
		for(int i=0; i<numClusters; i++)
		{
			if(cost[i][tuple] < minHere)
			{
				ID = i;
			}
		}
		return ID + 1;
	}
	
	public void printClusterCenters()
	{
		System.out.println("Cluster centers are: ");
		for(int j=0; j<numClusters; j++)
		{
			for(int i=0; i<originalData.attrCnt; i++)
			{
				System.out.print(bestCenters[j * originalData.attrCnt + i] + ", ");
			}
			System.out.println();
		}
	}

    @Override public int tupleCount() {
        return cost[0].length;
    }

    @Override public String toString() {
        return "KMedoids";
    }
}
