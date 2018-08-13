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

/*
 * Created on May 12, 2005
 */

package si.ijs.kt.clus.pruning;

import si.ijs.kt.clus.algo.tdidt.ClusNode;
import si.ijs.kt.clus.data.attweights.ClusAttributeWeights;
import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.statistic.ClusStatistic;


// import si.ijs.kt.clus.weka.*;

public class M5Pruner extends PruneTree {

    double m_PruningMult = 2;
    double m_GlobalDeviation;
    ClusAttributeWeights m_ClusteringWeights;
    RowData m_TrainingData;


    public M5Pruner(ClusAttributeWeights prod, double mult) {
        m_ClusteringWeights = prod;
        m_PruningMult = mult;
    }


    @Override
    public void prune(ClusNode node) {
        ClusStatistic stat = node.getClusteringStat();
        m_GlobalDeviation = Math.sqrt(stat.getSVarS(m_ClusteringWeights) / stat.getTotalWeight());
        //if pruning according to target stat
        //m_GlobalDeviation = Math.sqrt(stat.getSVarS(m_ClusteringWeights) / stat.getTargetSumWeights());
        pruneRecursive(node);
        // ClusLogger.info("Performing test of M5 pruning");
        // TestM5PruningRuleNode.performTest(orig, node, m_GlobalDeviation, m_TargetWeights, m_TrainingData);
    }


    @Override
    public int getNbResults() {
        return 1;
    }


    private double pruningFactor(double num_instances, int num_params) {
        if (num_instances <= num_params) { return 10.0; // Caution says Yong in his code
        }
        return ((num_instances + m_PruningMult * num_params) / (num_instances - num_params));
    }


    public void pruneRecursive(ClusNode node) {
        if (node.atBottomLevel()) { return; }
        for (int i = 0; i < node.getNbChildren(); i++) {
            ClusNode child = (ClusNode) node.getChild(i);
            pruneRecursive(child);
        }
        
        //FIXME: should we prune according to clustering or target stat?
        ClusStatistic stat = node.getClusteringStat();
        double rmsLeaf = Math.sqrt(stat.getSVarS(m_ClusteringWeights)/stat.getTotalWeight()); //stat.getRMSE(m_ClusteringWeights);
        double adjustedErrorLeaf = rmsLeaf * pruningFactor(stat.getTotalWeight(), 1);
        double rmsSubTree = Math.sqrt(node.estimateClusteringSS(m_ClusteringWeights) / stat.getTotalWeight());
        double adjustedErrorTree = rmsSubTree * pruningFactor(stat.getTotalWeight(), node.getModelSize());
        
        //pruning according to target stat
        //RegressionStat stat = (RegressionStat)node.getTargetStat();
        //double rmsLeaf = Math.sqrt(stat.getSVarS(m_ClusteringWeights)/stat.getTargetSumWeights());
        //double adjustedErrorLeaf = rmsLeaf * pruningFactor(stat.getTargetSumWeights(), 1);
        //double rmsSubTree = Math.sqrt(node.estimateTargetSS(m_ClusteringWeights)/stat.getTargetSumWeights());
        //double adjustedErrorTree = rmsSubTree * pruningFactor(stat.getTargetSumWeights(), node.getModelSize());
        
        // ClusLogger.info("C leaf: "+rmsLeaf+" tree: "+rmsSubTree);
        // ClusLogger.info("C leafadj: "+adjustedErrorLeaf +" treeadj: "+rmsSubTree);
        if ((adjustedErrorLeaf <= adjustedErrorTree) || (adjustedErrorLeaf < (m_GlobalDeviation * 0.00001))) {
            node.makeLeaf();
        }
    }


    @Override
    public void setTrainingData(RowData data) {
        m_TrainingData = data;
    }
}
