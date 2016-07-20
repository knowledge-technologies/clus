package feature.ranking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ObdelajRezultate {
	double[][][][][][] confMats;//[ponovitve][fold][addRem][numAtr]: confMatr
	String[] atrNames;
	int[][][] sortedAtributes;//[ponovitev][fold][i] = index of i-th best attribute in the fold fold on the ponovitev-th iteration if i != len(sortedAtr[0][0])-1 else class index
	double[][][] ranks, relevances;//ranks / relevances[i][ponov][fold] = rank/relevance of the i-th attribute in the fold fold,  
	String aggregator;
	int[] numsOfFeatures;
	int numFolds, numModels, numAtrs, classIndex;
	String[] resultsNames;
	int ponovitve;
	int numClassValues;
	//Boolean mode: ali podatki glede na povprecni ranking ali glede na ranking za vsak fold posebej
	public ObdelajRezultate(double[][][][][][] confMats, String[] atrNames, int[][][] sortedAtributes, double[][][] relevances, int[] numsOfFeatures, ParseOptions options, Boolean mode){
		this.confMats = confMats;
		this.atrNames = atrNames;
		this.sortedAtributes = sortedAtributes;
//		System.out.println("sortedatr: " + Arrays.deepToString(sortedAtributes));
		this.aggregator = options.aggregator;
		this.relevances = relevances;
		this.numsOfFeatures = numsOfFeatures;
		this.resultsNames = imena(options, mode);
		this.classIndex = options.classIndexRanker; // itak morta bit ista.
//		System.out.println("imenaRez:" + Arrays.toString(resultsNames));
		
		this.numClassValues = this.confMats[0][0][0][0].length;
		this.ponovitve = confMats.length;
		this.numFolds = confMats[0].length;
		this.numModels = numsOfFeatures.length;
		this.numAtrs = atrNames.length;
		ranks = new double[numAtrs][ponovitve][numFolds]; //sez[atr] = [ranks of atr in fold 1, ...]
		for(int ponov = 0; ponov < ponovitve; ponov++){
			for(int fold = 0; fold < numFolds; fold++){
				for(int mesto = 0; mesto < numAtrs; mesto++){
					ranks[sortedAtributes[ponov][fold][mesto]][ponov][fold] = (double) mesto;
				}
			}
		}
//		System.out.println("numClassV:" + numClassValues + "stAtr:" + numAtrs + "stModelov:" + numModels + "numFolc:" + numFolds);
//		System.out.println("Matrike:");
//		for(int i = 0; i < ponovitve; i++){
//			System.out.println("ponovitev: " + i);
//			for(int j = 0; j < numFolds; j++){
//				System.out.println("fold " + j);
//				for(int ar = 0; ar <= 1; ar++){
//					System.out.println(new String[]{"add", "rem"}[ar]);
//					for(int k = 0; k < numModels; k++){
//						System.out.println("model #" + k);
//						System.out.println("  " + Arrays.deepToString(confMats[i][j][ar][k]));
//					}
//					
//				}
//			}
//		}

	}
	public void dobiInZapisiRezultate() throws Exception{
		double[][][][] overallConfMats = overAllMatrices();
		// rang stats
		System.out.println("Ranks stats ..");
		double[][][] rangStats = stats(ranks);
		double[][] ranksPonovitve = rangStats[0];
		double[] averageRanks = rangStats[1][0];
		double[] stdRanks = rangStats[1][1];
		// relevance stats
		System.out.println("Relevance stats ..");
		double[][][] relevanceStats = stats(relevances);
		double[][] relevancesPonovitve = relevanceStats[0];
		double[] averageRelevances = relevanceStats[1][0];
		double[] stdRelevances = relevanceStats[1][1];
		System.out.println("Sorted attributes:");
		int[] overallSortedAttributes = urediAtribute(averageRanks, numAtrs, classIndex); // [index of the best attribute, index of second best, ...]
//		System.out.println(Arrays.toString(overallSortedAttributes));
//		System.out.println("overallConfMata:" + Arrays.deepToString(overallConfMats));
		//pomo
		int ind;
		System.out.println("Writing to files ...");
		// 1. file : ranking stat:
		// glava: Features,MeanRank,MeanRankStDev,MeanImportance,RankFold1,...,RankFoldK,ImporFold1,...,ImpoFoldK
		BufferedWriter wr = new BufferedWriter(new FileWriter(resultsNames[0]));
		wr.write(glavaRankStats(ponovitve));
		wr.write("\n");
		for(int atr = 0; atr < numAtrs; atr++){
			ind = overallSortedAttributes[atr];
			wr.write(vrstaRankStats(atrNames[ind], averageRanks[ind], stdRanks[ind],averageRelevances[ind], stdRelevances[ind], ranksPonovitve[ind], relevancesPonovitve[ind] ));
			wr.write("\n");
		}
		wr.close();
		
		// 2. and 3. file :  adding/removing stats
		// glava: #features,accuracy,confusionMatrix  (Slavkov ima milijon statistik, ki se jih da direkt zracunat iz matrike)
		for(int addRem = 0; addRem <= 1; addRem++){
			wr = new BufferedWriter(new FileWriter(resultsNames[1 + addRem]));
			wr.write(String.join(",", new String[]{"#features", "accuracy","confusionMatrix: confMat[i][j] = number of instances of class i that were classified as class j"}));
			wr.write("\n");
			for(int i = 0; i < numsOfFeatures.length; i++){
				wr.write(String.join(",", new String[]{Integer.toString(numsOfFeatures[i]), Double.toString(accuracy(overallConfMats[addRem][i])), Arrays.deepToString(overallConfMats[addRem][i])}));
				wr.write("\n");
			}
			wr.close();
		}
		System.out.println("Done.");
		
		
		
		
	}
	/**
	 * 
	 * @param mat [fold][addRem][model]: confusion matrix
	 * @return odg[addRem][numsOfFeatures[j]] = average confusion matrix: sum_i matrix[i] / ponovitve
	 */
	private double[][][][] aggFoldsMatrices(double[][][][][] mat) {
		double[][][][] odg = new double[2][numModels][numClassValues][numClassValues]; // kvadratne confMat
		for(int fold = 0; fold < numFolds; fold++){
			for(int addRem = 0; addRem < 2; addRem++){
				for(int model = 0; model < numModels; model++){
					for(int cl1 = 0; cl1 < numClassValues; cl1++){
						for(int cl2 = 0; cl2 < numClassValues; cl2++){
							odg[addRem][model][cl1][cl2] += mat[fold][addRem][model][cl1][cl2] / numFolds;
						}
					}
					
				}
			}
			
		}
		return odg;
	}
	/**
	 * 
	 * @param mat Confusion matrix: may be fake, for it can be 1 by 1 matrix that contains average correlation coefficient over folds
	 * @return accuracy / average correlation coefficient
	 */
	private static double accuracy(double[][] mat){
		double ok = 0.0, vsi = 0.0;
		for(int i = 0; i < mat.length; i++){
			ok += mat[i][i];
			for(int j = 0; j < mat[0].length; j++){
				vsi += mat[i][j];
			}
		}
		return mat.length != 1 ? ok / vsi : ok;
	}
	/**
	 * 
	 * @param ime att's name
	 * @param avgRank avg rank of the attribute
	 * @param stdRank standard deviation
	 * @param avgRel        analog
	 * @param stdRel        for relevances
	 * @param rankPono     ranks by ponovitve
	 * @param relPono      relevances ...
	 * @return typical line of the rankStats file without newline char.
	 */
	private String vrstaRankStats(String ime, double avgRank, double stdRank, double avgRel, double stdRel, double[] rankPono, double[] relPono) {
		String[] pomoRanks = new String[ponovitve], pomoRels = new String[ponovitve];
		for(int pono = 0; pono < ponovitve; pono++){
			pomoRanks[pono] = Double.toString(rankPono[pono] + 1.0); // da bo od 1 do nAtr ...
			pomoRels[pono] = Double.toString(relPono[pono]);
		}
		return String.join(",",new String[]{ime,Double.toString(avgRank + 1.0), Double.toString(stdRank), Double.toString(avgRel), Double.toString(stdRel), String.join(",", pomoRanks), String.join(",", pomoRels)});
	}
	/**
	 * 
	 * @param folds: number of folds
	 * @return header of the rankStats file without newline char.
	 */
	private static String glavaRankStats(int ponov){
		String[] glava = new String[5 + 2 * ponov];
		glava[0] = "Features";glava[1] = "MeanRank";glava[2] = "MeanRankStDev";glava[3]="MeanImportance";glava[4]="MeanImportanceStDev";
		for(int i = 0; i < ponov;i++){
			glava[5 + i] = "ponovitev"+(i+1) + "rank";
			glava[5 + ponov + i] = "ponovitev" + (i+1)+"imp";
		}
		return String.join(",", glava);
		
	}
	/**
	 * 
	 * @return 3 imena datotek za zapis v skladu z nomenklaturo (pri poimenovanju datotek sledimo tudi Slavkovu ...)
	 */
	private static String[] imena(ParseOptions opt, Boolean aliPovp) {
		int koncMape = opt.featureRankerFile.lastIndexOf("/");								//pricakujemo, da je v imenih datotek (ranking/classi) 
		int zacetekKoncnice = opt.featureRankerFile.lastIndexOf(".");						//koren nekako isti oz. da sta imeni 
		String imeDato = opt.featureRankerFile.substring(koncMape + 1, zacetekKoncnice);	//ranking: irisNEKApredelava.arff in iris.arff
		String imeMape = opt.resultsFolder;
		String nacin = aliPovp ? "AverageRanking" : "PerFoldRanking";		
		
		String[] podaljski = new String[]{"_rank_stats_1.csv","_curve_stats_add1.csv","_curve_stats_rem1.csv"};
		String[] odg = new String[podaljski.length];
		String ranker = opt.featureRanker.substring(opt.featureRanker.lastIndexOf(".") + 1);
		String rankingOptions = sparsajOpcije(opt.featureRankerOptions);
		String classi = opt.classifier.substring(opt.classifier.lastIndexOf(".") + 1);
		String classiOptions = sparsajOpcije(opt.classifierOptions);
//		String ponov = Integer.toString(pono);
		for(int i = 0; i < odg.length; i++){
			odg[i] = String.format("%s/%s_Ranker%s%s_Klasifikator%s%s_%s_%s", imeMape, imeDato,ranker, rankingOptions, classi, classiOptions,nacin, podaljski[i]);			
		}
		return odg;
	}
	/**
	 * 
	 * @param sez: seznam Weka opcij: {K,10,M,-1}: Zaradi morebitnih trikov, kot so podatki o jedru za SMO, namo zahteval sode dolzine in bomo previdni pri dodajanju opcij v seznam.
	 * @return
	 */
	private static String sparsajOpcije(String[] sez){
		List<String> pomo = new ArrayList<String>();
		String[] par = new String[2];
		int kam;
		for(int i = 0; i < sez.length; i++){
			if(sez[i].equals("--")) break; // konec normalnih opcij?
			else{
				kam = Math.floorMod(i, 2);
				par[kam] = sez[i];
				if(kam == 1) pomo.add(String.join("", par)); 
			}			
		}
		pomo.sort(null);
		return String.join("", pomo);
	}
	
	/**
	 * 
	 * @param values: values[atr][ponovitev]: vector ranks/relevances over folds for a given attribute
	 * @return [x, [[avg?Atr1, avg?Atr2, ...], [std?Atr1, std?Atr2, ...]]]: average ? =  rank/standard deviation of rank of i-th attribute (i == n corresponds to the class and sez[n] should be nAtr/0); x: avg
	 * @throws Exception 
	 */
	private double[][][] stats(double[][][] values) throws Exception {
		double[] avg = new double[atrNames.length];
		double[] std = new double[atrNames.length];
		
		double[][] aggPerPonovitev = new double[numAtrs][ponovitve];
//		System.out.println("values: " + Arrays.deepToString(values));
//		System.out.println("avg/std: " + Arrays.toString(avg) + Arrays.toString(std));
		for(int pono = 0; pono < ponovitve; pono++){
			for(int atr = 0; atr < numAtrs; atr++){
				aggPerPonovitev[atr][pono] = aggregate(values[atr][pono], aggregator);
			}			
		}
		double vsota, delta, mean;		
		for(int atr = 0; atr < numAtrs; atr++){
			avg[atr] = aggregate(aggPerPonovitev[atr], "mean");
			mean = avg[atr];//aggregator.equals("mean") ? avg[atr] :  aggregate(values[atr], "mean");
			vsota = 0.0;
			for(int pono = 0; pono < ponovitve; pono++){
				delta = mean - aggPerPonovitev[atr][pono];
				vsota += delta * delta;
			}
			std[atr] = Math.sqrt(vsota / ponovitve);
		}
		double[][][] odg = new double[2][2][];
		odg[0] = aggPerPonovitev;
		odg[1][0] = avg;
		odg[1][1] = std;
		return odg;
	}
//	/**
//	 * 
//	 * @return [[avgRankAtr1, avgRankAtr2, ...], [stdRankAtr1, stdRankAtr2, ...]]: average rank/standard deviation of rank of i-th attribute (i == n corresponds to the class and sez[n] should be nAtr)
//	 * @throws Exception 
//	 */
//	private double[][] statsRangi() throws Exception {
//		double[] avRanks = new double[atrNames.length];
//		double[] stdRanks = new double[atrNames.length];
//
//		double vsota, delta, meanRank;
//		for(int atr = 0; atr < numAtrs; atr++){
//			avRanks[atr] = aggregate(ranks[atr], aggregator);
//			meanRank = aggregate(ranks[atr], "mean");
//			vsota = 0;
//			for(int fold = 0; fold < numFolds; fold++){
//				delta = meanRank - ranks[atr][fold];
//				vsota += delta * delta;
//			}
//			stdRanks[atr] = Math.sqrt(vsota / numFolds);
//		}
//
//		double[][] odg = new double[2][];
//		odg[0] = avRanks;
//		odg[1] = stdRanks;
//		return odg;
//	}
	/**
	 * 
	 * @return Aggregated confusion matrices over folds: sum_fold confMatr(fold)
	 */
	private double[][][][] overAllMatrices() {
//		System.out.println("Overall matrices from " + Arrays.deepToString(confMats));
		int normalisationFactor = numClassValues == 1 ? ponovitve * numFolds : ponovitve; // maybe, confMats are correlation coeffs
		double[][][][] overall = new double[2][numModels][numClassValues][numClassValues];
		for(int pono = 0; pono < ponovitve; pono++){
			for(int fold = 0; fold < numFolds; fold++){
				for(int addRem = 0; addRem <= 1; addRem++){
					for(int model = 0; model < numModels; model++){
						for(int cl1 = 0; cl1 < numClassValues; cl1++){
							for(int cl2 = 0; cl2 < numClassValues; cl2++){
								overall[addRem][model][cl1][cl2] += confMats[pono][fold][addRem][model][cl1][cl2] / ponovitve;
//								System.out.println("pristevam" + confMats[pono][fold][addRem][model][cl1][cl2] / ponovitve);
							}
						}
					}
				}
			}
		}

//		System.out.println("agregrirana: " + Arrays.deepToString(overall));
		return overall;
	}

	private double aggregate(double[] ranks, String agregator) throws Exception{
		double agg;
		switch(agregator){
			case "min":
				agg = numAtrs;
				for(double rank : ranks) agg = Math.min(agg, rank);
				break;
			case "max":
				agg = 0.0;
				for(double rank : ranks) agg = Math.max(agg, rank);
				break;
			case "mean":
				agg = 0.0;
				for(double rank : ranks) agg += rank;
				agg /= ranks.length;
				break;
			case "median":
				double[] urejeni = ranks;
				Arrays.sort(urejeni);
				agg = urejeni[urejeni.length / 2];
				break;
			default:
				throw new Exception("Your aggregator ("+agregator+") was not recognised.");
		}
		return agg;
	}
	/**
	 * 
	 * @param rels: skopirano DobiRanking.urediAtribute, s tem da je podatki.numAttributes() -> atrs, podatki.classIndex() -> classIndex, padajoce -> narascajoce
	 * @return: ...
	 */
	private static int[] urediAtribute(double[] rels, int atrs, int classIndex){
		final double[] relevance = new double[atrs - 1]; // razreda nocem
		Integer[] indices = new Integer[atrs -1];
		for(int i = 0; i < atrs - 1; i++){
			if(i != classIndex){
				relevance[i] = rels[i];
				indices[i] = i;
			}
		}
		Arrays.sort(indices, new Comparator<Integer>(){
			@Override
			public int compare(Integer o1, Integer o2) {
				return Double.compare(relevance[o1], relevance[o2]);
			}
		});
		int[] urejen = new int[atrs];
		for(int i = 0; i < urejen.length - 1; i++) urejen[i] = (int) indices[i];
		urejen[urejen.length - 1] = classIndex;
		return urejen;
	}
//	private static double[][] transponiraj(double[][] mat){
//		int m = mat.length, n = mat[0].length;
//		double[][] tran = new double[n][m];
//		for(int i = 0; i < m; i++){
//			for(int j = 0; j < n; j++){
//				tran[j][i] = mat[i][j];
//			}
//		}
//		return tran;
//	}

}
