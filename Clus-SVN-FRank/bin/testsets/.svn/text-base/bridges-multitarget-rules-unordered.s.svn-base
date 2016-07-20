
% *RUN*
% clus -rules %f.s
% cdiff %f.out orig/%f.out.orig

% *CLEAN*
% rm -f %f.out %f.xval %f.model %f.test.pred.arff %f.train.*.pred.arff %f.train.*.id

[Data]
File = ../data/bridges/bridges.arff
RemoveMissingTarget = Yes

[Tree]
Heuristic = RDispersionMlt

[Rules]
CoveringWeight = 0
PrintRuleWiseErrors = Yes
%CoveringMethod = Standard
CoveringMethod = WeightedError
ComputeDispersion = Yes

[Attributes]
ClusteringWeights = 
  TargetWeight = 1
  NonTargetWeight = 0
  NumericWeight = 1.0
  NominalWeight = 1.0 
Target = 9,10,11,12,13
Disable = 1,9-13
Clustering = 2-8,9,10,11,12,13

[Output]
AllFoldErrors = Yes

[Model]
MinimalWeight = 2

