package utils;

/**
 * Class responsible to make time measurements
 */
public class StopWatch {

	// Atributos
	private long start;
	private long stop;
	
	/**
	 * Constructor - does nothing
	 */
	public StopWatch() {
		// Does nothing
	}
	
	/**
	 * Starts the current timer
	 */
	public void start() {
		
		this.start = System.currentTimeMillis();
	}
	
	/**
	 * Stops the current timer
	 */
	public void stop() {
		
		this.stop = System.currentTimeMillis();
	}
	
	/**
	 * Returns the time difference between the start and end 
	 */
	public long getDuration() {
		
		return this.start - this.stop;
	}
}
