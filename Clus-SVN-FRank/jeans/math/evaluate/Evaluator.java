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

import java.util.Hashtable;

import jeans.math.*;

public class Evaluator {

	public final static int INFIX    = 0;
	public final static int PREFIX   = 1;
	public final static int POSTFIX  = 2;
	public final static int CONSTANT = 3;

	private int maxLevel;
	private Hashtable protoTypes = new Hashtable();

	public void addPrefixUnaryExpression(String name, int level, UnaryExpression function) {
		addPrototype(name, new ExpressionPrototype(level,1,PREFIX,function));
	}

	public void addPostfixUnaryExpression(String name, int level, UnaryExpression function) {
		addPrototype(name, new ExpressionPrototype(level,1,POSTFIX,function));
	}

	public void addInfixBinaryExpression(String name, int level, BinaryExpression function) {
		addPrototype(name, new ExpressionPrototype(level,2,INFIX,function));
	}

	public void addConstant(String name, MNumber value) {
		addPrototype(name, new ExpressionPrototype(0,0,CONSTANT,new ValueExpression(value)));
	}

//	public void addInfixBinaryExpression(String name, int level, BinaryExpression function) {
//		addPrototype(name, new ExpressionPrototype(level,2,INFIX,function));
//	}

	public void addPrototype(String name, ExpressionPrototype proto) {
		protoTypes.put(name, proto);
		maxLevel = Math.max(maxLevel, proto.getLevel());
	}

	public ExpressionPrototype getPrototype(String name) {
		return (ExpressionPrototype)protoTypes.get(name);
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public Expression evaluate(String strg) throws EvaluateException {
		/*
		int level = getMaxLevel();
		MStringTokenizer tokenizer = new MStringTokenizer(strg);
		Expression[] stackLevels = new Expression[level+1];
		Expression crResult = null;
		boolean expectNumberOrPrefix = true;
		while (tokenizer.hasMoreElements()) {
			String token = null;
			try {
				token = tokenizer.nextGroupToken();
			} catch (GeneralException e) {
				throw new EvaluateException("'(,)' Don't match.", tokenizer.getPosition());
			}
			if (tokenizer.getGroup() == MStringTokenizer.GROUP) {
				if (!expectNumberOrPrefix)
					throw new EvaluateException("Infix/Postfix operator expected.", tokenizer.getPosition());
				crResult = evaluate(token);
				expectNumberOrPrefix = false;
			} else {
				ExpressionPrototype proto = getPrototype(token);
				if (proto != null) {
					//System.out.println("Prototype found for: "+token);
					int protoType = proto.getType();
					int protoLevel = proto.getLevel();
					//System.out.println("Number: "+expectNumberOrPrefix+" Type: "+protoType);
					if (expectNumberOrPrefix && protoType != PREFIX) {
						//System.out.println("Expected number");
						if (protoType == INFIX || protoType == POSTFIX) {
							proto = null;
						} else {
							//System.out.println("No infix or postfix");
							if (protoType == CONSTANT) {
								crResult = proto.getPrototype();
								expectNumberOrPrefix = false;
								System.out.println("Constant: "+crResult);
							}
						}
					} else {
						while (level >= protoLevel) {
							if (stackLevels[level] != null) {
								crResult = ((BinaryExpression)stackLevels[level]).setRightExpression(crResult);
								stackLevels[level] = null;
							}
							level--;
						}
						if (protoType == INFIX || protoType == POSTFIX) {
							if (protoType == INFIX) {
								stackLevels[protoLevel] = ((BinaryExpression)proto.getPrototype()).setLeftExpression(crResult);
								level = getMaxLevel();
								expectNumberOrPrefix = true;
							} else {
							}
						} else {
							if (!expectNumberOrPrefix)
								throw new EvaluateException("Infix/Postfix operator expected.", tokenizer.getPosition());
						}
					}
				}
				if (proto == null) {
					if (expectNumberOrPrefix) {
						tokenizer.pushBack();
						token = tokenizer.nextNumberToken();
						int group = tokenizer.getGroup();
						if (group == MStringTokenizer.FLOAT) {
							try {
								crResult = new ValueExpression(new MDouble(token));
							} catch (NumberFormatException ex) {
								throw new EvaluateException("Float number format error.", tokenizer.getPosition());
							}
						} else if (group == MStringTokenizer.NUMBER) {
							try {
								crResult = new ValueExpression(new MLong(token));
							} catch (NumberFormatException ex) {
								throw new EvaluateException("Natural number format error.", tokenizer.getPosition());
							}
						} else {
							throw new EvaluateException("Number or prefix operator expected: '"+token+"'", tokenizer.getPosition());
						}
						expectNumberOrPrefix = false;
					} else {
						throw new EvaluateException("Unknown post/infix-operator: '"+token+"'", tokenizer.getPosition());
					}
				}
			}
		}
		while (level >= 0) {
			if (stackLevels[level] != null) {
				crResult = ((BinaryExpression)stackLevels[level]).setRightExpression(crResult);
				stackLevels[level] = null;
			}
			level--;
		}
		return crResult;
		*/
		return null;
	}

}

class ExpressionPrototype {

	private int level, arity, type;
	private Expression function;

	public ExpressionPrototype(int level, int arity, int type, Expression function) {
		this.level = level;
		this.function = function;
		this.arity = arity;
		this.type = type;
	}

	public Expression getPrototype() {
		return function.createSimilarExpression();
	}

	public int getType() {
		return type;
	}

	public int getLevel() {
		return level;
	}

	public int getArity() {
		return arity;
	}

}
