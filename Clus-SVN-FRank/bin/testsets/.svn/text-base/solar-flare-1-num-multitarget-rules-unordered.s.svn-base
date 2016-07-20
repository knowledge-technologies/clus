
% *EXCLUDE*

% *RUN*
% clus -rules %f.s
% cdiff %f.out orig/%f.out.orig

% *CLEAN*
% rm -f %f.out %f.xval %f.model %f.test.pred.arff %f.train.*.pred.arff %f.train.*.id

[Data]
File = ../data/solar-flare-1-num/solar-flare-1-num.arff
RemoveMissingTarget = Yes

[Tree]
Heuristic = RDispersionMlt

[Rules]
CoveringWeight = 0
RuleAddingMethod = IfBetter
HeurCoveragePar = 0.5
PrintRuleWiseErrors = Yes
CoveringMethod = WeightedError
ComputeDispersion = Yes

[Attributes]
ClusteringWeights = 
  TargetWeight = 1
  NonTargetWeight = 0
  NumericWeight = 1.0
  NominalWeight = 1.0 
Target = 11,12,13
Disable = 11-13
Clustering = 1-10,11,12,13

[Output]
AllFoldErrors = Yes

[General]
Verbose = 1

[Model]
MinimalWeight = 2

