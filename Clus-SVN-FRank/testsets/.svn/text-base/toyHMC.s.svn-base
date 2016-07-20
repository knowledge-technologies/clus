
% *RUN*
% cp ../data/toyHMC/toyHMC.arff .
% perl ../data/church_FUN/run_hsc.pl toyHMC
% perl ../data/church_FUN/run_sc.pl toyHMC
% cdiff toyHMC.sc.combined.out orig/toyHMC.sc.combined.out.orig
% cdiff toyHMC.hsc.combined.out orig/toyHMC.hsc.combined.out.orig

% *CLEAN*
% rm -f toyHMC.sc.combined.out toyHMC.hsc.combined.out toyHMC.arff hierarchy.txt
% rm -rf sc hsc

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
