% Work in progress!!!

[Data]
File = Clus-SVN-FRank/data/hierarchicalmtr/hmtrtest.arff

[Attributes]
Disable = 1
Descriptive = 2-4
Target = 5-10

[HMTR]
Type = Tree
Distance = WeightedEuclidean
Aggregation = SUM
Hierarchy = (root->left),(root->center),(root->var_i),(left->var_d),(left->var_e),(left->vsr_f),(center->var_g),(center->var_h)