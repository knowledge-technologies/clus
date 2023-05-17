# Examples of running CLUSplus

## Installation

A compiled **clus.jar** file is needed to run examples. The following prerequisites must be met in order to successfully build the **CLUSplus** codebase. First, Java 1.8 JDK must be installed (JRE is enough for running the built jar file). Second, Apache Maven 3.6 or newer is needed to perform the build. Finally, a git client is needed to clone the project repository. Once the prerequisites are met, the build process consists of downloading the code, navigating to the folder containing the `pom.xml` file, and building the code using `maven`, using the following commands:
```sh
git clone http://source.ijs.si/ktclus/clus-public
cd ClusProject
mvn clean package
```

Below are descriptions and instructions on how to run examples in this folder.

## Learning a single decision tree. 
This is the most basic and default option of **CLUSplus**. If we simply call
```sh
java -jar clus.jar example.s
``` 
from the command line, it takes as input the mentioned settings file and the `example.arff` data file to produce the output in the `example.out`. In the output file, the basic information for the run is specified, e.g., all the values of all the parameters, the list of the models, and some statistics for the run (e.g., induction time and error measures). Three models have been built: the Default (leaf) Model, a fully grown tree (Original Model) and its pruned version (Pruned Model).

The sections `Ensemble` and `SemiSupervised` of the settings file were ignored during this run, because the `-forest` and `-ssl` command-line switches were not used. Note also that there are some other possible sections of the parameter settings `General`, where verbosity level and random seed can be specified), and there are many other parameters in the sections listed above (e.g., `TestSet` in section `Data`). However, since every parameter has its default value, we do not have to list them all in the settings file.

## Semi-supervised learning of PCTs ##
In semi-supervised mode, **CLUSplus** can use partially labeled examples (where the values of some of the target variables are unknown) or unlabeled examples (where the values of all of the target variables are unknown). Note that the ``exampleSSL.arff`` data file contains such examples in the last four rows. To build a PCT in SSL mode, the ``-ssl`` switch needs to be included into the command line: The command: 
```sh
java -jar clus.jar -ssl exampleSSL.s
``` 
would result in a single semi-supervised tree (output file exampleSSL.out`). 

## Semi-supervised learning of a random forest of PCTs ##
When growing an ensemble of decision trees, e.g., random forests, we need to include the switch `-forest` in the command line: The command: 
```sh
java -jar clus.jar -forest -ssl exampleSSL.s
```
produces a random forest of SSL trees (output file `exampleSSLrf.out`). Since the settings also specify that a feature ranking should be computed (`Ensemble.FeatureRanking = Genie3`), we will additionally obtain an `exampleSSLTrees10Genie3.fimp` file where the feature importance values (as calculated by the Genie3 method) are listed.
