import fimp_manipulation
import unittest


class TestFimpManipulation(unittest.TestCase):
    def setUp(self):
        self.fimp_names = ["fimp{}.fimp".format(i) for i in range(1, 4)] + ["fimpCombined.fimp"]
        self.fimps = [fimp_manipulation.Fimp(fimp_path) for fimp_path in self.fimp_names]
        self.fimp_answer = self.fimps[-1]
        self.ens_sizes = [3, 2, 1, 6]
        self.attribute_names = [["atr3", "atr10"], ["atr3", "atr10"], ["atr10", "atr3"], ["atr3", "atr10"]]

    def test_sizes(self):
        for fimp, size in zip(self.fimps, self.ens_sizes):
            self.assertEqual(fimp.get_ensemble_size(), size)

    def test_attribute_names(self):
        for fimp, names in zip(self.fimps, self.attribute_names):
            self.assertListEqual(fimp.get_attr_names(), names)

    def test_fimp_aggregation(self):
        aggregated = fimp_manipulation.fimp_aggregation(self.fimp_names[:-1])
        self.assertEqual(aggregated.get_ensemble_size(), self.fimp_answer.get_ensemble_size())
        self.assertListEqual(aggregated.get_attr_names(), self.fimp_answer.get_attr_names())
        aggregated_relevances = aggregated.get_relevances()
        answer_relevances = self.fimp_answer.get_relevances()
        self.assertEqual(len(aggregated_relevances), len(answer_relevances))
        for x, y in zip(aggregated_relevances, answer_relevances):
            self.assertEqual(len(x), len(y))
            for r1, r2 in zip(x, y):
                self.assertAlmostEqual(r1, r2, delta=10**-10)
        aggregated_ranks = aggregated.get_ranks()
        answer_ranks = self.fimp_answer.get_ranks()
        self.assertEqual(len(aggregated_ranks), len(answer_ranks))
        for x, y in zip(aggregated_ranks, answer_ranks):
            self.assertListEqual(x, y)
