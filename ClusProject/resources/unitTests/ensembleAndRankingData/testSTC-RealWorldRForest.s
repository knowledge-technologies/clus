[General]
Verbose = 0
ResourceInfoLoaded = Yes
RandomSeed = 0

[Data]
File = resources/unitTests/ensembleAndRankingData/testSTC-RealWorld.arff

[Attributes]
Target = 5
Descriptive = 1-4

[Relief]
Iterations = [0.2,0.5,1]
Neighbours = [1,5,10]
WeightNeighbours = Yes
WeightingSigma = 0.4

[Ensemble]
SelectRandomSubspaces = SQRT
EnsembleMethod = RForest
Iterations = [2, 4]
FeatureRanking = [RForest]

[Output]
WriteOOBFile = No
