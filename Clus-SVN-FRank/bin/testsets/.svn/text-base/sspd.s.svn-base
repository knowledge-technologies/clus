% *RUN*
% cp ../data/sspd/sspd.dist .
% clus %f.s
% cdiff %f.out orig/%f.out.orig
% cdiff %f.train.1.id orig/%f.train.1.id.orig
% cdiff sspd.test.id orig/sspd_test.id.orig

% *CLEAN*
% rm -f %f.out %f.model %f.dist %f.train.1.id sspd.test.id


[Data]
File = ../data/sspd/sspd.arff
TestSet = ../data/sspd/sspd_test.arff

[Tree]
Heuristic = SSPD

[Output]
ShowInfo = {Count, Index}
ModelIDFiles = TRUE
