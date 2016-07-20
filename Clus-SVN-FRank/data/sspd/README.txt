
Input files
-----------

sspd.arff

-> contains training data

sspd.dist

-> contains distances between examples
   is a matrix with one element for each example (same order as sspd.arff)

	0.00,0.89,0.76,0.19,10.76,10.12,10.43,10.52
	0.87,0.00,0.30,0.53,10.09,10.15,10.05,10.09
	0.59,0.92,0.00,0.73,10.09,10.03,10.28,10.87
	0.08,0.04,0.57,0.00,10.41,10.38,10.27,10.10
	10.61,10.58,10.44,10.50,0.00,0.34,5.63,5.25
	10.46,10.07,10.77,10.33,0.36,0.00,5.30,5.41
	10.51,10.35,10.50,10.10,5.27,5.23,0.00,0.19
	10.29,10.51,10.05,10.37,5.55,5.63,0.79,0.00

sspd.s

-> settings file for clus
   important settings are:
   
	[Data]
	TestFile = sspd_test.arff -> use a test file (remove this line if no test file)

	[Tree]
	Heuristic = SSPD -> use sspd heuristic
   
   the output file contains all settings, one can copy settings from the output file 
   to this file if desired.

Run Clus
--------

$ clus sspd

-> sspd is the name of the settings file

-> output

	Clus v1.0
	---------

	Loading 'sspd'

	Reading ARFF Header

	Reading CSV Data
	Found 8 rows
	Loading SSPD Matrix: sspd.dist (Size: 8)
	Setting clustering statistic: clus.ext.sspd.SSPDStatistic
	Setting target statistic: clus.ext.sspd.SSPDStatistic
	Loading: sspd_test.arff
	Loading 'sspd_test.arff'
	  Normalization for: attr_c = 2.4844720496894417
	Clustering: Weights C=[1], N=[1]
	  Target weight     = 1.0
	  Non target weight = 1.0
	  Numeric weight    = 1.0
	  Nominal weight    = 1.0
	Compactness:   Weights C=[1,1,1,1], N=[1,1,2.484,1]

	Has missing values: false
	Run: 01
	TDIDT
	Heuristic: Sum of Squared Pairwise Distances (SSPD)

	Test: attr_a = a -> 398.42285
	Test: attr_b = 1 -> 58.33432500000001
	Induction Time: 0.049 sec
	Pruning Time: 0.035 sec

	Loading 'sspd_test.arff'
	Output written to: sspd.out

-> clus found two useful tests (heuristic value is behind "->")

Output files
------------

sspd.out

-> contains the settings and the decision tree

Pruned Model
************

	attr_a = a
	+--yes: 4.0 (1)
	+--no:  attr_b = 1
        	+--yes: 2.0 (2)
        	+--no:  2.0 (3)

-> the numbers between the "(,)" are the leaf ID's
-> the numbers before the leaf ID's are the number of training examples in the leave
-> pruning does not work for sspd, since there is no error measure defined

sspd.1.id

-> the training examples in each leaf (if you doe cross-validation, then there is one
   file of this kind for each fold).
   
	leaf(0,[ex1,ex2,ex3,ex4]).
	leaf(1,[ex5,ex6]).
	leaf(2,[ex7,ex8]).
   
-> for example, leaf ID 0 contains training examples with key "ex1", ...
-> if the data set has missing values than this may include weights

Test set files
--------------

sspd_test.arff

-> test file: must be same format as training data (same attributes)

sspd_test.id

-> predicted leaf ID's for the test data

	pred(e1,1).
	pred(e2,1).
	pred(e3,1).
	pred(e4,1).
	pred(e5,2).
	pred(e6,2).
	pred(e7,3).
	pred(e8,3).

that's it for now
mail questions to jan.struyf@cs.kuleuven.be
