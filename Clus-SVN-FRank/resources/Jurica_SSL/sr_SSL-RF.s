[General]
RandomSeed = 1
Verbose = 0
ResourceInfoLoaded = Yes

[Hierarchical]
Type = Tree
WType = ExpAvgParentWeight
HSeparator = /
WParam = 0.75
EmptySetIndicator = ?

[Data]
%No unlabeled data in the training set, and it is not provided in a separate file, so it will be automatically selected
File = sr_percL=100_0_SLTrain.arff

[Attributes]
Target = 17
Descriptive = 1-16

[Tree]
ConvertToRules = No
Heuristic = VarianceReduction
MissingClusteringAttrHandling = EstimateFromParentNode
MissingTargetAttrHandling = ParentNode
PruningMethod = M5

[Ensemble]
EnsembleMethod = RForest
Iterations = 10

[SemiSupervised]
PercentageLabeled = 20

[Output]
TrainErrors = Yes
TestErrors = Yes
WriteModelFile = No
