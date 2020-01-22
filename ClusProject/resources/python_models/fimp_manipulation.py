import scipy.stats.stats as stats
import math
import matplotlib.pyplot as plt
import numpy as np
from typing import List, Union
from tqdm import trange


class Fimp:
    def __init__(self, f_name=None, num_feat=float("inf"), attr_dict=None, header=None):
        self.f_name = f_name
        self.header = []  # list of lines from the start to the --------- line
        self.table = []  # [[dataset index, name, ranks, relevances], ...]
        self.attrs = {}  # {name: [dataset index, ranks, relevances], ...}
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

    def sort_by_outer_relevance(self, outer_ranks):
        self.sort_by_attr_index()
        table_extended = [(rank, row) for rank, row in zip(outer_ranks, self.table)]
        table_extended.sort()
        self.table = [pair[1] for pair in table_extended]

    def get_attr_indices(self):
        return [row[0] for row in self.table]

    def get_attr_names(self):
        return [row[1] for row in self.table]

    def get_relevances(self, ranking_index=None):
        return [row[-1] if ranking_index is None else row[-1][ranking_index] for row in self.table]

    def get_relevance(self, feature_name, ranking_index=None):
        i = 0 if ranking_index is None else ranking_index
        return self.attrs[feature_name][-1][i]

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

    @staticmethod
    def create_fimp_from_relevances(attribute_relevances,
                                    attribute_names: Union[List[str], None] = None,
                                    attribute_indices: Union[List[int], None] = None,
                                    fimp_header: Union[List[str], None] = None):
        n = len(attribute_relevances)
        if attribute_indices is None:
            attribute_indices = [i + 1 for i in range(n)]
        if attribute_names is None:
            attribute_names = ["a{}".format(i) for i in attribute_indices]
        if fimp_header is None:
            fimp_header = ["Unknown ranking meta-data", "-" * 100]
        # compute ranks
        ranks = [-1 for _ in range(n)]
        relevances_positions = list(zip(attribute_relevances, range(n)))
        relevances_positions.sort(reverse=True)
        rank = 0
        for i, relevance_position in enumerate(relevances_positions):
            relevance, position = relevance_position
            if i == 0 or abs(relevance - relevances_positions[i - 1][0]) > 10 ** -12:  # same tolerance as in clus
                rank = i + 1
            ranks[position] = rank
        d = {a: [i, [rank], [relevance]] for a, i, rank, relevance in zip(attribute_names,
                                                                          attribute_indices,
                                                                          ranks,
                                                                          attribute_relevances)}
        return Fimp(attr_dict=d, header=fimp_header)

    def create_extended_feature_weights(self, ranking_index=0, normalise=False):
        return Fimp._create_extended_feature_weights_static(self.get_attr_indices(),
                                                            self.get_relevances(ranking_index),
                                                            self.f_name,
                                                            normalise)

    @staticmethod
    def _create_extended_feature_weights_static(attribute_indices, attribute_relevances, ranking_file, normalise):
        n = max(attribute_indices)
        ws = [0] * n
        for attribute_index, w in zip(attribute_indices, attribute_relevances):
            ws[attribute_index - 1] = max(0, w)
        if normalise:
            w_max = max(ws)
            if w_max == 0.0:
                ws = [1.0] * len(ws)
                print("Only default models in", ranking_file)
            elif w_max == float("inf"):
                ws = [0.0 if w < w_max else 1.0 for w in ws]
                print("Maximal weight = inf, for", ranking_file)
            else:
                ws = [w / w_max for w in ws]
        return ws

    @staticmethod
    def create_extended_feature_weights_dict(feature_ranking, normalise=True):
        pairs = sorted(feature_ranking.items())
        ranking_file = "no file"
        return Fimp._create_extended_feature_weights_static([p[0] for p in pairs], [p[1] for p in pairs], ranking_file,
                                                            normalise)


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
            if i == 0 or abs(impo - ranking_impos[i - 1][0]) > 10 ** -12:  # same tolerance as in clus
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


def compute_similarity(fimp1: Fimp, fimp2: Fimp, ranking_index: int, similarity_measure: str, eps: float = 0.0,
                       step:  Union[str, int] = 1):
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

    def fuzzy_jaccard(f1: Fimp, f2: Fimp):
        def relative_score(absolute_score, normalisation_factor):
            if normalisation_factor == 0.0:
                return 1.0
            else:
                return absolute_score / normalisation_factor

        for f in [f1, f2]:
            f.sort_by_relevance(ranking_index)
        attributes = [f1.get_attr_names(), f2.get_attr_names()]
        n = len(attributes[0])

        if isinstance(step, str):
            feature_subset_sizes = []
            if step == "exp":
                i = 1
                while i <= n:
                    feature_subset_sizes.append(i - 1)
                    i *= 2
            elif step == "squared":
                i = 1
                while i ** 2 <= n:
                    feature_subset_sizes.append(i ** 2 - 1)
                    i += 1
            else:
                raise ValueError("Wrong step specification: {}".format(step))
        else:
            feature_subset_sizes = list(range(0, n, step))
        if feature_subset_sizes[-1] != n - 1:
            feature_subset_sizes.append(n - 1)

        n_evaluated_subsets = len(feature_subset_sizes)
        i_subset = 0
        results = [-1.0] * n_evaluated_subsets
        current_sets = [set(), set()]
        min_scores = [float("inf")] * 2
        union_set = set()
        for i in trange(n):
            for j, (current_set, attributes_ranking, f) in enumerate(zip(current_sets, attributes, [f1, f2])):
                feature = attributes_ranking[i]
                s = f.get_relevance(feature, ranking_index)
                min_scores[j] = min(min_scores[j], s)
                if max(min_scores) <= eps:
                    # for every i1 >= i, we have results[i1] = 1
                    for i1 in range(i_subset, n_evaluated_subsets):
                        results[i1] = 1.0
                    print("Smartly skipping the features with ranks >= {}".format(i))
                    return results
                current_set.add(feature)
                union_set.add(feature)
            if i != feature_subset_sizes[i_subset]:
                continue
            union = sorted(union_set)
            scores = [[-1.0, -1.0] for _ in union]
            for i1, feature in enumerate(union):
                for j1, f in enumerate([f1, f2]):
                    scores[i1][j1] = min(1.0, relative_score(f.get_relevance(feature, ranking_index),
                                                             min_scores[j1]))
            fuzzy_intersection = 0.0
            fuzzy_union = 0.0
            for pair in scores:
                fuzzy_intersection += min(pair)
                fuzzy_union += max(pair)  # should always equal 1.0
            results[i_subset] = fuzzy_intersection / fuzzy_union
            i_subset += 1
        return results

    def correlation(f1: Fimp, f2: Fimp):
        for f in [f1, f2]:
            f.sort_by_attr_index()
        scores1 = f1.get_relevances(ranking_index)
        scores2 = f2.get_relevances(ranking_index)
        return stats.pearsonr(scores1, scores2)

    def canberra(f1: Fimp, f2: Fimp):
        def expected_canberra(n, k):
            return (k + 1) * (2 * n - k) / n * math.log(4) + k * (k + 1) / n + 2 * k - 3

        for f in [f1, f2]:
            f.sort_by_attr_index()
        ranks1 = f1.get_ranks(ranking_index)
        ranks2 = f2.get_ranks(ranking_index)
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
        return jaccard(fimp1, fimp2)
    elif similarity_measure == "fuzzy_jaccard":
        return fuzzy_jaccard(fimp1, fimp2)
    elif similarity_measure == "correlation":
        return correlation(fimp1, fimp2)
    elif similarity_measure == "canberra":
        return canberra(fimp1, fimp2)
    else:
        raise ValueError("Wrong Error measure")


def feature_importances_side_by_side(fimps: List[Fimp], labels, colours, ranking_index=0, sort_by="rank",
                                     sort_by_difference=False, normalize=False):
    """
    Shows side-by-side bar chart of importances. The features are sorted by the ranks from the first ranking,
    or by their dataset index.
    :param fimps:
    :param labels:
    :param colours:
    :param ranking_index:
    :param sort_by: "rank" or "dataset_index"
    :return:
    """
    rank = "rank"
    dataset_index = "dataset_index"
    allowed = [rank, dataset_index]
    if sort_by not in allowed:
        raise ValueError("Wrong sorter: {} not in {}".format(sort_by, allowed))
    n = len(fimps)
    assert n == len(labels) == len(colours)
    width = 1 / (n + 1)
    subgroup_offsets = width * (np.array(range(n)) - (n - 1) / 2)
    if sort_by == rank:
        fimps[0].sort_by_relevance(ranking_index)
        order = [pair for pair in enumerate(fimps[0].get_attr_indices())]
        order.sort(key=lambda pair: pair[1])
        for fimp in fimps[1:]:
            fimp.sort_by_outer_relevance([pair[0] for pair in order])
    elif sort_by == dataset_index:
        for fimp in fimps:
            fimp.sort_by_attr_index()
    else:
        raise ValueError("Wrong sorter")
    for fimp in fimps[1:]:
        assert fimp.get_attr_names() == fimps[0].get_attr_names()
    if normalize:
        scores = [normalize_by_max(fimp.get_relevances(ranking_index)) for fimp in fimps]
    else:
        scores = [fimp.get_relevances(ranking_index) for fimp in fimps]
    if sort_by_difference:
        assert len(scores) == 2
        d = [abs(x - y) for x, y in zip(*scores)]
        for i in range(len(scores)):
            temp = list(enumerate(scores[i]))
            temp.sort(key=lambda x: d[x[0]], reverse=True)
            scores[i] = [x[1] for x in temp]
    m = len(scores[0])
    group_centres = np.array(range(m))
    for i in range(n):
        plt.bar(group_centres + subgroup_offsets[i], scores[i], width, color=colours[i], label=labels[i])
    plt.xticks(group_centres, fimps[0].get_attr_names(), rotation='vertical')
    plt.ylabel("Normalized feature importance")
    plt.legend()
    plt.show()


def feature_importances_superimposed(fimps: List[Fimp], labels, colours, ranking_index=0, sort_by="rank"):
    width = 0.8

    highPower = [1184.53, 1523.48, 1521.05, 1517.88, 1519.88, 1414.98,
                 1419.34, 1415.13, 1182.70, 1165.17]
    lowPower = [1000.95, 1233.37, 1198.97, 1198.01, 1214.29, 1130.86,
                1138.70, 1104.12, 1012.95, 1000.36]

    indices = np.arange(len(highPower))

    plt.bar(indices, highPower, width=width,
            color='b', label='Max Power in mW')
    plt.bar([i + 0.25 * width for i in indices], lowPower,
            width=0.5 * width, color='r', alpha=0.5, label='Min Power in mW')

    plt.xticks(indices + width / 2.,
               ['T{}'.format(i) for i in range(len(highPower))])

    plt.legend()

    plt.show()


def normalize_by_max(scores):
    s = np.array(scores)
    m = np.max(s)
    if m < 0:
        raise ValueError("Max is negative")
    elif m == 0:
        return scores
    return list(s / np.max(s))


def test_create_fimp_from_relevances():
    s = [0, 1, 2, 3, 2, 1, 0]
    f = Fimp.create_fimp_from_relevances(s)
    f.write_to_file("test.txt")


def test_fuzzy_jacard():
    f1 = Fimp.create_fimp_from_relevances([1.0, 0.8, 0.7, 0, 0, 0])
    f2 = Fimp.create_fimp_from_relevances([1.0, 0.7, 0.7, 0.8, 0, 0])
    fuzzy_jaccard1 = compute_similarity(f1, f2, 0, "fuzzy_jaccard", eps=-1.0, step=3)
    fuzzy_jaccard2 = compute_similarity(f1, f2, 0, "fuzzy_jaccard", step=3)
    print(fuzzy_jaccard1, fuzzy_jaccard2)

if __name__ == "__main__":
    test_fuzzy_jacard()


