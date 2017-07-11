/*************************************************************************
 * Clus - Software for Predictive Clustering                             *
 * Copyright (C) 2007                                                    *
 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
 *                                                                       *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 *                                                                       *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 *                                                                       *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
 *                                                                       *
 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
 *************************************************************************/

package clus.ext.optiontree;

import java.io.IOException;

import clus.Clus;
import clus.algo.ClusInductionAlgorithm;
import clus.algo.ClusInductionAlgorithmType;
import clus.data.ClusSchema;
import clus.main.ClusRun;
import clus.main.settings.Settings;
import clus.model.ClusModel;
import clus.util.ClusException;
import clus.util.jeans.util.cmdline.CMDLineArgs;

public class ClusOptionTree extends ClusInductionAlgorithmType {

    public void printInfo() {
        System.out.println("TDIOT");
        System.out.println("Heuristic: " + getStatManager().getHeuristicName());
    }

    public ClusOptionTree(Clus clus) {
        super(clus);
    }

    @Override
    public ClusInductionAlgorithm createInduce(ClusSchema schema, Settings sett, CMDLineArgs cargs) throws ClusException, IOException {
        return new DepthFirstInduceWithOptions(schema, sett);
    }

    @Override
    public ClusModel pruneSingle(ClusModel model, ClusRun cr) throws ClusException, IOException {
        return model;
    }
    
    @Override
    public void pruneAll(ClusRun cr) throws ClusException, IOException {
        // TODO Auto-generated method stub
    }
}
