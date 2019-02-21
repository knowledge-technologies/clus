import scipy.stats.stats as stats
import math


class Fimp:
    def __init__(self, f_name=None, num_feat=float("inf"), attr_dict=None, header=None):
        self.f_name = f_name
        self.header = []   # list of lines from the start to the --------- line
        self.table = []    # [[dataset index, name, ranks, relevances], ...]
        self.attrs = {}    # {name: [dataset index, ranks, relevances], ...}
        if f_name is None:
            assert attr_dict is not None
            self.attrs = attr_dict
            self.header = header
            for attr in attr_dict:
                row = [attr_dict[attr][0], attr, attr_dict[attr][1], attr_dict[attr][2]]
                self.table.append(row)
            # self.sort_by_relevance()
        else:
            with open(self.f_name) as f:
                for x in f:
                    self.header.append(x.strip())
                    if x.startswith("---------"):
                        break
                for feat_ind, x in enumerate(f):
                    if feat_ind == num_feat:
                        break
                    ind, name, rnks, rels = x.strip().split("\t")
                    ind = int(ind)
                    rnks = eval(rnks)
                    rels = eval(rels)
                    self.attrs[name] = [ind, rnks, rels]
                    self.table.append([ind, name, rnks, rels])

    def sort_by_attr_index(self):
        self.table.sort(key=lambda row: row[0])

    def sort_by_relevance(self, ranking_index=0):
        self.table.sort(key=lambda row: row[2][ranking_index])

    def get_attr_indices(self):
        return [row[0] for row in self.table]

    def get_attr_names(self):
        return [row[1] for row in self.table]

    def get_relevances(self, ranking_index=None):
        return [row[-1] if ranking_index is None else row[-1][ranking_index] for row in self.table]

    def get_ranks(self, ranking_index=None):
        return [row[-2] if ranking_index is None else row[-2][ranking_index] for row in self.table]

    def get_attribute_description(self, attr_name):
        return self.attrs[attr_name]

    def get_nb_rankings(self):
        return 0 if not self.table else len(self.table[0][-1])

    def get_ranking_names(self):
        return [r.strip() for r in self.header[-2].split("\t")[-1][1:-1].split(",")]

    def get_header(self):
        return self.header

    def get_ensemble_size(self):
        for line in self.header:
            if line.startswith("Ensemble size:"):
                return int(line[line.find(":") + 1:])
        return None

    def write_to_file(self, out_file):
        with open(out_file, "w", newline='') as f:
            print("\n".join(self.header), file=f)
            for row in self.table:
                ind, name, ranks, rels = row
                print("{}\t{}\t{}\t{}".format(ind, name, ranks, rels), file=f)


def fimp_aggregation(partial_fimp_files, out=None):
    """
    Joins partial ensemble fimps into one.
    """
    def initialize_importances_matrix(importances):
        num_attrs = len(importances)
        num_rankings = len(importances[0])
        return [[0 for _ in range(num_rankings)] for _ in range(num_attrs)]

    def add_importances(partial_importances, trees):
        assert len(partial_importances) == len(matrix_sum)
        assert len(partial_importances[0]) == len(matrix_sum[0])
        for attr_ind in range(len(matrix_sum)):
            for ranking_ind in range(len(matrix_sum[0])):
                matrix_sum[attr_ind][ranking_ind] += partial_importances[attr_ind][ranking_ind] * trees

    fimps = [Fimp(f_name=fimp_file) for fimp_file in partial_fimp_files]
    for fimp in fimps:
        fimp.sort_by_attr_index()
    # find ensemble sizes
    ensemble_size = "Ensemble size:"
    ensemble_size_line = None
    dummy_number = -1
    number_of_trees = [dummy_number for _ in fimps]
    header = None
    for i, fimp in enumerate(fimps):
        header = fimp.get_header()
        for j, line in enumerate(header):
            if line.startswith(ensemble_size):
                ensemble_size_line = j
                number_of_trees[i] = int(line[line.find(":") + 1:].strip())  # 'Ensemble size: 123' ---> 123
            assert number_of_trees != dummy_number
    # sum of importances
    matrix_sum = initialize_importances_matrix(fimps[0].get_relevances())
    attribute_names = fimps[0].get_attr_names()
    ranking_names = fimps[0].get_ranking_names()
    attr_indices = fimps[0].get_attr_indices()
    for i, fimp in enumerate(fimps):
        partial = fimp.get_relevances()
        assert attribute_names == fimp.get_attr_names()
        assert ranking_names == fimp.get_ranking_names()
        assert attr_indices == fimp.get_attr_indices()
        add_importances(partial, number_of_trees[i])
    # normalize
    nb_trees = sum(number_of_trees)
    for i in range(len(matrix_sum)):
        for j in range(len(matrix_sum[0])):
            matrix_sum[i][j] /= nb_trees
    # compute ranks
    matrix_ranks = [[dummy_number for _ in ranking_names] for _ in attribute_names]
    for r in range(len(ranking_names)):
        ranking_impos = [(matrix_sum[attr][r], attr) for attr in range(len(attribute_names))]
        ranking_impos.sort(reverse=True)
        rank = -1
        for i, impo_attr in enumerate(ranking_impos):
            impo, attr = impo_attr
            if i == 0 or abs(impo - ranking_impos[i - 1][0]) > 10**-12:  # same tolerance as in clus
                rank = i + 1
            matrix_ranks[attr][r] = rank

    attr_dict = {}
    header[ensemble_size_line] = "{} {}".format(ensemble_size, nb_trees)
    for i, attr in enumerate(attribute_names):
        attr_dict[attr] = [attr_indices[i], matrix_ranks[i], matrix_sum[i]]
    fimp_out = Fimp(attr_dict=attr_dict, header=header)
    if out is not None:
        fimp_out.write_to_file(out)
    return fimp_out


def compute_similarity(fimp1: Fimp, fimp2: Fimp, ranking_index: int, similarity_measure: str):
    def jaccard(f1: Fimp, f2: Fimp):
        for f in [f1, f2]:
            f.sort_by_relevance(ranking_index)
        attributes1 = f1.get_attr_names()
        attributes2 = f2.get_attr_names()
        results = [-1.0] * len(attributes1)
        current_set1 = set()
        current_set2 = set()
        for i in range(len(attributes1)):
            current_set1.add(attributes1[i])
            current_set2.add(attributes2[i])
            results[i] = len(current_set1 & current_set2) / len(current_set1 | current_set2)
        return results

    def correlation(f1: Fimp, f2: Fimp):
        for f in [f1, f2]:
            f.sort_by_attr_index()
        scores1 = f1.get_relevances(ranking_index)
        scores2 = f2.get_relevances(ranking_index)
        return stats.pearsonr(scores1, scores2)

    def canberra(f1: Fimp, f2: Fimp):
        def expected_canberra(n, k):
            return (k + 1) * (2 * n - k) / n * log(4) + k * (k + 1) / n + 2 * k - 3

        for f in [f1, f2]:
            f.sort_by_attr_index()
        ranks1 = f1.get_attr_indices()
        ranks2 = f2.get_attr_indices()
        results = [-1.0] * len(ranks1)
        for i in range(1, 1 + len(ranks1)):
            distance = 0.0
            for j in range(i):
                r1 = min(ranks1[j], i + 1)
                r2 = min(ranks2[j], i + 1)
                distance += abs(r1 - r2) / (r1 + r2)
            distance /= expected_canberra(len(ranks1), i)
            results[i - 1] = distance
        return results

    # sanity check
    for fimp in [fimp1, fimp2]:
        fimp.sort_by_attr_index()
    if fimp1.get_attr_names() != fimp2.get_attr_names():
        raise ValueError("Names of the attributes are not the same")
    if similarity_measure == "jaccard":
        return jaccard(f1, f2)
    elif similarity_measure == "correlation":
        return correlation(f1, f2)
    elif similarity_measure == "canberra":
        return canberra(f1, f2)
    else:
        raise ValueError("Wrong Error measure")
