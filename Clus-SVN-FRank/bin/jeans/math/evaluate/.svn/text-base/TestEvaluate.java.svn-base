/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

package jeans.math.evaluate;

public class TestEvaluate {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: TestEvaluate [expression]");
		} else {
			String strg = args[0];
			System.out.println("Expression: "+strg);
			Evaluator evaluator = EvaluatorBuilder.getEvaluator();
			try {
				Expression expr = evaluator.evaluate(strg);
				System.out.println("Evaluated to: "+expr);
				System.out.println("Value: "+expr.getValue());
				System.out.println("Double: "+expr.getValue().getDouble());
			} catch (EvaluateException exp) {
				System.out.println("Exception: "+exp);
			}
		}
	}


}
