[General]
Verbose = 0
ResourceInfoLoaded = Yes
RandomSeed = 0

[Data]
File = resources/unitTests/ensembleAndRankingData/testMLC-RealWorld.arff
% TestSet = C:/Users/matej/git/e8datasets/HMLC/wipo_test.arff

[Attributes]
Target = 1002-1054
Disable = 1002-1054
Descriptive = 1-1001

[Ensemble]
SelectRandomSubspaces = SQRT
EnsembleMethod = RForest
Iterations = [2, 4]
FeatureRanking = [Symbolic,RForest,Genie3]
FeatureRankingPerTarget = Yes
SortRankingByRelevance = No

[Relief]
Iterations = [0.2,0.5,1]
Neighbours = [1,5,10]
WeightNeighbours = Yes
WeightingSigma = 0.4
MultilabelDistance = HammingLoss

[Output]
WriteOOBFile = No
