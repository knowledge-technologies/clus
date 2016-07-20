
% *RUN*
% clus -xval %f.s
% cdiff %f.out orig/%f.out.orig
% cdiff %f.xval orig/%f.xval.orig

% *CLEAN*
% rm -f %f.out %f.xval %f.model

[Data]
File = ../data/iris/iris.arff

[Attributes]
Descriptive = 1-4
Clustering = 1-4
Target = 1-4
Weights = 1.0

[ILevelC]
NbRandomConstraints = 20
