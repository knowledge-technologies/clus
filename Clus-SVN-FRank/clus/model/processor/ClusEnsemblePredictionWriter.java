package clus.model.processor;

import java.io.IOException; 
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;

import jeans.util.MyArray;
import jeans.util.StringUtils;
import clus.data.io.ARFFFile;
import clus.data.rows.DataTuple;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.main.Settings;
import clus.statistic.ClusStatistic;
import clus.statistic.RegressionStatBase;
import clus.util.ClusException;
import clus.util.ClusFormat;

/**
 * Writing the predictions from the ensemble in a separate file, 
 * their standard deviations from the voting procedure and 
 * the respective votes from each base classifier.
 * This implementation can be applied only for (multi-target) regression. * 
 * 
 * @author dkocev
 */
public class ClusEnsemblePredictionWriter extends ClusModelProcessor{
	
	protected ClusSchema m_EnsPredSchema;
	protected Settings m_Sett;
	protected String m_Fname;
	protected MyArray m_Attrs;
	protected PrintWriter m_Writer;
	protected ArrayList m_AttributeNames; 
	protected int m_NbTargetAttributes;
	static int m_Type; //training or testing
	protected double[] m_StDev;
	static ArrayList m_Votes = new ArrayList();
	protected NumberFormat m_Format = ClusFormat.FOUR_AFTER_DOT;
	boolean m_Initialized = false;
	
	public ClusEnsemblePredictionWriter(String fname) {
		m_Fname = fname;
		m_AttributeNames = new ArrayList();
	}
	
	public ClusEnsemblePredictionWriter(String fname, ClusSchema schema, Settings sett) {
		m_Fname = fname;
		m_AttributeNames = new ArrayList();
		m_NbTargetAttributes = schema.getNbTargetAttributes();
		m_Sett = sett;
		m_Initialized = true;
		try{
			doInitialize(schema);
			ARFFFile.writeArffHeader(m_Writer, m_EnsPredSchema);
			m_Writer.flush();
			addEnsPredHeader(schema);
			m_Writer.flush();
		}catch (Exception e) {
			System.err.println("Error while writing ensemble prediction header!");
		}
		
	}
	
	
	private void addEnsPredHeader(ClusSchema schema) throws ClusException{
		int nb = schema.getNbAttributes();
		for (int i = 0; i < nb; i++) {
			ClusAttrType at = schema.getAttrType(i);
			if (at.getStatus() == ClusAttrType.STATUS_TARGET) {
				m_Writer.print("@ATTRIBUTE ");
				m_Writer.print(StringUtils.printStr(at.getName()+"-pred", 65));
				at.writeARFFType(m_Writer);
				m_Writer.println();
			}
		}
		
		for (int j = 0; j < nb; j++) {
			ClusAttrType at = schema.getAttrType(j);
			if (at.getStatus() == ClusAttrType.STATUS_TARGET) {
				m_Writer.print("@ATTRIBUTE ");
				m_Writer.print(StringUtils.printStr(at.getName()+"-stdev", 65));
				at.writeARFFType(m_Writer);
				m_Writer.println();
			}
		}
		m_Writer.println();
		m_Writer.print("@DATA");
		m_Writer.println();
	}
	
	private void doInitialize(ClusSchema schema) throws IOException, ClusException {
		m_Attrs = new MyArray();
		int nb = schema.getNbAttributes();
		m_EnsPredSchema = new ClusSchema(StringUtils.removeSingleQuote(schema.getRelationName())+"-pred-distr");
		m_EnsPredSchema.setSettings(schema.getSettings());
		for (int i = 0; i < nb; i++) {
			ClusAttrType at = schema.getAttrType(i);
			if (at.getStatus() == ClusAttrType.STATUS_KEY) {
				m_Attrs.addElement(at);
				m_AttributeNames.add(at.getName());
				m_EnsPredSchema.addAttrType(at.cloneType());
			}
		}
		for (int j = 0; j < nb; j++) {
			ClusAttrType at = schema.getAttrType(j);
			if (at.getStatus() == ClusAttrType.STATUS_TARGET) {
				m_Attrs.addElement(at);
				m_AttributeNames.add(at.getName());
				m_EnsPredSchema.addAttrType(at.cloneType());
			}
		}
		m_Writer = m_Sett.getFileAbsoluteWriter(m_Fname);
	}
	
	public void writePredictionsForTuple(DataTuple tuple, ClusStatistic distr){
		m_Writer.print(getRealValues(tuple) + ",");
		m_Writer.print(getPrediction(distr) + ",");
		String[] voting = processVotes(m_Votes);
		m_Writer.print(getPredictionStDev() + "\n");
		m_Writer.flush();
		for (int i = 0; i < m_NbTargetAttributes; i++){
			m_Writer.print("% Target = " + m_AttributeNames.get(i) + ": " + voting[i] + "\n");
			m_Writer.flush();
		}
	}
	
	private String getRealValues(DataTuple tuple) {
		String real = "";
		for (int j = 0; j < m_Attrs.size(); j++) {
			if (j != 0) real += ",";
			ClusAttrType at = (ClusAttrType)m_Attrs.elementAt(j);
			real += at.getPredictionWriterString(tuple);
		}
		return real;
	}
	
	private String getPrediction(ClusStatistic distr) {
		String result = "";
		double[] pred = distr.getNumericPred();
		for (int i = 0; i < pred.length; i++) {
			if (i != 0) result+= ",";
			if (pred != null) {
				result +=  m_Format.format(pred[i]);
			} else {
				result += "?";
			}
		}
		return result;
	}
	
	private String getPredictionStDev() {
		String result = "";
		for (int i = 0; i < m_StDev.length; i++) {
			if (i != 0) result+= ",";
				result +=  m_Format.format(m_StDev[i]);
		}
		return result;
	}
	
	private String[] processVotes(ArrayList votes){
		String[] result = new String[m_NbTargetAttributes];
		m_StDev = new double[m_NbTargetAttributes];
		double[][] predicts = new double[m_NbTargetAttributes][votes.size()];
		
		for (int i = 0; i < votes.size(); i++){
			RegressionStatBase stat = (RegressionStatBase) votes.get(i);
			if (i == 0){
				for (int j = 0; j < stat.getNbAttributes(); j++){
					predicts[j][i] = stat.getMean(j);
					result[j] = "" +  m_Format.format(predicts[j][i]);
				}
			}else{
				for (int j = 0; j < stat.getNbAttributes(); j++){
					predicts[j][i] = stat.getMean(j);
					result[j] = result[j] + "," +  m_Format.format(predicts[j][i]);
				}
			}
		}
		m_StDev = calcStDev(predicts);
		return result;
	}
	
	private double[] calcStDev(double[][] values){
		double[] result = new double[values.length];
		for (int i = 0; i < result.length; i++)
			result[i] = stDevOpt(values[i]);
		return result;
	}

	
	private double stDevOpt(double[] a){
		double avg = 0;;
		double summ = 0;
		for (int i = 0; i < a.length; i++){
			avg += a[i];
			summ += a[i] * a[i];
		}
		avg = avg / a.length;
		double sd = (summ - a.length * avg * avg)/(a.length -1);
		return Math.sqrt(sd);
	}
	
	public static int getType(){
		return m_Type;
	}
	
	public static void setType(int type){
		m_Type = type;
	}
	
	public void closeWriter(){
		System.out.println("Ensemble predictions written in " + m_Fname);
		m_Writer.flush();
		m_Writer.close();
	}
	
	public static void setVotes(ArrayList votes){
		m_Votes = votes;
	}
	
	public void printVotes(){
		System.out.println("Votes: " + m_Votes);
	}
	
	public boolean isInitialized(){
		return m_Initialized;
	}	
}
