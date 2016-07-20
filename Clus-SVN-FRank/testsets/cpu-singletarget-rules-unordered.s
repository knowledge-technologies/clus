
% *RUN*
% clus -rules %f.s
% cdiff %f.out orig/%f.out.orig

% *CLEAN*
% rm -f %f.out %f.xval %f.model %f.test.pred.arff %f.train.*.pred.arff %f.train.*.id

[Data]
File = ../data/cpu/cpu.arff
RemoveMissingTarget = Yes

[Tree]
Heuristic = RDispersionMlt

[Rules]
CoveringWeight = 0
RuleAddingMethod = IfBetter
PrintRuleWiseErrors = Yes
CoveringMethod = WeightedError
ComputeDispersion = Yes

[Attributes]
ClusteringWeights = 
  TargetWeight = 1
  NonTargetWeight = 0
  NumericWeight = 1.0
  NominalWeight = 1.0 
Clustering = 1-8

[Output]
AllFoldErrors = Yes

[General]
Verbose = 1

[Model]
MinimalWeight = 2

