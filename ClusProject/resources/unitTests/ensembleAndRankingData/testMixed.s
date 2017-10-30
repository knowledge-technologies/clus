[General]
ResourceInfoLoaded = No
Verbose = 1
RandomSeed = 1234

[Data]
File = resources/unitTests/ensembleAndRankingData/testMixed.arff

[Attributes]
Descriptive = 1-7
Target = 8-11

[Tree]
ConvertToRules = No

[Ensemble]
FeatureRankingPerTarget = Yes

[Relief]
Neighbours = [3,1]
Iterations = [-1,5]
WeightNeighbours = Yes
WeightingSigma = 0.5
