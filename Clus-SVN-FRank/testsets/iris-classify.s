
% *RUN*
% clus -xval %f.s
% cdiff %f.out orig/%f.out.orig
% cdiff %f.xval orig/%f.xval.orig

% *CLEAN*
% rm -f %f.out %f.xval %f.model %f.test.pred.arff %f.train.*.pred.arff %f.train.*.id

[Data]
File = ../data/iris/iris.arff

[Output]
TrainErrors = Yes
AllFoldModels = Yes
AllFoldErrors = Yes
%WriteErrorFile = Yes
UnknownFrequency = Yes
BranchFrequency = Yes
WritePredictions = {Train, Test}
ModelIDFiles = Yes
