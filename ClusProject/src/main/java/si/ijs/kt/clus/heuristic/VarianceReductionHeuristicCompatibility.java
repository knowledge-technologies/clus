/*************************************************************************
 * Clus - Software for Predictive Clustering *
 * Copyright (C) 2007 *
 * Katholieke Universiteit Leuven, Leuven, Belgium *
 * Jozef Stefan Institute, Ljubljana, Slovenia *
 * *
 * This program is free software: you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation, either version 3 of the License, or *
 * (at your option) any later version. *
 * *
 * This program is distributed in the hope that it will be useful, *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the *
 * GNU General Public License for more details. *
 * *
 * You should have received a copy of the GNU General Public License *
 * along with this program. If not, see <http://www.gnu.org/licenses/>. *
 * *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>. *
 *************************************************************************/

package si.ijs.kt.clus.heuristic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.main.settings.section.SettingsTree.SpatialMatrixType;
import si.ijs.kt.clus.main.settings.section.SettingsTree.SpatialMeasure;
import si.ijs.kt.clus.statistic.ClusStatistic;
import si.ijs.kt.clus.util.ClusLogger;
import si.ijs.kt.clus.util.FTest;


public class VarianceReductionHeuristicCompatibility extends ClusHeuristic {

    protected String m_BasicDist;
    //daniela
    protected RowData m_Data;
    protected int[] m_DataIndices; // the indices (in the original dataset) of the data at the current node
    protected ClusStatistic m_Pos, m_Neg;
    public static HashMap<Long, Double> m_distances = new HashMap<Long, Double>();
    public static HashMap<String, Double> m_distancesN = new HashMap<String, Double>();
    public static HashMap<String, Double> m_distancesS = new HashMap<String, Double>();
    //end daniela
    private boolean m_WarningGiven = false;


    public VarianceReductionHeuristicCompatibility(String basicdist, ClusStatistic negstat, ClusAttributeWeights targetweights, Settings sett) {
        super(sett);

        m_BasicDist = basicdist;
        m_ClusteringWeights = targetweights;
    }


    public VarianceReductionHeuristicCompatibility(ClusStatistic negstat, ClusAttributeWeights targetweights, Settings sett) {
        this(negstat.getDistanceName(), negstat, targetweights, sett);
    }


    @Override
    public double calcHeuristic(ClusStatistic tstat, ClusStatistic pstat, ClusStatistic missing) {
        // Acceptable?
        if (stopCriterion(tstat, pstat, missing)) { return Double.NEGATIVE_INFINITY; }
        // Compute |S|Var[S]
        double ss_tot = tstat.getSVarS(m_ClusteringWeights);
        double ss_pos = pstat.getSVarS(m_ClusteringWeights);
        double ss_neg = tstat.getSVarSDiff(m_ClusteringWeights, pstat);
        double value = FTest.calcVarianceReductionHeuristic(tstat.getTotalWeight(), ss_tot, ss_pos + ss_neg);
        if (getSettings().getGeneral().getVerbose() >= 10) {
            ClusLogger.info("TOT: " + tstat.getDebugString());
            ClusLogger.info("POS: " + pstat.getDebugString());
            ClusLogger.info("-> (" + ss_tot + ", " + ss_pos + ", " + ss_neg + ") " + value);
        }
        // NOTE: This is here for compatibility reasons only
        if (value < 1e-6)
            return Double.NEGATIVE_INFINITY;
        return value;
    }


    // daniela
    public double LanLongdistance(double lat1, double lon1, double lat2, double lon2) {
        double dist;
        if (lat1 == lat2 && lon1 == lon2) {
            dist = 0;
        }
        else {
            double theta = lon1 - lon2;
            dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
            dist = rad2deg(Math.acos(dist));
            dist = dist * 60 * 1.1515 * 1.609344;
        }
        return (dist);
    }


    public double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }


    public double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
    // daniela end


    @Override
    public String getName() {
        return "Variance Reduction with Distance '" + m_BasicDist + "', (" + m_ClusteringWeights.getName() + ") (FTest = " + FTest.getSettingSig() + ")";
    }


    // daniela
    //generate matrix & write to file
    public void generateMatrix(RowData data) throws IOException {
        //PrintWriter pw = new PrintWriter(new FileWriter("distances.txt"));            
        m_distances.clear();
        ClusSchema schema = data.getSchema();
        int N = data.getNbRows();
        double maxdist = 0;
        double d = 0;
        //double W = 0;
        double minDistLine = Double.POSITIVE_INFINITY;
        double maxOnMinDistLine = 0;
        double[][] w = new double[N][N];
        //max distance in the file
        for (int i = 0; i < N; i++) {
            minDistLine = Double.POSITIVE_INFINITY;
            for (int j = 0; j < N; j++) {
                DataTuple exi = data.getTuple(i);
                DataTuple exj = data.getTuple(j);
                ClusAttrType xt = schema.getNumericAttrUse(AttributeUseType.GIS)[0];
                ClusAttrType yt = schema.getNumericAttrUse(AttributeUseType.GIS)[1];
                double xi = xt.getNumeric(exi);
                double yi = yt.getNumeric(exi);
                double xj = xt.getNumeric(exj);
                double yj = yt.getNumeric(exj);
                Boolean longlat = schema.getSettings().getTree().isLonglat();
                if (longlat == true)
                    d = LanLongdistance(xi, yi, xj, yj);
                else
                    d = Math.sqrt((xi - xj) * (xi - xj) + (yi - yj) * (yi - yj));
                if (d > maxdist)
                    maxdist = d;
                if (d != 0 && d < minDistLine)
                    minDistLine = d;
            }
            if (minDistLine > maxOnMinDistLine)
                maxOnMinDistLine = minDistLine;
        }
        double bandwidth = schema.getSettings().getTree().getBandwidth();
        double b = bandwidth * maxdist;
        //ClusLogger.info(maxdist);
        if (maxOnMinDistLine != Double.POSITIVE_INFINITY && b < maxOnMinDistLine)
            b = maxOnMinDistLine;
        //calculate the weights 
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i > j) {
                    w[i][j] = w[j][i];
                   // W += w[i][j];
                    continue;
                }

                DataTuple exi = data.getTuple(i); //example i
                DataTuple exj = data.getTuple(j); //example j               
                ClusAttrType xti = schema.getNumericAttrUse(AttributeUseType.GIS)[0];
                ClusAttrType yti = schema.getNumericAttrUse(AttributeUseType.GIS)[1];
                double xii = xti.getNumeric(exi);
                double yii = yti.getNumeric(exi);
                double xji = xti.getNumeric(exj);
                double yji = yti.getNumeric(exj);
                Boolean longlat = schema.getSettings().getTree().isLonglat();
                if (longlat == true)
                    d = LanLongdistance(xii, yii, xji, yji);
                else
                    d = Math.sqrt((xii - xji) * (xii - xji) + (yii - yji) * (yii - yji));
                /*
                 * distance over descriptive and all atributes
                 * NumericAttrType[] arr = schema.getNumericAttrUse(AttributeUseType.Descriptive);
                 * int countA=arr.length; double dd=0;double d=0;
                 * double xi[] = new double[countA];double xj[] = new double[countA];
                 * for (int ii = 0; ii < countA; ii++) {
                 * ClusAttrType xt = schema.getNumericAttrUse(AttributeUseType.Descriptive)[ii];
                 * xi[ii] = xt.getNumeric(exi); xj[ii] = xt.getNumeric(exj);
                 * dd += (xi[ii]-xj[ii])*(xi[ii]-xj[ii]); //euc over all atrb}
                 */
                SpatialMatrixType spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();
                if (d >= b)
                    w[i][j] = 0;
                else {
                    if (d == 0)
                        w[i][j] = 1;
                    else {
                        switch (spatialMatrix) {
                            case Binary:
                                w[i][j] = 1; //0;   break;  //binary 
                            case Euclidean:
                                w[i][j] = 1 - d / b;
                                break; //euclidian
                            case Modified:
                                w[i][j] = (1 - (d * d) / (b * b)) * (1 - (d * d) / (b * b));
                                break; //modified
                            case Gaussian:
                                w[i][j] = Math.exp(-(d * d) / (b * b));
                                break; //gausian
                            default:
                                w[i][j] = 0;
                                break;
                        }
                        //W += w[i][j]; //ClusLogger.info(i+"\t"+j+"\t"+exi+"\t"+exj+"\t"+(long)(i*N+j)+"\t"+w[i][j]);
                    }
                    m_distances.put((long) (i * N + j), w[i][j]); //write to hasp map only, not in file
                    //pw.println(i+"#"+j+" "+d);
                    //pw.println(i+"\t"+W);     //write to file
                }
            }
        }
        //ClusLogger.info("spatialMatrix end: "+ System.currentTimeMillis());
        //pw.close();
    }


    public void readMatrixFromFile(RowData data) throws IOException {
        //PrintWriter pw = new PrintWriter(new FileWriter("distancesDistance.txt"));
        m_distancesS.clear();
        m_distancesN.clear();
        ClusSchema schema = data.getSchema();
        //int N = data.getNbRows();
        double maxdist = 0;
        double minDistLine = Double.POSITIVE_INFINITY;
        double maxOnMinDistLine = 0;
        double iEqual = Double.POSITIVE_INFINITY;
        String filename = "distances.csv";
        BufferedReader br = new BufferedReader(new FileReader(filename));
        double bandwidth = schema.getSettings().getTree().getBandwidth();
        double w;
        if (bandwidth != 100.0) {
            while (br.ready()) {
                String s = br.readLine();
                StringTokenizer st = new StringTokenizer(s, ",");
                Long ii = Long.parseLong(st.nextToken());
                Long jj = Long.parseLong(st.nextToken());
                double d = Double.parseDouble(st.nextToken());
                if (iEqual != ii) {
                    m_distancesN.put((ii + "#" + ii), 0.0);
                    iEqual = ii; // pw.println(ii+" "+ii+" "+0.0);
                }
                if (ii > jj) {
                    m_distancesN.put((jj + "#" + ii), d); //pw.println(jj+" "+ii+" "+d);
                }
                else {
                    m_distancesN.put((ii + "#" + jj), d); //pw.println(ii+" "+jj+" "+d);
                }
            }

            for (double d : m_distancesN.values()) {
                if (d > maxdist)
                    maxdist = d;
                if (d != 0 && d < minDistLine)
                    minDistLine = d;
            }
            if (minDistLine > maxOnMinDistLine)
                maxOnMinDistLine = minDistLine;
            double b = bandwidth * maxdist;
            if (maxOnMinDistLine != Double.POSITIVE_INFINITY && b < maxOnMinDistLine)
                b = maxOnMinDistLine;
            SpatialMatrixType spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();
            for (Map.Entry<String, Double> entry : m_distancesN.entrySet()) {
                String i = entry.getKey();
                double d = entry.getValue();
                if (d >= b)
                    w = 0;
                else {
                    if (d == 0)
                        w = 1;
                    else {
                        switch (spatialMatrix) {
                            case Binary:
                                w = 0;
                                break; //binary 
                            case Euclidean:
                                w = 1 - d / b;
                                break; //euclidian
                            case Modified:
                                w = (1 - (d * d) / (b * b)) * (1 - (d * d) / (b * b));
                                break; //modified
                            case Gaussian:
                                w = Math.exp(-(d * d) / (b * b));
                                break; //gausian
                            default:
                                w = 0;
                                break;
                        }
                    }
                    m_distancesS.put((i), w); //write to hasp map non-zero only, not in file
                    //ClusLogger.info((i)+" "+w);
                }
            }
        }
        else {
            while (br.ready()) {
                String s = br.readLine();
                StringTokenizer st = new StringTokenizer(s, ",");
                //Long ii=Long.parseLong(st.nextToken());
                //Long jj=Long.parseLong(st.nextToken());
                String ii = st.nextToken();
                String jj = st.nextToken();
                double d = Double.parseDouble(st.nextToken());
                SpatialMatrixType spatialMatrix = schema.getSettings().getTree().getSpatialMatrix();
                switch (spatialMatrix) {
                    case Binary:
                        if (d == 1)
                            w = 1;
                        else
                            w = 0;
                        break; //binary 
                    case Euclidean:
                        if (d == 1)
                            w = 0.01;
                        else
                            w = 1 - 1 / d;
                        break; //euclidian
                    default:
                        w = 0;
                        break;
                }
                //if (ii>jj) m_distancesS.put((jj+"#"+ii), w); else m_distancesS.put((ii+"#"+jj), w); 
                m_distancesS.put((ii + "#" + jj), w);
                m_distancesS.put((jj + "#" + ii), w);
            }
        }
        br.close();
    }


    public double calcI(ClusStatistic t_stat, ClusStatistic p_stat, ClusStatistic missing, Integer[] permutation) throws Exception {
        //try{
        //ClusStatistic tstat = (ClusStatistic)t_stat;
        ClusStatistic pstat = p_stat; // Acceptable?
        if (stopCriterion(t_stat, p_stat, missing)) { return Double.NEGATIVE_INFINITY; }

        double ss_pos = 0; //pstat.calcItotal();             
        ClusSchema schema = m_Data.getSchema();
        SpatialMeasure sm = schema.getSettings().getTree().getSpatialMeasure();
        if (!m_WarningGiven && !sm.equals(SpatialMeasure.GlobalMoran)) {
            m_WarningGiven = true;
            System.err.println("Warning: your spatial measure was not tested. Be careful.");
        }
        switch (sm) {
            case GlobalMoran:
                ss_pos = pstat.calcItotal(permutation);
                break; //"Global Moran"
            case GlobalGeary:
                ss_pos = pstat.calcGtotal(permutation);
                break; //"Global Geary"
            case GlobalGetis:
                ss_pos = pstat.calcGetisTotal(permutation);
                break;//"Global Getis"
            case LocalMoran:
                ss_pos = pstat.calcLISAtotal(permutation);
                break; //"Local Moran"
            case LocalGeary:
                ss_pos = pstat.calcGLocalTotal(permutation);
                break; //"Local Geary"
            case LocalGetis:
                ss_pos = pstat.calcLocalGetisTotal(permutation);
                break;//"Local Getis"
            case StandardizedGetis:
                ss_pos = pstat.calcGETIStotal(permutation);
                break;//Local Standardized Getis
            case EquvalentI:
                ss_pos = pstat.calcEquvalentItotal(permutation);
                break;//EquvalentI 
            case IwithNeighbours:
                ss_pos = pstat.calcIwithNeighbourstotal(permutation);
                break; //Global Moran with neigh
            case EquvalentIwithNeighbours:
                ss_pos = pstat.calcEquvalentIwithNeighbourstotal(permutation);
                break; // EquvalentI Global Moran with neigh
            case GlobalMoranDistance:
                ss_pos = pstat.calcItotalD(permutation);
                break; //"Global Moran with a separate distance file"
            case GlobalGearyDistance:
                ss_pos = pstat.calcGtotalD(permutation);
                break; //"Global Geary with a separate distance file"
            case CI:
                ss_pos = pstat.calcCItotal(permutation);
                break; // Connectivity Index (CI) for Graph Data
            case MultiVariateMoranI:
                ss_pos = pstat.calcMutivariateItotal(permutation);
                break; //Cross Moran (Wartenberg, 1985)
            case CwithNeighbours:
                ss_pos = pstat.calcCwithNeighbourstotal(permutation);
                break; //Global Geary with neigh
            case Lee:
                ss_pos = pstat.calcBivariateLee(permutation);
                break; //Bivariate Lee's measure: integration of Moran&Pearson coef. (Lee, 2001)
            case MultiIwithNeighbours:
                ss_pos = pstat.calcMultiIwithNeighbours(permutation);
                break; //Cross Moran (Wartenberg, 1985) with neigh
            case CIwithNeighbours:
                ss_pos = pstat.calcCIwithNeighbours(permutation);
                break; // Connectivity Index (CI) for Graph Data with neigh
            case LeewithNeighbours:
                ss_pos = pstat.calcLeewithNeighbours(permutation);
                break; //Bivariate Lee's measure with neigh
            case Pearson:
                ss_pos = pstat.calcPtotal(permutation);
                break; //Global Moran without weights i.e Pearson c. coef.
            case CIDistance:
                ss_pos = pstat.calcCItotalD(permutation);
                break; // Connectivity Index (CI) for Graph Data with a separate distance file
            case DH:
                ss_pos = pstat.calcDHtotalD(permutation);
                break; // Dyadicity and Heterophilicity for Graph Data with a separate distance file
            case EquvalentIDistance:
                ss_pos = pstat.calcEquvalentIDistance(permutation);
                break; // EquvalentI with a separate distance file
            case PearsonDistance:
                ss_pos = pstat.calcPDistance(permutation);
                break; //Pearson c. coef. with a separate distance file
            case EquvalentG:
                ss_pos = pstat.calcEquvalentGtotal(permutation);
                break; //EquvalentG, (Global Geary)
            case EquvalentGDistance:
                ss_pos = pstat.calcEquvalentGDistance(permutation);
                break; //EquvalentGDistance, EquvalentG with a separate distance file
            case EquvalentPDistance:
            	ss_pos = pstat.calcEquvalentPDistance(permutation);
            	break;
        }

        return ss_pos;
    }

    // end daniela
}
