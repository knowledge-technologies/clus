
[Data]
File = toyHMC.arff
PruneSet = toyHMC.arff
TestSet = toyHMC.arff

[Hierarchical]
Type = TREE
WType = ExpAvgParentWeight
HSeparator = /

[Tree]
FTest = [0.001,0.01,0.1,1.0]

[Model]
MinimalWeight = 1.0

[Output]
WritePredictions = {Test}

