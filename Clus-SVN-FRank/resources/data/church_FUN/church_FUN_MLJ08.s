

% HMC: Average AUROC: 0.5343507405843365, Average AUPRC: 0.02855423573980789, Average AUPRC (weighted): 0.1294195148197721, Pooled AUPRC: 0.1697051282808009
% HSC: Average AUROC: 0.5244585733730156, Average AUPRC: 0.02902416018088555, Average AUPRC (weighted): 0.1268332350313958, Pooled AUPRC: 0.1309560003304250
% SC:  Average AUROC: 0.5278170279406131, Average AUPRC: 0.03108210312641788, Average AUPRC (weighted): 0.1280239438871272, Pooled AUPRC: 0.1275119210526612

[General]
Compatibility = MLJ08

[Data]
File = church_FUN.train.arff.zip
PruneSet = church_FUN.valid.arff.zip
TestSet = church_FUN.test.arff.zip

[Attributes]
ReduceMemoryNominalAttrs = yes

[Hierarchical]
Type = TREE
WType = ExpAvgParentWeight
HSeparator = /
ClassificationThreshold = [0,2,4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,52,54,56,58,60,62,64,66,68,70,72,74,76,78,80,82,84,86,88,90,92,94,96,98,100]

[Tree]
ConvertToRules = No
FTest = [0.001,0.005,0.01,0.05,0.1,0.125]

[Model]
MinimalWeight = 5.0

[Output]
TrainErrors = No
ValidErrors = No
TestErrors = No

