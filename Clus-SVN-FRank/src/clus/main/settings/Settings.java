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

package clus.main.settings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import clus.data.type.ClusSchema;
import clus.data.type.IntegerAttrType;
import clus.util.jeans.io.ini.INIFile;
import clus.util.jeans.io.ini.INIFileSection;
import clus.util.jeans.resource.ResourceInfo;
import clus.util.jeans.util.StringUtils;
import clus.util.jeans.util.cmdline.CMDLineArgs;


/**
 * All the settings. Includes the command line parameters as boolean class attributes.
 * The settings file attributes are included by get* methods.
 * 
 * @author User
 *
 */
public class Settings implements Serializable {

    public final static long SERIAL_VERSION_ID = 1L;
    public final static long serialVersionUID = SERIAL_VERSION_ID;

    /***********************************************************************
     * Create the settings structure *
     ***********************************************************************/
    private INIFile m_Ini;

    private SettingsGeneric m_SettGeneric;
    private SettingsGeneral m_SettGeneral;
    private SettingsData m_SettData;
    private SettingsAttribute m_SettAttribute;
    private SettingsConstraints m_SettConstraints;
    private SettingsOutput m_SettOutput;
    private SettingsNominal m_SettNominal;
    private SettingsModel m_SettModel;
    private SettingsTree m_SettTree;
    private SettingsRules m_SettRules;
    private SettingsMLC m_SettMLC;
    private SettingsHMLC m_SettHMLC;
    private SettingsHMTR m_SettHMTR;
    private SettingsILevelC m_SettILevelC;
    private SettingsBeamSearch m_SettBeamSearch;
    private SettingsExhaustiveSearch m_SettExhaustiveSearch;
    private SettingsTimeSeries m_SettTimeSeries;
    private SettingsPhylogeny m_SettPhylogeny;
    private SettingsRelief m_SettRelief;
    private SettingsDistances m_SettDistances;
    private SettingsEnsemble m_SettEnsemble;
    private SettingsKNN m_SettKNN;
    private SettingsKNNTree m_SettKNNTree;
    private SettingsOptionTree m_SettOptionTree;
    private SettingsExperimental m_SettExperimental;
    private SettingsSIT m_SettSIT;


    public Settings() {
        m_Ini = new INIFile();

        m_SettGeneric = new SettingsGeneric();
        m_SettGeneral = new SettingsGeneral();
        m_SettData = new SettingsData();
        m_SettAttribute = new SettingsAttribute();
        m_SettNominal = new SettingsNominal();
        m_SettModel = new SettingsModel();
        m_SettTree = new SettingsTree();
        m_SettRules = new SettingsRules();
        m_SettMLC = new SettingsMLC();
        m_SettHMLC = new SettingsHMLC();
        m_SettILevelC = new SettingsILevelC();
        m_SettBeamSearch = new SettingsBeamSearch();
        m_SettExhaustiveSearch = new SettingsExhaustiveSearch();
        m_SettTimeSeries = new SettingsTimeSeries();
        m_SettPhylogeny = new SettingsPhylogeny();
        m_SettRelief = new SettingsRelief();
        m_SettDistances = new SettingsDistances();
        m_SettEnsemble = new SettingsEnsemble();
        m_SettKNN = new SettingsKNN();
        m_SettKNNTree = new SettingsKNNTree();
        m_SettOptionTree = new SettingsOptionTree();
        m_SettSIT = new SettingsSIT();
        m_SettExperimental = new SettingsExperimental();
        m_SettConstraints = new SettingsConstraints();
        m_SettHMTR = new SettingsHMTR(m_SettAttribute, m_SettGeneric);
        m_SettOutput = new SettingsOutput();
    }


    public SettingsGeneric getGeneric() {
        return m_SettGeneric;
    }


    public SettingsGeneral getGeneral() {
        return m_SettGeneral;
    }


    public SettingsData getData() {
        return m_SettData;
    }


    public SettingsAttribute getAttribute() {
        return m_SettAttribute;
    }


    public SettingsConstraints getConstraints() {
        return m_SettConstraints;
    }


    public SettingsOutput getOutput() {
        return m_SettOutput;
    }


    public SettingsNominal getNominal() {
        return m_SettNominal;
    }


    public SettingsModel getModel() {
        return m_SettModel;
    }


    public SettingsTree getTree() {
        return m_SettTree;
    }


    public SettingsRules getRules() {
        return m_SettRules;
    }


    public SettingsMLC getMLC() {
        return m_SettMLC;
    }


    public SettingsHMLC getHMLC() {
        return m_SettHMLC;
    }


    public SettingsHMTR getHMTR() {
        return m_SettHMTR;
    }


    public SettingsILevelC getILevelC() {
        return m_SettILevelC;
    }


    public SettingsBeamSearch getBeamSearch() {
        return m_SettBeamSearch;
    }


    public SettingsExhaustiveSearch getExhaustiveSearch() {
        return m_SettExhaustiveSearch;
    }


    public SettingsTimeSeries getTimeSeries() {
        return m_SettTimeSeries;
    }


    public SettingsPhylogeny getPhylogeny() {
        return m_SettPhylogeny;
    }


    public SettingsRelief getRelief() {
        return m_SettRelief;
    }


    public SettingsDistances getDistances() {
        return m_SettDistances;
    }


    public SettingsEnsemble getEnsemble() {
        return m_SettEnsemble;
    }


    public SettingsKNN getKNN() {
        return m_SettKNN;
    }


    public SettingsKNNTree getKNNTree() {
        return m_SettKNNTree;
    }


    public SettingsOptionTree getOptionTree() {
        return m_SettOptionTree;
    }


    public SettingsExperimental getExperimental() {
        return m_SettExperimental;
    }


    public SettingsSIT getSIT() {
        return m_SettSIT;
    }


    public void create() {
        // Initialize individual settings. Order of initialization is important (see dependencies in the constructor).

        ArrayList<INIFileSection> lst = new ArrayList<INIFileSection>();

        Collections.addAll(lst, 
                m_SettGeneral.create(),
                m_SettData.create(),
                m_SettAttribute.create(),
                m_SettConstraints.create(),
                m_SettOutput.create(),
                m_SettNominal.create(),
                m_SettModel.create(),
                m_SettTree.create(),
                m_SettRules.create(),
                m_SettMLC.create(),
                m_SettHMLC.create(),
                m_SettHMTR.create(),
                m_SettILevelC.create(),
                m_SettBeamSearch.create(),
                m_SettExhaustiveSearch.create(),
                m_SettTimeSeries.create(),
                m_SettPhylogeny.create(),
                m_SettRelief.create(),
                m_SettDistances.create(),
                m_SettEnsemble.create(),
                m_SettKNN.create(),
                m_SettKNNTree.create(),
                m_SettOptionTree.create(),
                m_SettExperimental.create(),
                m_SettSIT.create());

        for (INIFileSection sec : lst) {
            m_Ini.addNode(sec);
        }        
    }


    public void initNamedValues() {
        m_SettConstraints.setTreeMaxDepthNamedValue(-1, "Infinity");
        
        m_SettBeamSearch.m_TreeMaxSize.setNamedValue(-1, "Infinity");

        m_SettTree.setTreeSplitSamplingNamedValue(0, "None");
    }


    public void updateTarget(ClusSchema schema) {
        if (m_SettTree.checkHeuristic("SSPD")) {
            schema.addAttrType(new IntegerAttrType("SSPD"));
            int nb = schema.getNbAttributes();
            m_SettAttribute.m_Target.setValue(String.valueOf(nb));
        }
    }


    public void initialize(CMDLineArgs cargs, boolean loads) throws IOException {
        create();
        initNamedValues();

        if (loads) {
            try {
                String fname = m_SettGeneric.getFileAbsolute(m_SettGeneric.getAppName() + ".s");
                m_Ini.load(fname, '%');
            }
            catch (FileNotFoundException e) {
                System.out.println("No settings file found");
            }
        }
        if (cargs != null)
            process(cargs);

        m_SettData.updateDataFile(m_SettGeneric.getAppName() + ".arff");
        m_SettHMLC.initHierarchical();
    }


    public void process(CMDLineArgs cargs) {
        if (cargs.hasOption("target")) {
            m_SettAttribute.setTarget(cargs.getOptionValue("target"));
        }
        if (cargs.hasOption("disable")) {
            String disarg = cargs.getOptionValue("disable");
            String orig = m_SettAttribute.getDisabled();

            if (StringUtils.unCaseCompare(orig, ISettings.NONE)) {
                m_SettAttribute.setDisabled(disarg);
            }
            else {
                m_SettAttribute.setDisabled(orig + "," + disarg);
            }
        }
        if (cargs.hasOption("silent")) {
            SettingsGeneric.VERBOSE = 0;
        }
    }


    public void update(ClusSchema schema) {
        m_SettTree.setFTest(m_SettTree.getFTest(), m_SettGeneric.getVerbose());

        SettingsTree.MINIMAL_WEIGHT = m_SettModel.getMinimalWeight();
        SettingsTree.ONE_NOMINAL = (schema.getNbNominalTargetAttributes() == 1 && schema.getNbNumericTargetAttributes() == 0);

        SettingsOutput.SHOW_UNKNOWN_FREQ = m_SettOutput.isShowUnknown();
        SettingsOutput.SHOW_BRANCH_FREQ = m_SettOutput.isShowBranchFreq();

        SettingsExperimental.SHOW_XVAL_FOREST = m_SettExperimental.isShowXValForest();

        SettingsBeamSearch.SIZE_PENALTY = m_SettBeamSearch.getSizePenalty();
        SettingsBeamSearch.BEAM_WIDTH = m_SettBeamSearch.getBeamWidth();
        SettingsBeamSearch.BEAM_SIMILARITY = m_SettBeamSearch.getBeamSimilarity();
        SettingsBeamSearch.BEAM_SYNT_DIST_CONSTR = m_SettBeamSearch.hasBeamConstraintFile();

        SettingsGeneric.VERBOSE = m_SettGeneric.getVerbose();
    }


    public void updateDisabledSettings() {
        int pruning = m_SettTree.getPruningMethod();
        int heur = m_SettTree.getHeuristic();
        
        m_SettTree.setM5PruningMultEnabled(pruning == SettingsTree.PRUNING_METHOD_M5 || pruning == SettingsTree.PRUNING_METHOD_M5_MULTI);
        m_SettTree.set1SERuleEnabled(pruning == SettingsTree.PRUNING_METHOD_GAROFALAKIS_VSB);
        m_SettTree.setFTestEnabled(heur == SettingsTree.HEURISTIC_SSPD || heur == SettingsTree.HEURISTIC_VARIANCE_REDUCTION);
        
        m_SettData.setPruneSetMaxEnabled(!m_SettData.isPruneSetString(ISettings.NONE));
        
        if (ResourceInfo.isLibLoaded())
            m_SettGeneral.m_ResourceInfoLoaded.setSingleValue(SettingsGeneral.RESOURCE_INFO_LOAD_YES);
        else
            m_SettGeneral.m_ResourceInfoLoaded.setSingleValue(SettingsGeneral.RESOURCE_INFO_LOAD_NO);
    }


    public void show(PrintWriter where) throws IOException {
        updateDisabledSettings();

        // For TreeToRules PredictionMethod might have been temporarily put to DecisionList instead of some other
        boolean tempInduceParamNeeded = m_SettRules.getRuleInduceParamsDisabled(); // They were changed in the first place

        if (m_SettRules.getCoveringMethod() == SettingsRules.COVERING_METHOD_RULES_FROM_TREE && tempInduceParamNeeded)
            m_SettRules.returnRuleInduceParams();

        m_Ini.save(where);

        if (m_SettRules.getCoveringMethod() == SettingsRules.COVERING_METHOD_RULES_FROM_TREE && tempInduceParamNeeded)
            m_SettRules.disableRuleInduceParams();
    }

}