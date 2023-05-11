[Data]
File = example.arff

[Attributes]
Descriptive = 1-3
Target = 4-6

[Tree]
Heuristic = VarianceReduction

[Ensemble]
SelectRandomSubspaces = SQRT
EnsembleMethod = RForest
FeatureRanking = Genie3
Iterations = 10 
