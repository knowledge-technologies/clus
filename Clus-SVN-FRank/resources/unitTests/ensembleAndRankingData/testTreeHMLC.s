[General]
ResourceInfoLoaded = No
Verbose = 1
RandomSeed = 0

[Data]
File = C:/Users/matejp/git/clusfr/Clus-SVN-FRank/resources/unitTests/ensembleAndRankingData/testTreeHMLC.arff

[Attributes]
Descriptive = 1-3
Target = 4-6

[Tree]
ConvertToRules = No

[Ensemble]
FeatureRankingPerTarget = Yes

[Relief]
neighbours = [3,1]
iterations = [-1,5]
weightNeighbours = Yes
weightingSigma = 0.5

[Hierarchical]
Type = Tree
WType = ExpAvgParentWeight
HSeparator = /
WParam = 0.9
