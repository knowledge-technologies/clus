
package si.ijs.kt.clus.addon.sit.mtLearner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;

import si.ijs.kt.clus.addon.sit.TargetSet;
import si.ijs.kt.clus.data.ClusSchema;
import si.ijs.kt.clus.data.rows.DataTuple;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.data.type.ClusAttrType;
import si.ijs.kt.clus.data.type.ClusAttrType.AttributeUseType;
import si.ijs.kt.clus.data.type.primitive.NumericAttrType;
import si.ijs.kt.clus.util.ClusLogger;


public class KNNLearner extends MTLearnerImpl {

    @Override
    protected RowData[] LearnModel(TargetSet targets, RowData train, RowData test) {

        String appName = m_Sett.getGeneric().getAppName();

        writeCSV("train.csv" + appName, targets, train);
        writeCSV("test.csv" + appName, targets, test);

        /*
         * ClusLogger.info(train.getNbRows());
         * ClusLogger.info(test.getNbRows());
         */

        NumericAttrType[] descriptive = test.m_Schema.getNumericAttrUse(AttributeUseType.Descriptive);
        int nrFeatures = descriptive.length;
        int nrTargets = targets.size();

        try {
            /// ga_basic_SIT <config file> <fold size> <# features> <# targets> <training data> <test data> <output file
            /// name>

            int benchmk_cnt = (train.getNbRows());
            // /data/home/u0051096/top40/
            String[] commands = new String[] { "/home/beau/SIT_evaluation/gent/top40/ga_basic_SIT", "config.txt", test.getNbRows() + "", nrFeatures + "", nrTargets + "", "train.csv" + appName, "test.csv" + appName, "result.csv" + appName, benchmk_cnt + "" };

            for (int i = 0; i < commands.length; i++) {
                System.out.print(commands[i] + " ");
            }
            // ClusLogger.info();

            // commands = new String[]{"/home/beau/SIT_evaluation/gent/top40/ga_basic_SIT"};

            Process child = Runtime.getRuntime().exec(commands);
            //String line;
            //BufferedReader input = new BufferedReader(new InputStreamReader(child.getInputStream()));
            //while ((line = input.readLine()) != null) {
                // ClusLogger.info(line);
            //}

            child.waitFor();

        }
        catch (IOException e) {}
        catch (InterruptedException e) {
            
            e.printStackTrace();
        }

        RowData predictions = new RowData(test.m_Schema, test.getNbRows());

        readResult(targets, predictions);
        RowData[] result = { test, predictions };

        return result;
    }


    @Override
    public String getName() {
        return "KNN";
    }


    private RowData readResult(TargetSet targets, RowData result) {

        try {
            FileReader input = new FileReader("result.csv" + m_Sett.getGeneric().getAppName());
            BufferedReader bufRead = new BufferedReader(input);
            String line = bufRead.readLine();

            int count = 0;
            while (line != null) {
                DataTuple t = parseLine(line, targets, result.m_Schema);
                result.setTuple(t, count);
                count++;
                line = bufRead.readLine();

            }
            bufRead.close();
            if (count == 0) {
                System.err.println("No results from KNN found???");
            }

        }
        catch (ArrayIndexOutOfBoundsException e) {
            ClusLogger.info("no results file found?");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    private DataTuple parseLine(String line, TargetSet targets, ClusSchema schema) {
        DataTuple t = new DataTuple(schema);

        Iterator trgts = targets.iterator();

        String[] values = line.split(",");
        Double[] doubles = new Double[values.length];

        for (int i = 0; i < values.length; i++) {
            doubles[i] = Double.parseDouble(values[i]);

        }

        int count = 0;
        while (trgts.hasNext()) {
            NumericAttrType atr = (NumericAttrType) trgts.next();
            atr.setNumeric(t, doubles[count]);
            count++;
        }

        return t;
    }


    private void writeCSV(String fname, TargetSet targets, RowData data) {
        ClusSchema schema = m_Data.getSchema();
        NumericAttrType[] descriptive = schema.getNumericAttrUse(AttributeUseType.Descriptive);

        PrintWriter p = null;
        try {
            p = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int ti = 0; ti < data.getNbRows(); ti++) {
            DataTuple t = data.getTuple(ti);
            for (int i = 0; i < descriptive.length; i++) {
                double d = descriptive[i].getNumeric(t);
                p.print(d + ",");
            }

            Iterator<ClusAttrType> i = targets.iterator();
            while (i.hasNext()) {
                double d = i.next().getNumeric(t);
                if (i.hasNext())
                    p.print(d + ",");
                else
                    p.print(d);
            }
            p.println();
        }
        p.flush();
    }

}
