[General]
ResourceInfoLoaded = Yes
RandomSeed = 0

[Data]
File = resources/unitTests/ensembleAndRankingData/testMTR-RealWorld.arff

[Attributes]
Target = 48,49,50
Disable = 48-50

[Ensemble]
FeatureRankingPerTarget = Yes
SortRankingByRelevance = No

[Relief]
Iterations = [0.2,0.5,1]
Neighbours = [1,5,10]
WeightNeighbours = Yes
WeightingSigma = 0.4
