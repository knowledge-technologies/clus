
package si.ijs.kt.clus.ext.ensemble.ros;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import si.ijs.kt.clus.main.settings.Settings;


public class ClusROSModelInfo implements Serializable {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

    private int m_TreeNumber;
    private HashMap<Integer, Integer> m_EnabledTargetAttributes; // key = target index, value = dataset index
    private int m_SizeOfSubspace;
    private String m_SubspaceString;
    private int m_SizeOfSubspaceSetting;

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


    public String getSubspaceString() {
        return m_SubspaceString;
    }


    public ClusROSModelInfo(int treeNumber, int subspaceSizeSetting, HashMap<Integer, Integer> enabledTargetAttributes) {
        m_TreeNumber = treeNumber;
        m_EnabledTargetAttributes = enabledTargetAttributes;
        m_SizeOfSubspace = enabledTargetAttributes.size();

        m_SizeOfSubspaceSetting = subspaceSizeSetting;

        m_SubspaceString = String.join(" ", m_EnabledTargetAttributes.keySet().stream().map(m -> m.intValue()).sorted().map(m -> Integer.toString(m)).collect(Collectors.toList()));

        m_Children = new ArrayList<>();
    }


    @Override
    public ClusROSModelInfo clone() {
        return new ClusROSModelInfo(m_TreeNumber, m_SizeOfSubspaceSetting, m_EnabledTargetAttributes);
    }


    public ClusROSModelInfo initWithNewSubspace(HashMap<Integer, Integer> newSubspace) {
        return new ClusROSModelInfo(m_TreeNumber, m_SizeOfSubspaceSetting, newSubspace);
    }


    @Override
    public String toString() {
        return String.format("Tree Number: %d | Subspace: %s | SizeOfSubspace: %s | NbChildren: %d", m_TreeNumber, m_SubspaceString, m_SizeOfSubspace, m_Children.size());
    }
}
