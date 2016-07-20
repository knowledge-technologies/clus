
% *RUN*
% clus %f.s
% cdiff %f.out orig/%f.out.orig

% *CLEAN*
% rm -f %f.out %f.model

[Data]
File = ../data/sparse/sparse-test.arff

[Model]
MinimalWeight = 1.0
