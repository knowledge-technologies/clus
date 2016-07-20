[Tree]
PruningMethod = EncodingCost
FTest = 1.0
AlternativeSplits = true
Heuristic = GeneticDistance

[Attributes]
Key = 1
Descriptive = 2-264
Target = 2-264
Weights = 1
ReduceMemoryNominalAttrs = yes

[Model]
MinimalWeight = 1

[Data]
File = secretin.arff

[Output]
TrainErrors = No
PrintModelAndExamples = true

[Phylogeny]
DistanceMatrix = secretin.dist
OptimizationCriterion = MinTotBranchLength 
Sequence = Protein
