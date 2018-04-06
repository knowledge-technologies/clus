package si.ijs.kt.clus.util.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import javax.management.RuntimeErrorException;

import si.ijs.kt.clus.util.jeans.util.StringUtils;

/**
 * Clus formatter for the numbers that differently formats numbers whose absolute value is at least 1,
 * and the others. The precision that is specified in the constructor of this class is explained in the
 * following, where we use the value {@code precision = 2}
 * 
 * <ul>
 * 	<li> Zero and the numbers whose absolute value is at least one are rounded to {@code precision} decimal places, e.g., 
 *  	<ul>
 *  		<li> {@code 12345} is transformed to {@code "12345"} <il>
 *  		<li> {@code 12345.0} is transformed to {@code "12345"} <il>
 *  		<li> {@code 12345.678} is transformed to {@code "12345.68"} <il>
 *  	</ul>     
 *  <il>
 *  <li> The numbers whose absolute value is smaller than one (except for zero) are rounded to {@code precision} number of significant digits, e.g.,
 *  	<ul>
 *  		<li> {@code 0.12345} is transformed to {@code "1.2E-1"} <il>
 *  		<li> {@code 0.012345} is transformed to {@code "1.2E-2"} <il>
 *  		<li> {@code 0.0012345678} is transformed to {@code "1.2E-3"} <il>
 *  	</ul> 
 *  <il>
 * </ul>
 * A negative number {@code x} is transformed to the string {@code "-" + transformation(-x)}.
 * As a decimal separator, dot is always used.
 * 
 * @author matej
 *
 */
public class ClusNumberFormat {
	DecimalFormat m_SmallNumbersFormat;
	NumberFormat m_BigNumbersFormat;
	int m_AfterDot;
	
	public ClusNumberFormat(int precision) {
		String pattern_for_small;
		if (precision < 0) {
			throw new RuntimeException("Precision has to be positive!");
		} else if(precision == 0) {
			pattern_for_small = "0E0";
		} else {
			pattern_for_small = String.format("0.%sE0", StringUtils.makeString('0', precision));
		}
    	m_SmallNumbersFormat = new DecimalFormat(pattern_for_small);
    	DecimalFormatSymbols sym = m_SmallNumbersFormat.getDecimalFormatSymbols();
        sym.setDecimalSeparator('.');
        m_SmallNumbersFormat.setGroupingUsed(false);
        m_SmallNumbersFormat.setDecimalFormatSymbols(sym);
    	m_BigNumbersFormat = ClusFormat.makeNAfterDot(precision);
    	m_AfterDot = precision;
	}
	
	public String format(double d) {
		if (-1 < d && d < 1 && d != 0) {
			return m_SmallNumbersFormat.format(d);
		} else{
			return m_BigNumbersFormat.format(d);
		}
	}
	
	public String format(int d) {
		if (-1 < d && d < 1 && d != 0) {
			return m_SmallNumbersFormat.format(d);
		} else{
			return m_BigNumbersFormat.format(d);
		}
	}
	
	public String format(long d) {
		if (-1 < d && d < 1 && d != 0) {
			return m_SmallNumbersFormat.format(d);
		} else{
			return m_BigNumbersFormat.format(d);
		}
	}
}
