
package si.ijs.kt.clus.ext.ensemble.ros;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import si.ijs.kt.clus.main.settings.Settings;


public class ClusROSModelInfo implements Serializable {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

    private int m_TreeNumber;
    private HashMap<Integer, Integer> m_EnabledTargetAttributes; // key = target index, value = dataset index; this is a
                                                                 // map of ENABLED targets
    private HashSet<Integer> m_DisabledTargetAttributes; // key = target index, value = dataset index ; this is a map of
                                                         // DISABLED targets
    private int m_SizeOfSubspace;
    private String m_SubspaceString;
    private int m_SizeOfSubspaceSetting;
    private int m_NbTargetAttributes;

    private ArrayList<ClusROSModelInfo> m_Children;


    public void addChild(ClusROSModelInfo info) {
        m_Children.add(info);
    }


    public boolean isRandom() {
        return m_SizeOfSubspaceSetting < 0;
    }


    public boolean isRandomPerTree() {
        return isRandom() && m_SizeOfSubspaceSetting < -1;
    }


    public ArrayList<ClusROSModelInfo> getChildren() {
        return m_Children;
    }


    public int getNbChildren() {
        return m_Children.size();
    }


    public boolean isTargetEnabled(int targetIndex) {
        return m_EnabledTargetAttributes.keySet().contains(targetIndex);
    }


    public int getTreeNumber() {
        return m_TreeNumber;
    }


    public int getSizeOfSubspace() {
        return m_SizeOfSubspace;
    }


    public Set<Integer> getTargets() {
        return m_EnabledTargetAttributes.keySet();
    }


    public Set<Integer> getTargetsDisabled() {
        return m_DisabledTargetAttributes;
    }


    public String getSubspaceString() {
        return m_SubspaceString;
    }


    public ClusROSModelInfo(int treeNumber, int subspaceSizeSetting, HashMap<Integer, Integer> enabledTargetAttributes, int nbTargetAttributes) {
        m_TreeNumber = treeNumber;
        m_EnabledTargetAttributes = enabledTargetAttributes;
        m_DisabledTargetAttributes = new HashSet<>();
        m_SizeOfSubspace = enabledTargetAttributes.size();
        m_NbTargetAttributes = nbTargetAttributes;

        m_SizeOfSubspaceSetting = subspaceSizeSetting;

        m_SubspaceString = String.join(" ", m_EnabledTargetAttributes.keySet().stream().map(m -> m.intValue()).sorted().map(m -> Integer.toString(m)).collect(Collectors.toList()));

        m_Children = new ArrayList<>();

        for (int i = 0; i < nbTargetAttributes; i++) {
            if (!isTargetEnabled(i)) {
                m_DisabledTargetAttributes.add(i);
            }
        }
    }


    @Override
    public ClusROSModelInfo clone() {
        return new ClusROSModelInfo(m_TreeNumber, m_SizeOfSubspaceSetting, m_EnabledTargetAttributes, m_NbTargetAttributes);
    }


    public ClusROSModelInfo initWithNewSubspace(HashMap<Integer, Integer> newSubspace) {
        return new ClusROSModelInfo(m_TreeNumber, m_SizeOfSubspaceSetting, newSubspace, m_NbTargetAttributes);
    }


    @Override
    public String toString() {
        return String.format("Tree Number: %d | Subspace: %s | SizeOfSubspace: %s | NbChildren: %d", m_TreeNumber, m_SubspaceString, m_SizeOfSubspace, m_Children.size());
    }
}
