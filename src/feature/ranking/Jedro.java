package feature.ranking;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import weka.core.Instances;

public class Jedro {
	/*
	 * Pricakujemo naslednje argumente za arg:
	 * fullyClassifiedRankerClassName rankerOptions fullyClassifiedPredictiveModelClass modelOptions inputFileName fileToBuildModels classIndexRanking classIndexClassifier
	 *
	 * fullyClassifiedRankerClassName: featureRanker=weka.attributeSelection.ReliefFAttributeEval (npr.)
	 * rankerOptions: coma separated values: featureRankerOptions=K10,M-1
	 * fullyClassifiedPredictiveModel class/options: kot zgoraj
	 * inputFileName: featureRankerFile=C:/Users/matejp/.../datasets/iris_ecdf.arff
	 * fileToBuildModel: buildClassifierFile=C:/Users/.../datasets/iris_ecdf.arff (lahko sta pa tud ista)
	 * classIndexRanking/Classifier: classIndexRanking/...=2 (starta z 0)
	 */

	public static void main(String[] args) throws Exception {
		
		System.out.println("args = "+Arrays.toString(args));
		
		ParseOptions opcije = new ParseOptions(args);
		opcije.prikazi();
		DobiRanking aaa = new DobiRanking(opcije, 10, 10);
		aaa.evalFolds();

	}

}
