[General]
ResourceInfoLoaded = No
Verbose = 1
RandomSeed = 0

[Data]
File = C:/Users/matej/git/clusproject/ClusProject/resources/unitTests/ensembleAndRankingData/testTreeHMLC.arff

[Attributes]
Descriptive = 1-3
Target = 4-6

[Tree]
ConvertToRules = No

[Ensemble]
FeatureRankingPerTarget = Yes

[Relief]
Neighbours = [3,1]
Iterations = [-1,5]
WeightNeighbours = Yes
WeightingSigma = 0.5

[Hierarchical]
Type = Tree
WType = ExpAvgParentWeight
HSeparator = /
WParam = 0.9
