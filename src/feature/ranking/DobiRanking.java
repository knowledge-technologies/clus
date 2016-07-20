package feature.ranking;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import javax.swing.event.TreeExpansionEvent;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

public class DobiRanking {
	Instances podatki, testiranje;
	int[] numAtr; // vektor stevil atributov: addCurve/remCurve: stevilo uporabljenih atributov za izgradnjo modela
	int numFolds, stPonovitev;
	String agg;
	ParseOptions opcije;
	
	/**
	 * 
	 * @param opcie ParseOptions object of options
	 * @param numFolc Number of folds in cross-validation
	 * @param stPonovitev delamo stPonovitev times numFolc CV
	 * @throws Exception 
	 */
	public DobiRanking(ParseOptions opcie, int numFolc, int stPonovitev) throws Exception{
		this.opcije = opcie;
		// podatki
		this.podatki = new Instances(new BufferedReader(new FileReader(opcije.featureRankerFile)));
		this.testiranje = new Instances(new BufferedReader(new FileReader(opcije.buildClassifierFile)));
//		System.out.println(podatki);
		int clInd;
		clInd = opcije.classIndexRanker == -1 ? podatki.numAttributes() - 1 : opcije.classIndexRanker;
		this.podatki.setClassIndex(clInd);
		opcije.classIndexRanker = clInd;
		clInd = opcije.classIndexClassifier == -1 ? testiranje.numAttributes() - 1 : opcije.classIndexClassifier;
		this.testiranje.setClassIndex(clInd);
		opcije.classIndexClassifier = clInd;
		
		// razni parametri
		this.agg = opcije.aggregator;
		this.numFolds = Math.min(numFolc, this.podatki.numInstances());
		this.stPonovitev = stPonovitev;
		this.numAtr = getNumFeatVector(podatki.numAttributes() - 1);

		
	}
	/**
	 * 1. razdelimo na numFolds foldov
	 * 2. za vsak training del naredimo ranking, ki ga potem uporabimo pri izgradji gnezdenih modelov
	 * 3. modele preizkusamo na test delu in dobimo krivulje
	 * 4. zdruzimo rankinge (neodvisno od 3)
	 * 5. zracunamo statistike (bomo simulirali tabele Slavkova ...)
	 * @throws Exception 
	 */
	public void evalFolds() throws Exception {
		int seed = 1234;
		
		// feature names
		String[] features = new String[podatki.numAttributes()];
		for(int atr = 0; atr < features.length; atr++)	features[atr] = podatki.attribute(atr).name();
		// relevances ...
		double[][][] koristnostiPovprecje = new double[podatki.numAttributes()][stPonovitev][numFolds]; // simuliramo kao vedno druge, da obdelava rezultatov stima
		double[][][] koristnosti = new double[podatki.numAttributes()][stPonovitev][numFolds];
		double[] koristnostEnFold = new double[podatki.numAttributes()];
		int[][][] sortedAtributesPovprecje = new int[stPonovitev][numFolds][podatki.numAttributes()]; // sez[ponovitev]: i-th best attribute according to average relevance
		int[][][] sortedAtributes = new int[stPonovitev][numFolds][podatki.numAttributes()]; // sortAtr[ponovitev][fold][i] = i-th best attribute in the fold fold on the ponovitev-th interation
		double[][] pomoRel = new double[podatki.numAttributes()][numFolds];
		
		// confusion matrices
		double[][][][][][] confMatricesPovprecniRanking = new double[stPonovitev][numFolds][2][][][];
		double[][][][][][] confMatrices = new double[stPonovitev][numFolds][2][][][];
		double[][][][] pomo = new double[2][][][];
		
		// pomo
		double[] temp;
		
		// Check, whether data can be handled by SMO: TO DO: SMO --> used classifier
		Instances poskusna = testiranje.trainCV(testiranje.numInstances() - 1, 0);
		System.out.println("stevilo Instanc: " + poskusna.numInstances());
		SMO smo = new SMO();
		smo.buildClassifier(poskusna);
		System.out.println("Vse v redu");
	
		for(int ponovitev = 0; ponovitev < stPonovitev; ponovitev++){
			System.out.println(ponovitev + " evalFolds().............");
			// randomize and stratify dataset: USE SEED!
			podatki.randomize(new Random(seed));
			podatki.stratify(numFolds);
			testiranje.randomize(new Random(seed));
			testiranje.stratify(numFolds);
			
//			System.out.println("podatki");
//			for(int i = 0; i < 7; i++){
//				System.out.println(podatki.instance(i).toString());
//			}
//			System.out.println("testiranje");
//			for(int i = 0; i < 7; i++){
//				System.out.println(testiranje.instance(i));
//			}
			
			// feature ranker staff
			System.out.println("ranker");
			Class cl;
			if(opcije.featureRanker.contains("gozdovi")){
				cl = Class.forName("weka.attributeSelection.ReliefFAttributeEval"); //kr neki pac
			}
			else{
				cl = Class.forName(opcije.featureRanker);
			}
			Constructor con = cl.getConstructor();
			Object ranker = con.newInstance();
//			System.out.println("rankerjeve opcije");
			java.lang.reflect.Method rankerOptions = ranker.getClass().getMethod("setOptions", new Class[]{String[].class});
			rankerOptions.invoke(ranker, new Object[]{Arrays.copyOf(opcije.featureRankerOptions, opcije.featureRankerOptions.length)});
			java.lang.reflect.Method method1 = ranker.getClass().getMethod("buildEvaluator", Instances.class);
			java.lang.reflect.Method method2 = ranker.getClass().getMethod("evaluateAttribute", int.class);
						
			// evaluate folds
			System.out.println("folds: " + numFolds);
			for(int fold = 0; fold < numFolds; fold++){
				System.out.println("fold: " + fold);
				System.out.println(" relevances");
				// compute relevances: posodobi koristostEnFold in koristnosti
				if(opcije.outerRanker == null){
					if(opcije.featureRanker.contains("gozdovi")){
						RndForestR rndF = new RndForestR(opcije.featureRankerFile, podatki.trainCV(numFolds, fold));
						koristnostEnFold = rndF.pozeni();
						for(int atr = 0; atr < koristnosti.length; atr++){
							koristnosti[atr][ponovitev][fold] = koristnostEnFold[atr];
						}
					}
					else{
						// TU SE NAJVEC DOGAJA, CE RANKINGA SE NI
						method1.invoke(ranker, podatki.trainCV(numFolds, fold));
//						System.out.println("Ranker: Zgradil na\n" + podatki.trainCV(numFolds, fold));
//						
//						java.lang.reflect.Method pokaziOpt = ranker.getClass().getMethod("getOptions");
//						System.out.println(Arrays.toString((String[]) pokaziOpt.invoke(ranker)));
						
						for(int atr = 0; atr < koristnosti.length; atr++){
							koristnosti[atr][ponovitev][fold] = (double) method2.invoke(ranker, atr);
							koristnostEnFold[atr] = koristnosti[atr][ponovitev][fold];
						}
					}

				}
				else{
					// imamo zunanji ranking: vsak fold bo imel iste vrednosti, dokler ne odkrijem
					// nacina, kako zagotoviti iste folde: probably, we have: save folds ... and construct the dataset ...
					for(int atr = 0; atr < koristnosti.length; atr++){
						// TEGA NE RAZUMEM ... koristnosti[opcije.outerRankings[ponovitev][fold]][ponovitev][fold] = opcije.outerRelevances[ponovitev][atr];
						koristnosti[atr][ponovitev][fold] = opcije.outerRelevances[ponovitev][fold][atr];
						koristnostEnFold[atr] = koristnosti[atr][ponovitev][fold];
					}
				}
				sortedAtributes[ponovitev][fold] = urediAtribute(koristnostEnFold);

				
				// compute predictive models' accuracies			
				System.out.println(" models");
				pomo = evaluirajRanking(sortedAtributes[ponovitev][fold], fold); 
				confMatrices[ponovitev][fold][0] = pomo[0];
				confMatrices[ponovitev][fold][1] = pomo[1];			
			}
//			System.out.println("Matrike add modelov:");
//			for(int fold = 0; fold < numFolds; fold++){
//				System.out.println(Arrays.deepToString(confMatrices[ponovitev][fold][0]));
//			}
			// compute predictive models' accuracies with respect to the average ranking
//			for(int atr = 0; atr < podatki.numAttributes(); atr++){
//				for(int fold = 0; fold < numFolds; fold++){
//					pomoRel[atr][fold] = koristnosti[atr][ponovitev][fold];
//				}
//			}
//			temp = povprecneRelevance(pomoRel);// allow for different aggregators than mean?
//			sortedAtributesPovprecje[ponovitev][0] = urediAtribute(temp); 
//			for(int fold = 0; fold < numFolds; fold++){
//				for(int atr = 0; atr < podatki.numAttributes(); atr++){
//					sortedAtributesPovprecje[ponovitev][fold][atr] = sortedAtributesPovprecje[ponovitev][0][atr]; // pri fold = 0 to brezveze
//					koristnostiPovprecje[atr][ponovitev][fold] = temp[atr];										// to pa ni
//				}
//			}
//			for(int fold = 0; fold < numFolds; fold++){
//				pomo = evaluirajRanking(sortedAtributesPovprecje[ponovitev][fold], fold);
//				confMatricesPovprecniRanking[ponovitev][fold][0] = pomo[0];
//				confMatricesPovprecniRanking[ponovitev][fold][1] = pomo[1];				
//			}
			
		}
		// obdelava
		System.out.println("obdelava");
		String[] atrNames = new String[podatki.numAttributes()];
		for(int atr = 0; atr < atrNames.length; atr++) atrNames[atr] = podatki.attribute(atr).name();
		ObdelajRezultate obdelaj = new ObdelajRezultate(confMatrices, atrNames, sortedAtributes, koristnosti, numAtr, opcije, false);
		obdelaj.dobiInZapisiRezultate();
//		obdelaj = new ObdelajRezultate(confMatricesPovprecniRanking, atrNames, sortedAtributesPovprecje, koristnostiPovprecje, numAtr, opcije, true);
//		obdelaj.dobiInZapisiRezultate();
		
		
		

	}
	/**
	 * 
	 * @param urejeni urejeni[i] = i-th best attribute
	 * @param fold 0 <= fold < numFolds, fold to evaluate
	 * @return add/rem curve accuracies of the models
	 * @throws Exception 
	 */
	private double[][][][] evaluirajRanking(int[] urejeni, int fold) throws Exception {
		// sets
		Instances trainSet = testiranje.trainCV(numFolds, fold);
		Instances testSet = testiranje.testCV(numFolds, fold);
//		System.out.println("testiram:");
		// st. atributov
//		System.out.println(" stevila atr:" + Arrays.toString(numAtr));
		
		// confusion matrices
		double[][][][] confMats = new double[2][numAtr.length][trainSet.numClasses()][trainSet.numClasses()]; // [add:0, rem:1][nAtr]: confMatr
		
		// classifier staff
		Class cl = Class.forName(opcije.classifier);
		Constructor con = cl.getConstructor();
		Object classi = con.newInstance();

		java.lang.reflect.Method classiOpt = classi.getClass().getMethod("setOptions", new Class[]{String[].class});
		classiOpt.invoke(classi, new Object[]{Arrays.copyOf(opcije.classifierOptions, opcije.classifierOptions.length)}); // setOptions poradira options.classiOptions		
		
		// filtering staff
		Remove rm = new Remove();
		rm.setInvertSelection(true);				
		FilteredClassifier fc = new FilteredClassifier();
		fc.setFilter(rm);
		int[] usedInd; // indices of used attributes
		
		//evaluation
		Evaluation eval;
		System.out.print(" Models to evaluate: " + numAtr.length+"\n ");
		for(int atr = 0; atr < numAtr.length; atr++){
			System.out.print(".");
//			System.out.println("----------------------------------------------------------------------------------------------");
			for(int addRem = 0; addRem < 2; addRem++){
//				System.out.println(".....................................................");
				eval = new Evaluation(trainSet);
				usedInd = new int[numAtr[atr] + 1];// se class
				// set used attributes
				if(addRem == 0){					
					for(int ind = 0; ind < numAtr[atr]; ind++){
						usedInd[ind] = urejeni[ind];
					}
				}
				else{
					for(int ind = 0; ind < numAtr[atr]; ind++){
						usedInd[usedInd.length - 2 - ind]  = urejeni[urejeni.length - 2 - ind]; // [a1, a2, ... , aN, class] -> [ a_k, ... , a_N-1, aN]
					}
				}
				usedInd[usedInd.length - 1] = testiranje.classIndex();
				rm.setAttributeIndicesArray(usedInd);
				fc.setClassifier((Classifier) classi);
				fc.buildClassifier(trainSet);
//				if(addRem == 0 && atr == 0) System.out.println("  Zgradil nas\n" + trainSet + "\nocenila na\n" + testSet);
//				if(atr < 2){
//					System.out.println("Pri atr = "+ atr +" testiram na");
//					for(int insta = 0; insta < testSet.numInstances(); insta++){
//						System.out.println(testSet.instance(insta).toString().substring(0, Math.min(50, testSet.instance(insta).toString().length())));
//					}
//				}

				
				eval.evaluateModel(fc, testSet);
//				System.out.println("  "+Arrays.deepToString(eval.confusionMatrix()));
				if(testiranje.attribute(opcije.classIndexClassifier).isNominal()){
					confMats[addRem][atr] = eval.confusionMatrix();
				}
				else{
					confMats[addRem][atr] = corrCoef2Mat(eval.correlationCoefficient());
				}
				
				
			}
		}
		System.out.println();
		
		return confMats;
	}
	private double[][] corrCoef2Mat(double correlationCoefficient) {
		double[][] odg = new double[1][1];
		odg[0][0] = correlationCoefficient;
		return odg;
	}
	/**
	 * 
	 * @param numFeat number of attributes
	 * @return vector of possible values: I suspect this is a copy of Weka's method in SVM-RFE class.
	 */
	public static int[] getNumFeatVector(int numFeat){
		int[] numFeats;
		ArrayList<Integer> temp = new ArrayList<Integer>();
		int i = numFeat;
		double pctToElim = 10 / 100.0;
		int percTresh = Math.min(50, i);
		int numToEliminatePerIteration = 1;
		int numToElim;

		
		while(i > 0){
	        if (pctToElim > 0) {
				numToElim = (int) (i * pctToElim);
				numToElim = (numToElim > 1) ? numToElim : 1;
				if (i - numToElim < percTresh) {
					pctToElim = 0;
					numToElim = Math.max(i - percTresh, 1);
				}
	        }
	        else {
	        	numToElim = (i >= numToEliminatePerIteration) ? numToEliminatePerIteration : i;
	        }
	        temp.add(i);
	        i -= numToElim;
		}
		numFeats = new int[temp.size()];
		int j = temp.size()-1;
		for(i = 0 ; i < temp.size(); i++){
			numFeats[j] = temp.get(i);
			j--;
		}
		return numFeats;
	}
	
	
	/**
	 * 
	 * @param rels rels[i] = relevance of i-th attribute (class included and is threated as 0.0)
	 * @return urejen urejen[i] = index of i-th best attribute, urejen[-1] = classIndex
	 */
	private int[] urediAtribute(double[] rels){
		final double[] relevance = new double[podatki.numAttributes() - 1]; // razreda nocem
		Integer[] indices = new Integer[podatki.numAttributes() -1];
		for(int i = 0; i < podatki.numAttributes() - 1; i++){
			if(i != podatki.classIndex()){
				relevance[i] = rels[i];
				indices[i] = i;
			}
		}
		Arrays.sort(indices, new Comparator<Integer>(){
			@Override
			public int compare(Integer o1, Integer o2) {
				return -Double.compare(relevance[o1], relevance[o2]);
			}
		});
		int[] urejen = new int[podatki.numAttributes()];
		for(int i = 0; i < urejen.length - 1; i++) urejen[i] = (int) indices[i];
		urejen[urejen.length - 1] = podatki.classIndex();
		return urejen;
	}
	private int[][] pretvoriConfMat(double[][] mat){
		int[][] odg = new int[mat.length][mat[0].length];
		for(int i = 0; i < mat.length; i++){
			for(int j = 0; j < mat[0].length; j++){
				odg[i][j] = (int) Math.round(mat[i][j]);
			}
		}
		return odg;		
	}
	/**
	 * 
	 * @param attRels [atr][fold]: relevance of atr for the fold fold
	 * @return average relevance of each attribute over folds
	 */
	private double[] povprecneRelevance(double[][] attRels){
		double[] povp = new double[attRels.length];
		double p;
		for(int atr = 0; atr < attRels.length; atr++){
			p = 0.0;
			for(int fold = 0; fold < numFolds; fold++){
				p += attRels[atr][fold];
			}
			povp[atr] = p / numFolds;
		}
		return povp;
		
	}
}
