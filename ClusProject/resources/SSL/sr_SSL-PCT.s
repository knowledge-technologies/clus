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
File = sr_percL=100_0_SSLTrain.arff
TestSet = sr_percL=100_0_Test.arff

[Attributes]
Target = 17
Descriptive = 1-16

[Tree]
ConvertToRules = No
Heuristic = VarianceReduction
MissingClusteringAttrHandling = EstimateFromParentNode
MissingTargetAttrHandling = ParentNode
PruningMethod = M5

[SemiSupervised]

[Output]
TrainErrors = Yes
TestErrors = Yes
WriteModelFile = No
