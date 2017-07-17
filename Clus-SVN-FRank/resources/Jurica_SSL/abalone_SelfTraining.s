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

[Ensemble]
EnsembleMethod = RForest
Iterations = 50
SelectRandomSubspaces = 0
OOBestimate = Yes

[SemiSupervised]
SemiSupervisedMethod = SelfTraining
UnlabeledData = abalone_percL=25_0_Unlabeled.arff
ConfidenceMeasure = Variance
UnlabeledCriteria = AutomaticOOBInitial

[Output]
TrainErrors = Yes
TestErrors = Yes
