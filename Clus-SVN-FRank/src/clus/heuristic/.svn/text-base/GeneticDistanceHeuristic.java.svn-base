package clus.heuristic;

import clus.data.rows.RowData;
import clus.data.rows.DataTuple;
import clus.main.Settings;
import clus.statistic.GeneticDistanceStat;
import clus.statistic.ClusStatistic;
import java.util.*;

public abstract class GeneticDistanceHeuristic extends ClusHeuristic {

	protected RowData m_Data; // the data at the current node
	protected RowData m_RootData; // the complete data set at the root of the tree, this is needed for taking the complement of the data in the current node
	protected int[] m_DataIndices; // the indices (in the original dataset) of the data at the current node
	protected int[] m_ComplDataIndices; // the indices (in the original dataset) of the data in the complement of the data at the current node

	public String getName() {
		return "GeneticDistanceHeuristic";
	}

	// called when a new node should be split
	public void setData(RowData data) {
		m_Data = data;
		m_DataIndices = constructIndexVector(m_Data);
		m_ComplDataIndices = constructComplIndexVector(m_RootData, m_DataIndices);
	}

	public int[] constructIndexVector(RowData data) {
		int nb = data.getNbRows();
		int[] resultvector = new int[nb];
		for (int i=0; i<nb; i++) {
			int index = data.getTuple(i).getIndex();
			resultvector[i] = index;
		}
		return resultvector;
	}

	public int[] constructIndexVector(RowData data, ClusStatistic stat) {
		GeneticDistanceStat gstat = (GeneticDistanceStat)stat;
		int nb = (int)gstat.m_SumWeight;
		int[] resultvector = new int[nb];
		for (int i=0; i<nb; i++) {
			int tupleindex = gstat.getTupleIndex(i);
			int origindex = data.getTuple(tupleindex).getIndex();
			resultvector[i] = origindex;
		}
		return resultvector;
	}

	public int[] constructComplIndexVector(RowData data, int[] indices) {
		int totalnb = data.getNbRows();
		int indnb = indices.length;
		int complnb = totalnb - indnb;
		int[] resultvector = new int[complnb];
		int indexpos = 0;
		int currindex = indices[indexpos];
		int j=0;
		for (int i=0; i<totalnb; i++) {
			int index = data.getTuple(i).getIndex();
			if (currindex>index) {
				resultvector[j] = index;
				j++;
			}
			else if (currindex==index) {
				indexpos++;
				if (indexpos >= indnb) currindex = 1000000;
				else currindex = indices[indexpos];
			}
			else if (currindex<index) {
				System.err.println("GeneticDistanceHeuristic : Something is wrong with datatuple indices!");
			}
		}
		return resultvector;
	}


	// Genetic distances
	// Remark: In some distance calculations, string positions with "?" (missing values) or "-" (gaps) are discarded.
	// This is to be consistent with the Phylip program (at least, for Jukes-Cantor; Kimura still gives different output).


	public double getDistance(String[] seq1, String[] seq2) {
		switch (Settings.m_PhylogenyDM.getValue()) {
		case Settings.PHYLOGENY_DISTANCE_MEASURE_EDIT:
			return getEditDistance(seq1,seq2);
		case Settings.PHYLOGENY_DISTANCE_MEASURE_PDIST:
			return getPDistance(seq1,seq2);
		case Settings.PHYLOGENY_DISTANCE_MEASURE_JC:
			return getJukesCantorDistance(seq1,seq2);
		case Settings.PHYLOGENY_DISTANCE_MEASURE_KIMURA:
			return getKimuraDistance(seq1,seq2);
		case Settings.PHYLOGENY_DISTANCE_MEASURE_AMINOKIMURA:
			return getAminoKimuraDistance(seq1,seq2);
		}
		return 0.0; // is never executed
	}

	
	// the number of positions that are different
	public double getEditDistance(String[] seq1, String[] seq2) {
		//System.out.println("edit");
		double p=0;
		for (int i=0; i<seq1.length; i++) {
			if (!seq1[i].equals(seq2[i])) {
				p++;
			}
		}
		return p;
	}

	// the percentage of positions that are different (only non-gap and non-missing positions taken into account!)
	public double getPDistance(String[] seq1, String[] seq2) {
		double p=0;
		int nb = 0;
		for (int i=0; i<seq1.length; i++) {
			if (((seq1[i].equals("?") || seq2[i].equals("?")) || seq1[i].equals("-")) || seq2[i].equals("-")) {
			}
			else {
				if (!seq1[i].equals(seq2[i])) {
					p++;
					nb++;
				}
				else {
					nb++;
				}
			}
		}
		double p_distance = (double)p / (double)nb;
		if (p_distance == Double.POSITIVE_INFINITY) System.out.println("p: " + p + " nb: " + nb + " " + seq1 + " " + seq2);
		if (p_distance == Double.NEGATIVE_INFINITY) System.out.println("p: " + p + " nb: " + nb + " " + seq1 + " " + seq2);
		return p_distance;
	}

	// yields same tree as p-distance, but different - more realistic - distances
	public double getJukesCantorDistance(String[] seq1, String[] seq2) {
		double p_distance = getPDistance(seq1, seq2);
		double jk_distance;
		if (p_distance > 0.749) {
			jk_distance = 2.1562; // not defined for >= 0.75
			System.out.println("Warning: infinite Jukes Cantor distance (p-distance => 0.75), set to 2.1562");
		}
		else jk_distance = -0.75 * Math.log(1.0-((4.0*p_distance)/3.0));
		return jk_distance;
	}

	public double getKimuraDistance(String[] seq1, String[] seq2) {
		int nb = 0;
		int ti=0;
		int tv=0;
		for (int i=0; i<seq1.length; i++) {
			if (((seq1[i].equals("?") || seq2[i].equals("?")) || seq1[i].equals("-")) || seq2[i].equals("-")) {
			}
			else {
			nb++;
			if (!seq1[i].equals(seq2[i])) {
				if (seq1[i].equals("A")) {
					if (seq2[i].equals("G")) {
						ti++;
					}
					else tv++;
				} else if (seq1[i].equals("C")) {
					if (seq2[i].equals("T")) {
						ti++;
					}
					else tv++;
				} else if (seq1[i].equals("G")) {
					if (seq2[i].equals("A")) {
						ti++;
					}
					else tv++;
				} else if (seq1[i].equals("T")) {
					if (seq2[i].equals("C")) {
						ti++;
					}
					else tv++;
				}
			}
			}
		}
		double ti_ratio = (double)ti / (double)nb;
		double tv_ratio = (double)tv / (double)nb;

		double term1 = Math.log10(1.0/(1.0-2.0*ti_ratio-tv_ratio));
		double term2 = Math.log10(1.0/(1.0-2.0*tv_ratio));
		double kimura = term1+term2;

		//System.out.println("kimura_distance: " + kimura);
		return kimura;
	}

	public double getAminoKimuraDistance(String[] seq1, String[] seq2) {
		double p_distance = getPDistance(seq1, seq2);
		double kimura;
		if (p_distance > 0.8541) {
			kimura = 12.84; // not defined for >= 0.8514
			System.out.println("Warning: infinite AminoKimura distances (p-distance > 0.85), set to 12.84");
		}
		else kimura = -1.0 * Math.log(1.0 - p_distance - 0.2 * Math.pow(p_distance,2.0));
		return kimura;
	}

/*************************************************************************************
 * Not sure if the code below is still used / useful
 * 
 *************************************************************************************/


/*	public double calculateStarDistance(GeneticDistanceStat stat, RowData data) {
		int nb_tg = stat.m_NbTarget;
		double nb_ex = stat.m_SumWeight;
		double dist = 0.0;
		for (int i=0; i<nb_ex; i++) {
			for (int j=i+1; j<nb_ex; j++) {
				double newdist = calculateDistance(nb_tg, stat, data, i, stat, data, j);
				dist += newdist;
			}
		}
		if (nb_ex == 1)
			dist = 0.0;
		else dist = dist / (nb_ex-1);
		return dist;
	}*/

/*	public double calculatePairwiseDistanceWithin(GeneticDistanceStat stat, RowData data) {
		int nb_tg = stat.m_NbTarget;
		double nb_ex = stat.m_SumWeight;
		double dist = 0.0;
		for (int i=0; i<nb_ex; i++) {
			for (int j=i+1; j<nb_ex; j++) {
				double newdist = calculateDistance(nb_tg, stat, data, i, stat, data, j);
				dist += newdist;
			}
		}
		dist = dist / nb_ex;
		return dist;
	}*/

/*	public double calculatePairwiseDistance(GeneticDistanceStat pstat, RowData pdata, GeneticDistanceStat nstat, RowData ndata) {
		boolean useSampling = false;
		int sampleSize = 500;

		int nb = pstat.m_NbTarget;
		double n_pos = pstat.m_SumWeight;
		double n_neg = nstat.m_SumWeight;

		double dist=0.0;
		Random rnd = new Random();
		switch (Settings.m_PhylogenyLinkage.getValue()) {
			case Settings.PHYLOGENY_LINKAGE_SINGLE:
				// maximize the minimal distance
				dist = Double.MAX_VALUE;
				if (!useSampling || n_pos * n_neg < sampleSize) {
					for (int i=0; i<n_pos; i++) {
						for (int j=0; j<n_neg; j++) {
							dist = Math.min(dist, calculateDistance(nb, pstat, pdata, i, nstat, ndata, j));
						}
					}
				}
				else {
					for (int i=0; i<sampleSize; i++) {
						int rndpos = rnd.nextInt((int)n_pos);
						int rndneg = rnd.nextInt((int)n_neg);
						dist = Math.min(dist, calculateDistance(nb, pstat, pdata, rndpos, nstat, ndata, rndneg));
					}
				}
				break;

			case Settings.PHYLOGENY_LINKAGE_AVERAGE:
				// maximize the average distance
				dist = 0.0;
				if (!useSampling || n_pos * n_neg < sampleSize) {
					for (int i=0; i<n_pos; i++) {
						for (int j=0; j<n_neg; j++) {
							dist += calculateDistance(nb, pstat, pdata, i, nstat, ndata, j);
						}
					}
					dist = dist / (n_pos * n_neg);
				}
				else {
					for (int i=0; i<sampleSize; i++) {
						int rndpos = rnd.nextInt((int)n_pos);
						int rndneg = rnd.nextInt((int)n_neg);
						dist += calculateDistance(nb, pstat, pdata, rndpos, nstat, ndata, rndneg);
					}
					dist = dist / 100.0;
				}
				break;

			case Settings.PHYLOGENY_LINKAGE_COMPLETE:
				// maximize the maximal distance
				dist = Double.MIN_VALUE;
				if (!useSampling || n_pos * n_neg < sampleSize) {
					for (int i=0; i<n_pos; i++) {
						for (int j=0; j<n_neg; j++) {
							dist = Math.max(dist, calculateDistance(nb, pstat, pdata, i, nstat, ndata, j));
						}
					}
				}
				else {
					for (int i=0; i<sampleSize; i++) {
						int rndpos = rnd.nextInt((int)n_pos);
						int rndneg = rnd.nextInt((int)n_neg);
						dist = Math.max(dist, calculateDistance(nb, pstat, pdata, rndpos, nstat, ndata, rndneg));
					}
				}
				break;
		}

		return dist;
	}*/

/*	public int getOriginalIndex(DataTuple tuple) {
		//System.out.println("target tuple: " + tuple.toString());
		String str = tuple.toString();
		for(int i=0; i<m_RootData.getNbRows(); i++) {
			DataTuple oertuple = m_RootData.getTuple(i);
			String oerstr = oertuple.toString();
			//System.out.println("tuple: " + i + " : " + oertuple.toString());
			if (str.equals(oerstr))
				return i;
		}
		System.out.println("*************** original tupleindex not found *****************");
		return -1;
	}*/

/*	public double calculateMutations(int nbtargets, GeneticDistanceStat pstat, RowData pdata, GeneticDistanceStat nstat, RowData ndata) {
		double nbmutations = 0;
		double n_pos = pstat.m_SumWeight;
		double n_neg = nstat.m_SumWeight;
		HashMap[] poshash = new HashMap[nbtargets];
		HashMap[] neghash = new HashMap[nbtargets];
		for (int p=0; p<nbtargets; p++) {
			poshash[p] = new HashMap();
			neghash[p] = new HashMap();
		}

		for (int i=0; i<n_pos; i++) {
			int posindex = pstat.getTupleIndex(i);
			DataTuple postuple = pdata.getTuple(posindex);
			String ch = new String();
			for (int p=0; p<nbtargets; p++) {
				int posnomvalue = pstat.m_Attrs[p].getNominal(postuple);
				ch = pstat.m_Attrs[p].getValueOrMissing(posnomvalue);
				poshash[p].put(ch, true);
			}
		}
		for (int i=0; i<n_neg; i++) {
			int negindex = nstat.getTupleIndex(i);
			DataTuple negtuple = ndata.getTuple(negindex);
			String ch = new String();
			for (int p=0; p<nbtargets; p++) {
				int negnomvalue = nstat.m_Attrs[p].getNominal(negtuple);
				ch = nstat.m_Attrs[p].getValueOrMissing(negnomvalue);
				neghash[p].put(ch, true);
			}
		}

		for (int p=0; p<nbtargets; p++) {
			Set posset = poshash[p].keySet();
			Set negset = neghash[p].keySet();
			//System.out.println("pos " + p + " : posset = " + posset.toString() + " negset = " + negset.toString());
			int nbintersection = intersectionSize(posset,negset);
			// if nbintersection == 0 -> for sure a mutation happened
			// if nbintersection == 1 -> for sure no mutation happened
			// if nbintersection > 1 -> maybe a mutation happened
			if (nbintersection == 0) nbmutations++;
			else if (nbintersection == 2) nbmutations = nbmutations + 0.5;
			else if (nbintersection == 3) nbmutations = nbmutations + 0.6667;
			else if (nbintersection == 4) nbmutations = nbmutations + 0.75;
			else if (nbintersection == 5) nbmutations = nbmutations + 0.8;
		}

		return nbmutations;
	}*/


/*	public int calculateMutationsWithin(int nbtargets, GeneticDistanceStat stat, RowData data) {
		int nbmutations = 0;
		double nb_ex = stat.m_SumWeight;
		HashMap[] hash = new HashMap[nbtargets];
		for (int p=0; p<nbtargets; p++) {
			hash[p] = new HashMap();
		}

		for (int i=0; i<nb_ex; i++) {
			int index = stat.getTupleIndex(i);
			DataTuple tuple = data.getTuple(index);
			String ch = new String();
			for (int p=0; p<nbtargets; p++) {
				int nomvalue = stat.m_Attrs[p].getNominal(tuple);
				ch = stat.m_Attrs[p].getValueOrMissing(nomvalue);
				hash[p].put(ch, true);
			}
		}

		for (int p=0; p<nbtargets; p++) {
			Set set = hash[p].keySet();
			//System.out.println("pos " + p + " : posset = " + posset.toString() + " negset = " + negset.toString());
			int mutationsatposition = set.size()-1;
			nbmutations += mutationsatposition;
		}
		return nbmutations;
	}*/


/*	public double calculateTotalDistanceToPrototype(int nbtargets, GeneticDistanceStat stat, RowData data) {
		double dist = 0.0;
		double dist2 = 0.0;

//		double[][] protomatrix = stat.getProbabilityPrediction();


		String[] proto = new String[nbtargets];
		for (int i=0; i<nbtargets; i++) {
			proto[i] = stat.getPredictedClassName(i);
		}

		double nb_ex = stat.m_SumWeight;
		for (int i=0; i<nb_ex; i++) {
			int index = stat.getTupleIndex(i);
			DataTuple tuple = data.getTuple(index);
			String[] ch = new String[nbtargets];
			for (int p=0; p<nbtargets; p++) {
				int nomvalue = stat.m_Attrs[p].getNominal(tuple);
//				dist += (1 - protomatrix[p][nomvalue]);
				ch[p] = stat.m_Attrs[p].getValueOrMissing(nomvalue);
			}
			dist2 += getDistance(proto,ch);
		}
	//	System.out.println("dist: " + dist + " dist2: " + dist2);
		return dist;
		//return dist2;
	}*/


/*	public double calculateTotalDistanceBetweenPrototypeMatrices(int nbtargets, GeneticDistanceStat stat1, GeneticDistanceStat stat2) {
		double dist = 0.0;

//		double[][] protomatrix1 = stat1.getProbabilityPrediction();
//		double[][] protomatrix2 = stat2.getProbabilityPrediction();

		for (int j=0; j<nbtargets; j++) {
			for (int k=0; k<5; k++) {
//				double d = Math.abs(protomatrix1[j][k] - protomatrix2[j][k]);
//				dist += d;
			}
		}
		return dist;
	}*/



/*	public int maxArrayIndex(int[] t) {
	    int maximum = t[0];
	    int maxindex = 0;
	    for (int i=1; i<t.length; i++) {
	        if (t[i] > maximum) {
	            maximum = t[i];
	            maxindex = i;
	        }
	    }
	    return maxindex;
	}*/

/*	public boolean emptyStringIntersection(Set set1, Set set2) {
		Object[] arr1 = set1.toArray();
		Object[] arr2 = set2.toArray();
		for (int i=0; i<arr1.length; i++) {
			String s1 = (String)arr1[i];
			for (int j=0; j<arr2.length; j++) {
				String s2 = (String)arr2[j];
				if (s1.equals(s2))
					return false;
			}
		}
		return true;
	}*/

/*	public int intersectionSize(Set set1, Set set2) {
		int intersectionnb = 0;
		Object[] arr1 = set1.toArray();
		Object[] arr2 = set2.toArray();
		for (int i=0; i<arr1.length; i++) {
			String s1 = (String)arr1[i];
			for (int j=0; j<arr2.length; j++) {
				String s2 = (String)arr2[j];
				if (s1.equals(s2))
					intersectionnb++;
			}
		}
		return intersectionnb;
	}*/

/*	public double calculateDistance(int nbtargets, GeneticDistanceStat pstat, RowData pdata, int randompos, GeneticDistanceStat nstat, RowData ndata, int randomneg) {
		int posindex = pstat.getTupleIndex(randompos);
		int negindex = nstat.getTupleIndex(randomneg);

		// make pos string
		DataTuple postuple = pdata.getTuple(posindex);
		String[] posstring = new String[nbtargets];
		for (int i=0; i<nbtargets; i++) {
			int posnomvalue = pstat.m_Attrs[i].getNominal(postuple);
			posstring[i] = pstat.m_Attrs[i].getValueOrMissing(posnomvalue);
		}

		// make neg string
		DataTuple negtuple = ndata.getTuple(negindex);
		String[] negstring = new String[nbtargets];
		for (int i=0; i<nbtargets; i++) {
			int negnomvalue = nstat.m_Attrs[i].getNominal(negtuple);
			negstring[i] = nstat.m_Attrs[i].getValueOrMissing(negnomvalue);
		}
		return getDistance(posstring, negstring);

	}*/


/*	public double calculatePrototypeDistance(GeneticDistanceStat pstat, GeneticDistanceStat nstat) {
		// Equal for all target attributes
		int nb = pstat.m_NbTarget;
		double n_pos = pstat.m_SumWeight;
		double n_neg = nstat.m_SumWeight;
		// Acceptable?
		if (n_pos < Settings.MINIMAL_WEIGHT || n_neg < Settings.MINIMAL_WEIGHT) {
			return Double.NEGATIVE_INFINITY;
		}

		String[] proto_pos = new String[nb];
		String[] proto_neg = new String[nb];
		for (int i=0; i<nb; i++) {
			proto_pos[i] = pstat.getPredictedClassName(i);
			proto_neg[i] = nstat.getPredictedClassName(i);
		}

		return getDistance(proto_pos,proto_neg);
	}*/
	
	// The test that yields the largest heuristic will be chosen in the end. Since we want to minimize the total branch length,
	// we maximize the inverse of it.
	/*public double calcHeuristicPars(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic missing) {
		// first create all needed statistics and data
		GeneticDistanceStat tstat = (GeneticDistanceStat)c_tstat;
		GeneticDistanceStat pstat = (GeneticDistanceStat)c_pstat;
		GeneticDistanceStat nstat = (GeneticDistanceStat)tstat.cloneStat();
		nstat.copy(tstat);
		nstat.subtractFromThis(pstat);

		double n_pos = pstat.m_SumWeight;
		double n_neg = nstat.m_SumWeight;
//		System.out.println("nb pos examples: " + n_pos);
//		System.out.println("nb neg examples: " + n_neg);

		// Acceptable test?
		if (n_pos < Settings.MINIMAL_WEIGHT || n_neg < Settings.MINIMAL_WEIGHT) {
			return Double.NEGATIVE_INFINITY;
		}

		// If split position missing for some sequence, don't use it in split (probably this approach is not optimal)
		if (Math.round(n_pos) != n_pos || Math.round(n_neg) != n_neg) {
			return Double.NEGATIVE_INFINITY;
		}
	// -------------

		String key = pstat.getBits().toString();
		Double value = (Double) m_HeurComputed.get(key);
		if (value!=null) {
			return value.doubleValue();
		}

		int[] posindices = constructIndexVector(m_Data, pstat);
		int[] negindices = constructComplIndexVector(m_Data, posindices);

		//double result = calculatePairwiseDistance(pstat,m_Data,nstat,m_Data);
		//return result;
		//return calculatePrototypeDistance(pstat,nstat);

		//double interiordist = calculateMutations(tstat.m_NbTarget,pstat,m_Data,nstat,m_Data);
//		double interiordist = calculatePrototypeDistance(pstat,nstat);
//		double interiordist = calculatePairwiseDistance(pstat,m_Data,nstat,m_Data);
//		double interiordist = calculateTotalDistanceBetweenPrototypeMatrices(tstat.m_NbTarget,pstat,nstat);
		double interiordist = calcPWSLDistance(posindices, negindices);
	//	System.out.println("heur = " + interiordist);



		//double maxposdist = calculateTotalDistanceToPrototype(tstat.m_NbTarget, pstat,m_Data);
		//double maxnegdist = calculateTotalDistanceToPrototype(tstat.m_NbTarget, nstat,m_Data);
		//double posdist = calculateStarDistance(pstat,m_Data);
		//double negdist = calculateStarDistance(nstat,m_Data);
		//double minposdist = calculateMutationsWithin(tstat.m_NbTarget,pstat,m_Data);
		//double minnegdist = calculateMutationsWithin(tstat.m_NbTarget,nstat,m_Data);
		//double posdist = (maxposdist + minposdist) / 2;
		//double negdist = (maxnegdist + minnegdist) / 2;

		double result = interiordist;

//		double result = calcTotalDistanceWithSlAsProto(posindices, negindices);
		//System.out.println("posdist: " + posdist + " (max: " + maxposdist + ", min: " + minposdist + ") " + " negdist: " + negdist + " (max: " + maxnegdist + ", min: " + minnegdist + ") "+ " interior: " + interiordist + " result: " + result);

		//return 0.0 - result;
		return result;
	}
*/

/*	public double calcPWSLDistance(int[] posindices, int[] negindices) {
		// look for 2 ancestors with minimal distance
		double dist = Double.MAX_VALUE;
		int posanc = Integer.MAX_VALUE;
		int neganc = Integer.MAX_VALUE;
		for (int i=0; i<posindices.length; i++) {
			for (int j=0; j<negindices.length; j++) {
				int row = posindices[i];
				int col = negindices[j];
				double distance = 0; //m_DistMatrix.get(row, col);
				if (distance < dist) {
					posanc = row;
					neganc = col;
					dist = distance;
				}
			}
		}
		return dist;

	}*/


/*	public double calcTotalDistanceWithSlAsProto(int[] posindices, int[] negindices) {
		// look for 2 ancestors with minimal distance
		double dist = Double.MAX_VALUE;
		int posanc = Integer.MAX_VALUE;
		int neganc = Integer.MAX_VALUE;
		for (int i=0; i<posindices.length; i++) {
			for (int j=0; j<negindices.length; j++) {
				int row = posindices[i];
				int col = negindices[j];
				double distance = 0; //m_DistMatrix.get(row, col);
				if (distance < dist) {
					posanc = row;
					neganc = col;
					dist = distance;
				}
			}
		}

		double posdist = 0.0;
		for (int i=0; i<posindices.length; i++) {
			int index = posindices[i];
			posdist += 0; //m_DistMatrix.get(posanc, index);
		}

		double negdist = 0.0;
		for (int i=0; i<negindices.length; i++) {
			int index = negindices[i];
			negdist += 0; //m_DistMatrix.get(neganc, index);
		}

		double result = dist + posdist + negdist;
		return result;

	}
*/

	
	

	/*	//old code
	// The test that yields the largest heuristic will be chosen in the end. Since we want to minimize the total branch length,
	// we maximize the inverse of it.
	public double calcHeuristicdist(ClusStatistic c_tstat, ClusStatistic c_pstat, ClusStatistic missing) {

		// first create all needed statistics and data
		GeneticDistanceStat tstat = (GeneticDistanceStat)c_tstat;
		GeneticDistanceStat pstat = (GeneticDistanceStat)c_pstat;
		GeneticDistanceStat nstat = (GeneticDistanceStat)tstat.cloneStat();
		nstat.copy(tstat);
		nstat.subtractFromThis(pstat);

		double n_pos = pstat.m_SumWeight;
		double n_neg = nstat.m_SumWeight;
		double n_tot = tstat.m_SumWeight;

		// Acceptable test?
		if (n_pos < Settings.MINIMAL_WEIGHT || n_neg < Settings.MINIMAL_WEIGHT) {
			return Double.NEGATIVE_INFINITY;
		}

		// If position missing for some sequence, don't use it in split (probably this approach is not optimal)
		if (Math.round(n_pos) != n_pos || Math.round(n_neg) != n_neg) {
			return Double.NEGATIVE_INFINITY;
		}

		double posdist = calculatePairwiseDistanceWithin(pstat,m_Data);
		double negdist = calculatePairwiseDistanceWithin(nstat,m_Data);

		if (m_Data.getNbRows() == m_RootData.getNbRows()) { // root of the tree
			double betweendist = calculatePairwiseDistance(pstat, m_Data, nstat, m_Data);
			double result = betweendist + posdist + negdist;

			return 0.0 - result;
		}
		else {
			GeneticDistanceStat compStat = new GeneticDistanceStat(tstat.m_Attrs);
			m_CompData.calcTotalStatBitVector(compStat);

			double betweenpndist = 0.5 * calculatePairwiseDistance(pstat, m_Data, nstat, m_Data);
			double betweenpcdist = 0.5 * calculatePairwiseDistance(pstat, m_Data, compStat, m_CompData);
			double betweenncdist = 0.5 * calculatePairwiseDistance(nstat, m_Data, compStat, m_CompData);
			double compdist = calculatePairwiseDistanceWithin(compStat,m_CompData);

			// compdist not really needed to pick best test, but including it gives right total branch length of phylo tree
			double result = compdist + posdist + negdist + betweenpndist + betweenpcdist + betweenncdist;

			return 0.0 - result;
		}
	}
*/

}
