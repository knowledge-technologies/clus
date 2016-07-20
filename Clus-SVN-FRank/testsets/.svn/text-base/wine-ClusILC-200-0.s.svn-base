
% *RUN*
% clus -xval %f.s
% cdiff %f.out orig/%f.out.orig

% *CLEAN*
% rm -f %f.out %f.xval %f.model

!JOB = 33
[General]
RandomSeed = 0
[Data]
File = ../data/wine/wine.arff
[Attributes]
Descriptive = 1-13
Clustering = 1-13
Target = 1-13
Weights = 1.0
[ILevelC]
NbRandomConstraints = 200
[Output]
AllFoldModels = No
AllFoldErrors = Yes
