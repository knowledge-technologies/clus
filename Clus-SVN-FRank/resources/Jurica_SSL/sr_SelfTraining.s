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
File = sr_percL=100_0_SLTrain.arff
TestSet = sr_percL=100_0_Test.arff

[Attributes]
Target = 17
Descriptive = 1-16

[Ensemble]
EnsembleMethod = RForest
Iterations = 50
SelectRandomSubspaces = 0
OOBestimate = Yes

[SemiSupervised]
SemiSupervisedMethod = SelfTraining
UnlabeledData = sr_percL=100_0_Unlabeled.arff
ConfidenceMeasure = ClassesProbabilities
UnlabeledCriteria = Threshold
ConfidenceThreshold = 0.5
Aggregation = Average
Normalization = Ranking

[Output]
TrainErrors = Yes
TestErrors = Yes
WriteModelFile = No
