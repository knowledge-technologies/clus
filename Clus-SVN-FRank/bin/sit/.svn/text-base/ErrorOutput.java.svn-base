package sit;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import clus.error.MSError;
import clus.main.Settings;

public class ErrorOutput {
	protected PrintWriter m_Writer;
	protected Settings m_Sett;

	public ErrorOutput(Settings sett){
		this.m_Sett=sett;
		try {
			m_Writer = m_Sett.getFileAbsoluteWriter(m_Sett.getAppName()+".err");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void writeHeader() throws IOException {
		m_Writer.println("@relation experiment");
		m_Writer.println("@attribute Dataset {"+m_Sett.getDataFile()+"}");
		m_Writer.println("@attribute Run numeric");
		m_Writer.println("@attribute Fold numeric");//TODO automaticly adjust to nr of folds, 10 is the default
		m_Writer.println("@attribute Learner string");
		m_Writer.println("@attribute Search string");
		m_Writer.println("@attribute MainTarget numeric");
		//the errors

		m_Writer.print("@attribute ");
		String errName = m_Sett.getError();
		m_Writer.print(errName+" numeric\n");

		//support targets
		m_Writer.println("@attribute SupportTargets string");
		m_Writer.println("@attribute Runtime numeric");
		m_Writer.println("@data");

		m_Writer.flush();
	}

	public void addFold(int run,int fold, String learner,String search,String mt, double error,String sts, Long dif){
		m_Writer.println(m_Sett.getDataFile()+","+run+","+fold+","+learner+","+search+","+mt+","+error+","+sts+","+dif/1000.0);
		m_Writer.flush();
	}


	public void addFoldAllErrors(int run,int fold, String learner,String search,String mt, MSError error,String sts, Long dif){
		m_Writer.print(m_Sett.getDataFile()+","+run+","+fold+","+learner+","+search+","+mt+",");

		for(int i = 0;i<error.getDimension();i++){
			//m_Writer.print(error.getModelErrorComponent(i)+",");

		//	m_Writer.println("@attribute Target"+i+" numeric");
		}

		m_Writer.print(error.getModelError()+",");

		m_Writer.print(sts+","+dif/1000.0);
		m_Writer.println();
		m_Writer.flush();
	}


}
