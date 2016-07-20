
% *RUN*
% clus %f.s
% cdiff %f.out orig/%f.out.orig

% *CLEAN*
% rm -f %f.out %f.model hierarchy.txt

[Data]
File = ../data/church_FUN/church_FUN.trainvalid.arff.zip
TestSet = ../data/church_FUN/church_FUN.test.arff.zip

[Tree]
ConvertToRules = No
FTest = 0.125

[Model]
MinimalWeight = 5.0

[Attributes]
ReduceMemoryNominalAttrs = yes

[Hierarchical]
Type = TREE
ClassificationThreshold = [90,95]
WType = ExpAvgParentWeight
HSeparator = /
RecallValues = [0.01, 0.10, 0.20, 0.50, 0.80, 1.00]
MEstimate = Yes
