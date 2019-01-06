package parallelVersionIslands;

import java.util.concurrent.Phaser;


/**
 * Represents inner threads which can split the work with the main island
 */
public class InnerIslandThread extends Thread {
	
	// Atributos
	private int threadId;
	private int geracao;
	private int startLimit;
	private int endLimit;
	
	private Phaser phaser;
	
	public InnerIslandThread(int threadId, int startLimit, int endLimit, int geracao, Phaser phaser) {
		
		this.threadId = threadId;
		this.geracao = geracao;
		this.startLimit = startLimit;
		this.endLimit = endLimit;
		
		this.phaser = phaser;
	}

	@Override
	public void run() {
		
		phaser.arriveAndAwaitAdvance();
		
		// TODO:
		for (int geracaoAtual = this.geracao; geracaoAtual < Island.AMOUNT_ITERATIONS; geracaoAtual++) {
			phaser.arriveAndAwaitAdvance();
			phaser.arriveAndAwaitAdvance();
			phaser.arriveAndAwaitAdvance();
		}

		phaser.arriveAndAwaitAdvance();
	}
	
	public void setStartLimit(int startLimit) {
		
		this.startLimit = startLimit;
	}
	
	public void setEndLimit(int endLimit) {
		
		this.endLimit = endLimit;
	}
}