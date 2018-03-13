[General]
RandomSeed = 1
Verbose = 1
ResourceInfoLoaded = Yes

[Data]
File = abalone_percL=25_0_SLTrain.arff
TestSet = abalone_percL=25_0_Test.arff

[Attributes]
Target = 9
Descriptive = 1-8


[Tree]
ConvertToRules = No
Heuristic = VarianceReduction
MissingClusteringAttrHandling = EstimateFromParentNode
MissingTargetAttrHandling = ParentNode
PruningMethod = M5

[SemiSupervised]
UnlabeledData = abalone_percL=25_0_Unlabeled.arff
SemiSupervisedMethod = PCT

[Output]
TrainErrors = Yes
TestErrors = Yes
