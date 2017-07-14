
package clus.algo.rules.probabilistic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import clus.algo.rules.ClusRule;
import clus.algo.rules.ClusRuleSet;
import clus.main.ClusRun;
import clus.main.ClusStatManager;
import clus.model.ClusModel;
import clus.model.ClusModelInfo;
import clus.model.test.NodeTest;


public class ClusRuleHelperMethods {

    /* HELPER METHODS */
    static void debug_RemoveExistingData(String folderName) {
        try {
            if (new File(folderName).exists()) {
                for (File ff : new File(folderName).listFiles())
                    ff.delete();
            }
        }
        catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }


    static void debug_PrintToFile(String data, String folderName, String fileName) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(folderName + "/" + fileName);

        pw.print(data);

        pw.flush();
        pw.close();
    }


    static void debug_PrintInitialRules(ClusRuleSet ruleSet, String folderName, String fileName, boolean removeExistingData) throws FileNotFoundException {
        if (removeExistingData)
            debug_RemoveExistingData(folderName);

        debug_PrintRuleSet(ruleSet, folderName, fileName + "_rules.txt", false, false); // rules
        debug_PrintRuleSet(ruleSet, folderName, fileName + "_tests.txt", false, true); // tests
    }


    static void debug_PrintRuleSet(ClusRuleSet ruleSet, String folderName, String fileName, boolean printDefaultRule, boolean printTests) {
        try {
            File f = new File(folderName);
            boolean cancel = false;
            if (!f.exists() ||
                    (f.exists() && !f.isDirectory())) {
                if (f.mkdir()) {
                    System.out.println("Created rules.debug directory.");
                }
                else {
                    System.out.println("Unable to create rules.debug directory.\nWill not write rule debug info.");
                    cancel = true;
                }
            }

            if (!cancel) {
                PrintWriter pw = new PrintWriter(folderName + "/" + fileName);

                boolean tmp = ruleSet.getSettings().getRules().isPrintAllRules();

                if (printDefaultRule) {
                    ruleSet.getSettings().getRules().setPrintAllRules(true); // enable printing all rules
                    ruleSet.printModel(pw);
                    ruleSet.getSettings().getRules().setPrintAllRules(tmp); // return back to what was set before
                }
                else {
                    for (int i = 0; i < ruleSet.getModelSize(); i++) {
                        if (printTests) {
                            // print tests
                            ruleSet.getRule(i).printModelTests(pw);
                        }
                        else {
                            // print rules
                            ruleSet.getRule(i).printModel(pw);
                            pw.print("\r\n\r\n");
                        }
                    }
                }

                pw.flush();
                pw.close();
            }
        }
        catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }


    static HashMap<Integer, ArrayList<Integer>> CalculateCounts(ClusRuleSet rs) {
        HashMap<Integer, ArrayList<Integer>> hm = new HashMap<Integer, ArrayList<Integer>>();

        for (int i = 0; i < rs.getModelSize(); i++) {
            ArrayList<Integer> indicesOfRulesWithCardinality_i = hm.getOrDefault(rs.getRule(i).getModelSize(), new ArrayList<Integer>());
            indicesOfRulesWithCardinality_i.add(i);

            hm.put(rs.getRule(i).getModelSize(), indicesOfRulesWithCardinality_i);
        }

        return hm;
    }


    List<ClusRule> generateInitialRuleCombinations(ArrayList<NodeTest> tests, int numberOfTestsPerRule_Max, ClusStatManager manager) {
        List<ClusRule> generatedRules = new ArrayList<ClusRule>();

        for (int i = 1; i <= numberOfTestsPerRule_Max; i++) {
            generatedRules.addAll(generateCombinations(tests, i, manager));
        }

        return generatedRules;
    }


    List<ClusRule> generateCombinations(ArrayList<NodeTest> tests, int numberOfTestsPerRule, ClusStatManager manager) {
        List<ClusRule> subsets = new ArrayList<ClusRule>();

        int[] s = new int[numberOfTestsPerRule]; // here we'll keep indices 
        // pointing to elements in input array
        if (numberOfTestsPerRule <= tests.size()) {
            // first index sequence: 0, 1, 2, ...
            for (int i = 0; (s[i] = i) < numberOfTestsPerRule - 1; i++)
                ;
            subsets.add(getSubset(tests, s, manager));
            for (;;) {
                int i;
                // find position of item that can be incremented
                for (i = numberOfTestsPerRule - 1; i >= 0 && s[i] == tests.size() - numberOfTestsPerRule + i; i--)
                    ;
                if (i < 0) {
                    break;
                }
                else {
                    s[i]++; // increment this item
                    for (++i; i < numberOfTestsPerRule; i++) { // fill up remaining items
                        s[i] = s[i - 1] + 1;
                    }
                    subsets.add(getSubset(tests, s, manager));
                }
            }
        }

        return subsets;
    }


    ClusRule getSubset(ArrayList<NodeTest> tests, int[] subset, ClusStatManager manager) {
        ClusRule rule = new ClusRule(manager);

        for (int i = 0; i < subset.length; i++) {
            rule.addTest(tests.get(subset[i]));
        }

        return rule;
    }

}
