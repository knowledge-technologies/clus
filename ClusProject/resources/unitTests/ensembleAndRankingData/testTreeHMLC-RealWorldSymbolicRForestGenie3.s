[General]
ResourceInfoLoaded = Yes
Verbose = 0
RandomSeed = 0

[Data]
File = resources/unitTests/ensembleAndRankingData/testTreeHMLC-RealWorld.arff

[Hierarchical]
Type = Tree
HSeparator = /

[Relief]
Iterations = [0.2,0.5,1]
Neighbours = [1,5,10]
WeightNeighbours = Yes
WeightingSigma = 0.4

[Ensemble]
SelectRandomSubspaces = SQRT
EnsembleMethod = RForest
Iterations = [2, 4]
FeatureRanking = [Symbolic,RForest,Genie3]

[Output]
WriteOOBFile = No
