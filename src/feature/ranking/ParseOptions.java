package feature.ranking;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParseOptions {
	String[] kljuci = new String[]{
			"featureRanker",
			"featureRankerOptions",
			"classifier",
			"classifierOptions",
			"featureRankerFile",
			"buildClassifierFile",
			"classIndexRanker",
			"classIndexClassifier",
			"aggregator",
			"resultsFolder",
			"outerRanker"};
	String featureRanker, classifier;
	String[] featureRankerOptions, classifierOptions;
	String featureRankerFile, buildClassifierFile;
	int classIndexRanker, classIndexClassifier;
	String aggregator;
	String resultsFolder;
	String outerRanker;
//	int[][] outerRankings;
	double[][][] outerRelevances; // opcije.outerRelevances[ponovitev][fold][atr] = koristnost atributa atr na ponovitev za fold.
	
	public ParseOptions(String[] argi) throws IOException{
		//featureRanker
		String ime;
		int enacaj, dvopicje;
		String[] pomo;
		int kam;
		Boolean[] popucano = new Boolean[kljuci.length];
		for(int i = 0; i < popucano.length; i++) popucano[i] = false;
		for(String opt : argi){
			enacaj = opt.indexOf('=');
			ime = opt.substring(0, enacaj);
//			System.out.println("kljuc = " + ime);
			switch(ime){
				case "featureRanker":
					this.featureRanker = opt.substring(enacaj + 1);
					popucano[0] = true;
					break;
				case "featureRankerOptions":
					if(enacaj + 1 < opt.length()){
						pomo = opt.substring(enacaj + 1).split(",");
						this.featureRankerOptions = new String[2 * pomo.length];
						kam = 0;
						for(String op : pomo){
							dvopicje = op.indexOf(':');
							this.featureRankerOptions[kam++] = "-" + op.substring(0, dvopicje);
							this.featureRankerOptions[kam++] = op.substring(dvopicje + 1);
						}
					}
					else{//ce prezen seznam opcij ...
						this.featureRankerOptions = new String[0];
					}

					popucano[1] = true;
					break;
				case "classifier":
					this.classifier = opt.substring(enacaj + 1);
					popucano[2] = true;
					break;
				case "classifierOptions":
					if(enacaj + 1 < opt.length()){
						pomo = opt.substring(enacaj + 1).split(",");
						this.classifierOptions = new String[2 * pomo.length];
						kam = 0;
						for(String op : pomo){
							dvopicje = op.indexOf(':');
							this.classifierOptions[kam++] = "-" + op.substring(0, dvopicje);//
							this.classifierOptions[kam++] = op.substring(dvopicje + 1);
						}
					}
					else{//ce prezen seznam opcij ...
						this.classifierOptions = new String[0];
					}

					popucano[3] = true;
					break;
				case "featureRankerFile":
					this.featureRankerFile = opt.substring(enacaj + 1);
					popucano[4] = true;
					break;
				case "buildClassifierFile":
					this.buildClassifierFile = opt.substring(enacaj + 1);
					popucano[5] = true;
					break;
				case "classIndexRanker":
					this.classIndexRanker = Integer.parseInt(opt.substring(enacaj + 1));
					popucano[6] = true;
					break;
				case "classIndexClassifier":
					this.classIndexClassifier = Integer.parseInt(opt.substring(enacaj + 1));
					popucano[7] = true;
					break;
				case "aggregator":
					this.aggregator = opt.substring(enacaj + 1);
					popucano[8] = true;
					break;
				case "resultsFolder":
					this.resultsFolder = opt.substring(enacaj + 1);
					popucano[9] = true;
					break;
				case "outerRanker":
					this.outerRanker = opt.substring(enacaj + 1);
					double[][][] pomo2 = preberiZunanjiRanking(this.outerRanker);					
					if(pomo2 != null){
						this.outerRelevances = pomo2;//[1];
//						this.outerRankings = new int[this.outerRelevances.length][this.outerRelevances[0].length];
//						for(int pono = 0; pono < this.outerRankings.length; pono++){
//							for(int atr = 0; atr < this.outerRankings[0].length; atr++){
//								this.outerRankings[pono][atr] = (int) pomo2[0][pono][atr];
//							}
//						}
					}else{
//						this.outerRankings = null;
						this.outerRelevances = null;
					}
					popucano[10] = true;
					break;
				default:
					throw new Error("You tried to set an unknow option: " + ime);
			}
		}
		for(int i = 0; i < popucano.length; i++){
			if(!popucano[i]){
				throw new Error("Nisi popucal vseh moznosti, npr. "+kljuci[i]+".");
			}
		}

		
	}
	/**
	 * Prikaze vse opcije.
	 */
	public void prikazi(){
		System.out.println("Nastavitve:");
		System.out.println("rankFile = " + featureRankerFile);
		System.out.println("classFile = " + buildClassifierFile);
		System.out.println("rankingIndex / classiIndex of class:" + classIndexRanker + "/" + classIndexClassifier);
		System.out.println("ranker / classi = " + featureRanker + "/" + classifier);
		System.out.println("rankOpt = " + Arrays.toString(featureRankerOptions));
		System.out.println("clasOpt = " + Arrays.toString(classifierOptions));
		System.out.println("aggre = " + aggregator);
		System.out.println("resultsFolder = " + resultsFolder);
		System.out.println("outerRanker = " + outerRanker);
	}
	/**
	 * 
	 * @param dato Pot do datoteke z zunanjim rankingom Ta naj vsebuje naslednjo strukturo:
	 * steviloPonovitev
	 * steviloFoldov
	 * AFNAponovitev
	 * rel(atr1fold1ponovitev1);rel(atr2fold1ponovitev1);...;rel(atrNfold1ponovitev1)
	 * ...
	 * rel(atr1foldK);...;rel(atrNfoldK)
	 * rel(atr1fold1ponovitev2);rel(atr2fold1ponovitev2);...;rel(atrNfold1ponovitev2)
	 * ... 
	 * 
	 * @return sez[ponovitev][fold][atr]= koristnost atr za fold na ponovitev
	 * @throws IOException
	 */
	public double[][][] preberiZunanjiRanking(String dato) throws IOException{
		if(! dato.equals("")){
			double[][][] odg = new double[2][1][];
//		    List<String> records = new ArrayList<String>();
		    System.out.println("Poskusam odpreti:---"+dato+"---");
		    BufferedReader bufferedReader = new BufferedReader(new FileReader(dato));
		    int stevec = 0;
		    String[] pomo;
		    // ponovitve, foldi
		    String line = bufferedReader.readLine();
		    int ponovitve = Integer.parseInt(line);
		    line = bufferedReader.readLine();
		    int foldi = Integer.parseInt(line);
		    
		    for(int pono = 0; pono < ponovitve; pono++){
		    	for(int fold = 0; fold < foldi; fold++){
		    		line = bufferedReader.readLine();
		    		pomo = line.trim().split(";");
		    		for(int atr = 0; atr < pomo.length; atr++){
		    			odg[pono][fold][atr] = Double.parseDouble(pomo[atr]);
		    		}
		    	}
		    }
//		    while ((line = bufferedReader.readLine()) != null)	    {
//		    	if(stevec < 2){
//		    		// ranking
//		    		pomo = line.trim().split(";");
//		    		odg[stevec][1] = new double[pomo.length];
//		    		for(int atr = 0; atr < pomo.length; atr++){
//		    			odg[0][1][atr] = Double.parseDouble(pomo[atr]);
//		    		}
//		    		stevec++;
//		    	}
//		    }
		    bufferedReader.close();
			return odg;
	
		}
		else{
			return null;
		}

	}
	

}
