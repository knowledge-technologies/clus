package si.ijs.kt.clus.util.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import si.ijs.kt.clus.BaseTestCase;

public class ClusNumberFormatTest extends BaseTestCase {
	int[] m_Precisions = new int[] {0, 1, 4};
	ClusNumberFormat[] m_Formatters = new ClusNumberFormat[] {new ClusNumberFormat(0), new ClusNumberFormat(1), new ClusNumberFormat(4)};
	
	@Test
	public void zeroTest(){
		for(ClusNumberFormat formatter : m_Formatters){
			assertEquals("0", formatter.format(0));
		}
		for(ClusNumberFormat formatter : m_Formatters){
			assertEquals("0", formatter.format(0.0));
		}
	}
	
	@Test
	public void smallTest() {
		double[] numbers = new double[]{0.987654, 0.0987654, 0.00987654, 0.000987654, 0.0000987654, 0.00000987654, 0.000000987654,
										0.98, 0.098, 0.0098, 0.00098, 0.000098, 0.0000098, 0.00000098};
		String[] numbersString = new String[] {"0.987654", "0.0987654", "0.00987654", "0.000987654", "0.0000987654", "0.00000987654", "0.000000987654",
											   "0.98", "0.098", "0.0098", "0.00098", "0.000098", "0.0000098", "0.00000098"};
		String[][] solutions = new String[][]{
			new String[] {
							"1E0", "1E-1", "1E-2", "1E-3", "1E-4", "1E-5", "1E-6",
							"1E0", "1E-1", "1E-2", "1E-3", "1E-4", "1E-5", "1E-6"
						 },
			new String[] {
					"9.9E-1", "9.9E-2", "9.9E-3", "9.9E-4", "9.9E-5", "9.9E-6", "9.9E-7",
					"9.8E-1", "9.8E-2", "9.8E-3", "9.8E-4", "9.8E-5", "9.8E-6", "9.8E-7"
				 		 },
			new String[] {
					"9.8765E-1", "9.8765E-2", "9.8765E-3", "9.8765E-4", "9.8765E-5", "9.8765E-6", "9.8765E-7",
					"9.8000E-1", "9.8000E-2", "9.8000E-3", "9.8000E-4", "9.8000E-5", "9.8000E-6", "9.8000E-7"
				 		 },
		};
		for(int i = 0; i < m_Formatters.length; i++) {
			for(int j = 0; j < numbers.length; j++) {
				double[] positiveNegative = new double[] {numbers[j], -numbers[j]};
				String[] solutionPositiveNegative = new String[] {solutions[i][j], "-" + solutions[i][j]};
				for(int k = 0; k < 2; k++) {
					String number = k == 0 ? numbersString[j] : "-" + numbersString[j];
					assertEquals(solutionPositiveNegative[k], m_Formatters[i].format(positiveNegative[k]), "Wrong format for " + number + " with precision " + m_Precisions[i]);
				}
			}
		}
	}
	
	@Test
	public void bigTestDouble() {
		double[] numbers = new double[] {1.0, 77.0, 777.0, 7777.0, 77777.0, 7.7, 7.77, 7.777, 7.7777, 7.77777, 777777.777777};		
		String[][] solutions = new String[][] {
			new String[] {"1", "77", "777", "7777", "77777", "7", "7", "7", "7", "7", "777777"},
			new String[] {"1", "77", "777", "7777", "77777", "7.7", "7.8", "7.8", "7.8", "7.8", "777777.8"},
			new String[] {"1", "77", "777", "7777", "77777", "7.7", "7.77", "7.777", "7.7777", "7.7778", "777777.7778"}
		};
		for(int i = 0; i < m_Formatters.length; i++) {
			for(int j = 0; j < numbers.length; j++) {
				double[] positiveNegative = new double[] {numbers[j], -numbers[j]};
				String[] solutionPositiveNegative = new String[] {solutions[i][j], "-" + solutions[i][j]};
				for(int k = 0; k < 2; k++) {
					assertEquals(solutionPositiveNegative[k], m_Formatters[i].format(positiveNegative[k]), "Wrong format for " + positiveNegative[k] + " with precision " + m_Precisions[i]);
				}
			}
		}
	}
	
	@Test
	public void bigTestInt() {
		int[] numbers = new int[] {1, 77, 777, 7777, 77777, 777777};
		String [][] solutions = new String[][] {
			new String[]{"1", "77", "777", "7777", "77777", "777777"},
			new String[]{"1", "77", "777", "7777", "77777", "777777"},
			new String[]{"1", "77", "777", "7777", "77777", "777777"}
		};
		for(int i = 0; i < m_Formatters.length; i++) {
			for(int j = 0; j < numbers.length; j++) {
				double[] positiveNegative = new double[] {numbers[j], -numbers[j]};
				String[] solutionPositiveNegative = new String[] {solutions[i][j], "-" + solutions[i][j]};
				for(int k = 0; k < 2; k++) {
					assertEquals(solutionPositiveNegative[k], m_Formatters[i].format(positiveNegative[k]), "Wrong format for " + positiveNegative[k] + " with precision " + m_Precisions[i]);
				}
			}
		}
	}
}
