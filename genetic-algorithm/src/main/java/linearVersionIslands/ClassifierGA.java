package linearVersionIslands;

import java.util.Scanner;

public class ClassifierGA {

	// Constantes
	private static final int AMOUNT_POPULATION = 1000;

	// Atributos
	private double[][] data;
	private double[] dataOutput;
	private String[] variables;

	public ClassifierGA(double[][] data, double[] dataOutput, String[] variables) {

		this.data = data;
		this.dataOutput = dataOutput;
		this.variables = variables;	
	}

	public void startClassification() {

		int qtdIlhas = 0;

		Scanner leitor = new Scanner(System.in);

		System.out.println("Introduza a quantidade de ilhas que pretende:");
		qtdIlhas = leitor.nextInt();

		Island[] ilhas = new Island[qtdIlhas];
		
		int popIlha = AMOUNT_POPULATION / qtdIlhas;

		for (int islandId = 0; islandId < qtdIlhas - 1; islandId++) {
			ilhas[islandId] = new Island(islandId, data, dataOutput, variables, ilhas, popIlha);
		}
		
		ilhas[qtdIlhas - 1] = new Island(qtdIlhas - 1, data, dataOutput, variables, ilhas, 
				AMOUNT_POPULATION - (qtdIlhas-1) * popIlha);
		
		for (int islandId = 0; islandId < qtdIlhas; islandId++) {
			ilhas[islandId].start();
		}
		
		for (int islandId = 0; islandId < qtdIlhas; islandId++) {
			try {
				ilhas[islandId].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Classification has terminated.");

		leitor.close();
	}
}
