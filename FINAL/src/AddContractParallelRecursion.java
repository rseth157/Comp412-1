import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * Class accepts graph and provide 3rd form of chromatic polynomial
 *
 */
public class AddContractParallelRecursion {

	int[][] graph;
	public List<Integer> polynomial;
	static int i = 0;
	private final Object lock = new Object();

	public AddContractParallelRecursion(int[][] graph) {
		this.graph = graph;
		polynomial = new ArrayList<Integer>();
	}

	public void chromaticPolynomial(int[][] G, int th) {
		ForkJoinPool pool = new ForkJoinPool(th);
		Chromatic c = new Chromatic(G);
		pool.invoke(c);
	}
  
	/**
	 * 
	 * @param g
	 * @return if graph is complete or not if not complete add an edge
	 */
	private boolean isGraphNotCompleteAddEdge(int[][] g, int[] k) {
		for (int i = 0; i < g.length; i++) {
			for (int j = 0; j < g[i].length; j++) {
				if (g[i][j] == 0 && i != j) {
					g[i][j] = 1;
					g[j][i] = 1;
					k[0] = i;
					k[1] = j;
					return false;
				}
			}
		}
		return true; // return 0 if complete
	}

	/**
	 * 
	 * @param g
	 * @param vertex
	 * @return
	 */
	private int[][] contractGraph(int[][] g, int vertex, int vertex2) {
		if (g.length == 1) {
			return g;
		} else {
			int size = g.length - 1;
			int[][] contracted = new int[size][size];

			int REMOVE_ROW = vertex;
			int REMOVE_COLUMN = vertex;
			int p = 0;
			for (int i = 0; i < g.length; ++i) {
				if (i == REMOVE_ROW)
					continue;

				int q = 0;
				for (int j = 0; j < g[i].length; ++j) {
					if (j == REMOVE_COLUMN)
						continue;
					if (i == vertex2 && p != q) {
						contracted[p][q] = or(g[i][j], g[REMOVE_ROW][j]);
					} else if (j == vertex2 && p != q) {
						contracted[p][q] = or(g[i][j], g[i][REMOVE_COLUMN]);
					} else {
						contracted[p][q] = g[i][j];
					}
					++q;
				}

				++p;
			}
			// System.out.println(toString(contracted));
			return contracted;
		}

	}

	// for print the matrix
	public static String toString(int[][] a) {
		String out = "";
		for (int r = 0; r < a.length; r++) {
			for (int c = 0; c < a[r].length; c++) {
				if (c == 0)
					out += "[";
				else
					out += "\t";
				out += a[r][c];
			}
			out += "]\n";
		}

		return out;
	}

	/**
	 * TESTING the program
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String args[]) throws InterruptedException {
		int n = Integer.parseInt(args[0]);
		int th = Integer.parseInt(args[1]);
		int nodei[] = { 0, 1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 5, 6 };
		int nodej[] = { 0, 2, 5, 6, 7, 8, 4, 7, 8, 5, 6, 7, 8 };
		// int nodei[] = {0, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 7, 8, 8, 9,
		// 10, 10, 11,
		// 11,12,13,13,14,15,15,16,16,17,18,18,19,20,20,25,22,23,23,24,25,25,26,27,27,28,29,29,30,31,31,32,33,33,34,35,35,36,37,37,38,39,40,41,41,42,43,44,44,45,46,46,47,48,49,49,50,51,51,52,53,54,55,56,57,58,59,60,39,54};
		// int nodej[] = {0, 2, 9, 6, 12, 3, 14, 4, 17, 5, 19, 6, 7, 8, 21, 24,
		// 9, 10, 11, 26, 28,
		// 12,13,30,14,15,16,32,17,34,18,36,19,20,38,21,22,23,42,24,25,43,26,27,45,28,29,47,30,31,48,32,33,50,34,35,52,36,37,38,53,39,40,41,42,56,43,44,57,45,46,58,47,48,49,59,50,51,60,52,53,54,55,56,57,58,59,60,55,22,40};
		int m = nodei.length - 1;
		// int nodei[] = {0,1,2,3,4};
		// int nodej[] = {0,2,3,4,1};
		// int nodei[] = { 0, 1 };
		// int nodej[] = { 0, 2 };
		int[][] g = new int[n][n];
		int i, j;
		for (i = 0; i < n; i++) {
			for (j = 0; j < n; j++) {
				g[i][j] = 0;
			}
		}
		for (i = 1; i <= m; i++) {
			g[nodei[i] - 1][nodej[i] - 1] = 1;
			g[nodej[i] - 1][nodei[i] - 1] = 1;
		}

		AddContractParallelRecursion ga = new AddContractParallelRecursion(g);
		long start = System.currentTimeMillis();
		// ExecutorService executor =
		// Executors.newFixedThreadPool(Integer.parseInt(args[0]));
		ExecutorService executor = Executors.newFixedThreadPool(th);
		ga.chromaticPolynomial(g, th);
		BigInteger coeff[] = new BigInteger[n];

		for (j = 0; j < n; j++) {
			executor.execute(new Count(ga, j, coeff));
		}
		// gradually shut down the thread
		executor.shutdown();

		// waits for all the threads to get terminated
		executor.awaitTermination(2, TimeUnit.DAYS);

		long end = System.currentTimeMillis();
		System.out.println(th + ":  " + (end - start) + " mili seconds");

		for (i = 0; i < coeff.length; i++) {
			System.out.println((i + 1) + ":  " + coeff[i]);
		}

	}

	public int or(int x, int y) {
		int result;
		if (x == 0 && y == 0) {
			result = 0;
		} else {
			result = 1;
		}

		return result;

	}

	/**
	 * Handles the parallel counting of vertices
	 *
	 */
	private static class Count implements Runnable {
		AddContractParallelRecursion ga;
		int j;
		BigInteger[] coeff;
		BigInteger one = new BigInteger("1");
		public Count(AddContractParallelRecursion ga, int j, BigInteger[] coeff) {
			this.ga = ga;
			this.j = j;
			this.coeff = coeff;
		}

		@Override
		public void run() {

			BigInteger count = new BigInteger("0");
			for (int i = 0; i < ga.polynomial.size(); i++) {
				if (ga.polynomial.get(i) == j + 1)
					count = count.add(one);
			}
			coeff[j] = count;
		}

	}

	/**
	 * 
	 * Parallel recursion for chromatic polynomial
	 */
	public class Chromatic extends RecursiveTask<Integer> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static final int THRESHOLD = 5;

		int[][] G;

		public Chromatic(int[][] G) {

			this.G = G;

		}

		/**
		 * Handles the parallelism
		 */
		@Override
		public Integer compute() {
			int[] k = new int[2];

			if (isGraphNotCompleteAddEdge(G, k)) {
				synchronized (lock) {
					polynomial.add(G.length);
					return 0;
				}

			} else {
				// add edge
				int[][] G1 = G;
				// contract graph
				int[][] G2 = contractGraph(G1, k[0], k[1]);
				// chromaticPolynomial(G1);
				// chromaticPolynomial(G2);

				Chromatic left = new Chromatic(G1);

				left.fork();

				Chromatic right = new Chromatic(G2);

				return Math.max(right.compute(), (int) left.join());

			}

		}

	}

}
