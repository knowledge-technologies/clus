package sit.mtLearner;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;

import sit.TargetSet;
import clus.data.rows.DataTuple;
import clus.data.rows.RowData;
import clus.data.type.ClusAttrType;
import clus.data.type.ClusSchema;
import clus.data.type.NumericAttrType;

public class KNNLearner extends MTLearnerImpl {

	@Override
	protected RowData[] LearnModel(TargetSet targets, RowData train,RowData test) {

		String appName = m_Sett.getAppName();

		writeCSV("train.csv"+appName,targets,train);
		writeCSV("test.csv"+appName,targets,test);

		/*
		System.out.println(train.getNbRows());
		System.out.println(test.getNbRows());
		*/

		NumericAttrType[] descriptive = test.m_Schema.getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);
		int nrFeatures = descriptive.length;
		int nrTargets = targets.size();

		try {
			///ga_basic_SIT <config file> <fold size> <# features> <# targets> <training data> <test data> <output file name>

			int benchmk_cnt = (train.getNbRows());
			// /data/home/u0051096/top40/
			String[] commands = new String[]{"/home/beau/SIT_evaluation/gent/top40/ga_basic_SIT","config.txt",
					 test.getNbRows()+"",nrFeatures+"",nrTargets+"","train.csv"+appName,"test.csv"+appName,"result.csv"+appName,benchmk_cnt+""};



			for(int i = 0;i<commands.length;i++){
				System.out.print(commands[i]+" ");
			}
	//		System.out.println();

			//commands = new String[]{"/home/beau/SIT_evaluation/gent/top40/ga_basic_SIT"};

			Process child = Runtime.getRuntime().exec(commands);
			String line;
			BufferedReader input =
		        new BufferedReader
		          (new InputStreamReader(child.getInputStream()));
		      while ((line = input.readLine()) != null) {
		     //   System.out.println(line);
		      }



			child.waitFor();


		} catch (IOException e) {
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RowData predictions = new RowData(test.m_Schema,test.getNbRows());


		readResult(targets,predictions);
		RowData[] result = {test,predictions};

		return result;
	}

	public String getName() {
		return "KNN";
	}

	private RowData readResult(TargetSet targets,RowData result){

		try {
			FileReader input = new FileReader("result.csv"+m_Sett.getAppName());
			BufferedReader bufRead = new BufferedReader(input);
			String line = bufRead.readLine();

			int count = 0;
			while (line != null){
				DataTuple t = parseLine(line,targets,result.m_Schema);
				result.setTuple(t,count);
				count++;
				line = bufRead.readLine();

			}
			bufRead.close();
			if(count==0){
				System.err.println("No results from KNN found???");
			}

		}catch (ArrayIndexOutOfBoundsException e){
			System.out.println("no results file found?");
		}catch (IOException e){
			e.printStackTrace();
		}


		return result;
	}

	private DataTuple parseLine(String line,TargetSet targets,ClusSchema schema){
		DataTuple t = new DataTuple(schema);

		Iterator trgts = targets.iterator();

		String[] values = line.split(",");
		Double[] doubles = new Double[values.length];

		for(int i=0;i<values.length;i++){
			doubles[i] = Double.parseDouble(values[i]);

		}

		int count = 0;
		while(trgts.hasNext()){
			NumericAttrType atr = (NumericAttrType) trgts.next();
			atr.setNumeric(t,doubles[count]);
			count++;
		}


		return t;
	}

	private void writeCSV(String fname,TargetSet targets, RowData data){
		ClusSchema schema = m_Data.getSchema();
		NumericAttrType[] descriptive = schema.getNumericAttrUse(ClusAttrType.ATTR_USE_DESCRIPTIVE);


		PrintWriter p = null;
		try {
			p = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
		} catch (FileNotFoundException e) {e.printStackTrace();}

		for(int ti = 0;ti<data.getNbRows();ti++){
			DataTuple t = data.getTuple(ti);
			for(int i=0;i<descriptive.length;i++){
				double d = descriptive[i].getNumeric(t);
				p.print(d+",");
			}

			Iterator<ClusAttrType> i = targets.iterator();
			while(i.hasNext()){
				double d = i.next().getNumeric(t);
				if(i.hasNext())
					p.print(d+",");
				else
					p.print(d);
			}
			p.println();
		}
		p.flush();
	}

}
