package utils;

import java.util.concurrent.RecursiveAction;

import abstractsyntaxtree.ExpressionTree;

/**
 * Merge Sort paralelo implementado recorrendo a ForkJoin
 *
 */
public class ParallelMergeSort extends RecursiveAction {

	/**
	 * Generated serial version
	 */
	private static final long serialVersionUID = 1163062893685074970L;

	// Constantes
	private static final int THRESHOLD = 7;

	// Atributos
	private int low;
	private int high;
	private ExpressionTree[] individuals;

	public ParallelMergeSort(int low, int high, ExpressionTree[] individuals) {

		this.low = low;
		this.high = high;
		this.individuals = individuals;
	}

	@Override
	public void compute() {

		if ((this.high - this.low) > THRESHOLD) {

			int mid = this.low + (this.high - this.low) / 2;

			ParallelMergeSort fork1 = new ParallelMergeSort(this.low, mid, this.individuals);
			ParallelMergeSort fork2 = new ParallelMergeSort(mid, this.high, this.individuals);

			fork1.fork();

			fork2.compute();

			fork1.join();

			merge(mid);
		}

		else {

			insertionSort();
		}
	}

	private void insertionSort() {

		for (int i = this.low + 1; i < this.high; i++) {

			ExpressionTree aux = this.individuals[i];

			int j = i;

			while ((j > this.low) && this.individuals[j - 1].compareTo(aux) > 0) {
				this.individuals[j] = this.individuals[j - 1];
				j--;
			}

			this.individuals[j] = aux;
		}
	}

	private void merge(int middle) {

		ExpressionTree[] newIndiv = new ExpressionTree[this.high - this.low];

		int index = 0;
		int i1 = this.low;
		int i2 = middle;

		while (i1 < middle && i2 < this.high) {

			if (individuals[i1].compareTo(individuals[i2]) < 0) {
				newIndiv[index] = individuals[i1];
				i1++;
			}

			else {
				newIndiv[index] = individuals[i2];
				i2++;
			}

			index++;
		}

		if (i1 < middle) {

			for (int i = i1; i < middle; i++) {
				newIndiv[index] = individuals[i];
				index++;
			}
		}
		if (i2 < this.individuals.length) {

			for (int i = i2; i < this.high; i++) {
				newIndiv[index] = individuals[i];
				index++;
			}
		}

		System.arraycopy(newIndiv, 0, individuals, this.low, high - low);
	}
}
