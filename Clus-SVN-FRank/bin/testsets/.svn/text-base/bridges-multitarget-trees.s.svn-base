
% *RUN*
% clus %f.s
% cdiff %f.out orig/%f.out.orig

% *CLEAN*
% rm -f %f.out %f.xval %f.model %f.test.pred.arff %f.train.*.pred.arff %f.train.*.id

[Data]
File = ../data/bridges/bridges.arff
PruneSet = 0.33

[Attributes]
Target = 9-13
Disable = 1

[Tree]
PruningMethod = CartVSB

