
% *RUN*
% clus %f.s
% cdiff %f.out orig/%f.out.orig

% *CLEAN*
% rm -f %f.out %f.model

[Data]
File = ../data/cpu/cpu.arff
RemoveMissingTarget = Yes

[Tree]
Heuristic = VarianceReduction
PruningMethod = M5



