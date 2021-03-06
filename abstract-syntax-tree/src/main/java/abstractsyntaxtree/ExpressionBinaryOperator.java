package abstractsyntaxtree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This enum contains binary operators
 * @author Faculdade de Ciencias da Universidade de Lisboa - FC47806 FC49034
 */
public enum ExpressionBinaryOperator {

	// Binary Operators
	SUM_OPERATOR("+"),
	MINUS_OPERATOR("-"),
	MULT_OPERATOR("*"),
	QUOCIENT_OPERATOR("/"),
	REMAINDER_OPERATOR("%");

	// Operator name
	private String operator;
	
	// attributes necessary to randomBinaryOperator function
	private static final List<ExpressionBinaryOperator> VALUES =
			Collections.unmodifiableList(Arrays.asList(values()));
	private static final int SIZE = VALUES.size();
	private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

	/**
	 * Constructor of the binary operator
	 */
	ExpressionBinaryOperator(String operator) {

		this.operator = operator;
	}

	/**
	 * Returns the current operator textual representation
	 */
	public String getOperator() {

		return this.operator;
	}

	/**
	 * Generates a random element from ExpressionBinaryOperator
	 * @return
	 */
	public static ExpressionBinaryOperator randomBinaryOperator()  {
		return VALUES.get(RANDOM.nextInt(SIZE));
	}
}
