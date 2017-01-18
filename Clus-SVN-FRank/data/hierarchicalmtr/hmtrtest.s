% Work in progress!!!

[Data]
File = Clus-SVN-FRank/data/hierarchicalmtr/hmtrtest.arff

[Attributes]
Disable = 1
Descriptive = 2-4
Target = 5-10

[HMTR]
Type = Tree
Distance = WeightedEuclidean
Aggregation = SUM
Weight = 0.75
Hierarchy = (root -> left), (root -> center), (root -> var_i), (left -> var_d), (left -> var_e), (left -> var_f), (center -> var_g), (center -> var_h)
%Hierarchy = root-A,root-ADOKS,root-AMSC2005,root-FCCS2005,root-LODZ2004,root-ZAKOPANE2004,root-DYDAKTYKA,root-DYDAKTYKA_KST,root-DYDAKTYKA_WPR,A-IMAGES,IMAGES-BACKUP,ADOKS-ROZDZIAL_2,ADOKS-ROZDZIAL_3,ADOKS-ROZDZIAL_4,AMSC2005-AMSC2004,FCCS2005-source,FCCS2005-TMP,DYDAKTYKA-DYDAKTYKA_ISI,DYDAKTYKA-DYDAKTYKA_PRG,DYDAKTYKA_ISI-COLLS,DYDAKTYKA_ISI-IMAGES2,DYDAKTYKA_ISI-src,DYDAKTYKA_ISI-WYKLAD5,COLLS-Q1,COLLS-Q2,COLLS-RAZEM,RAZEM-RYSUNKI_COLL1,RAZEM-RYSUNKI_COLL2,DYDAKTYKA_PRG-images,DYDAKTYKA_PRG-POMOC,DYDAKTYKA_KST-images2,DYDAKTYKA_KST-src2,DYDAKTYKA_WPR-images3,DYDAKTYKA_WPR-src3

% root
% +---A
% |   +---IMAGES
% |       +---BACKUP
% +---ADOKS
% |   +---ROZDZIAL_2
% |   +---ROZDZIAL_3
% |   +---ROZDZIAL_4
% +---AMSC2005
% |   +---AMSC2004
% +---FCCS2005
% |   +---source
% |   +---TMP
% +---LODZ2004
% +---ZAKOPANE2004
% +---DYDAKTYKA
% |   +---DYDAKTYKA_ISI
% |   |   +---COLLS
% |   |   |   +---Q1
% |   |   |   +---Q2
% |   |   |   +---RAZEM
% |   |   |       +---RYSUNKI_COLL1
% |   |   |       +---RYSUNKI_COLL2
% |   |   +---IMAGES2
% |   |   +---src
% |   |   +---WYKLAD5
% |   +---DYDAKTYKA_PRG
% |       +---images
% |       +---POMOC
% +---DYDAKTYKA_KST
% |   +---images2
% |   +---src2
% +---DYDAKTYKA_WPR
%     +---images3
%     +---src3