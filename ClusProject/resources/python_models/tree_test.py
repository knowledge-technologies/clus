# -*- coding: utf-8 -*-
"""
Created on Mon Mar 26 16:12:01 2018

@author: matej
"""

# Problem: with given attributes (x0, x1, x2), predict the value of y = (y0, y1)
# Implement the following tree:


# x2 > 5.0
# +--yes: [1.0, 8.0]: frequency = 0.2
# +--no:  x0 > 4.0 : frequency = 0.8
#        +--yes: [4.1, 3.2]: frequency = 0.6
#        +--no:  [2.1, 2.2]: frequency = 0.4
#
#
# Predict the values for
# x = (1.0, 1.0, 6.0) ---> [1.0, 8.0]
# x = (4.0, 1.0, '?') ---> 0.2 * [1.0, 8.0] + 0.8 * [2.1, 2.2] = [1.88, 3.36]
# x = ('?', 1.0, 5.0) ---> 0.6 * [4.1, 3.2] + 0.4 * [2.1, 2.2] = [3.3, 2.8]
# x = ('?', 1.0, '?') ---> 0.2 * [1.0, 8.0] + 0.8 * (0.6 * [4.1, 3.2] + 0.4 * [2.1, 2.2]) =  [2.84, 3.84]
# 
# Implement the following tree:
#
# [1.0, 21.21]

from tree import *
import unittest


class TestTreeStuff(unittest.TestCase):
    def setUp(self):
        self.xss = [(1.0, 1.0, 6.0), (4.0, 1.0, '?'), ('?', 1.0, 5.0), ('?', 1.0, '?')]
        self.yss = [[[1.0, 8.0], [1.0, 21.21]],
                    [[1.88, 3.36], [1.0, 21.21]],
                    [[3.3, 2.8], [1.0, 21.21]],
                    [[2.84, 3.84], [1.0, 21.21]]]

        # leaves
        self.node0_yes = TreeNode(prediction_statistics=RegressionStat([1.0, 8.0]))
        self.node0_no_yes = TreeNode(prediction_statistics=RegressionStat([4.1, 3.2]))
        self.node0_no_no = TreeNode(prediction_statistics=RegressionStat([2.1, 2.2]))
        # internal
        self.test_node0_no = BinaryNodeTest(0, test_function=lambda t: t > 4.0)
        self.test_node0 = BinaryNodeTest(2, test_function=lambda t: t > 5.0)
        self.node0_no = TreeNode(children=[self.node0_no_yes, self.node0_no_no],
                                 branch_frequencies=[0.6, 0.4],
                                 test=self.test_node0_no)
        self.node0 = TreeNode(children=[self.node0_yes, self.node0_no],
                              branch_frequencies=[0.2, 0.8],
                              test=self.test_node0)

        self.a_tree = Tree(self.node0)
        self.default_model = Tree(TreeNode(prediction_statistics=RegressionStat([1.0, 21.21])))

    def test_default(self):
        for xs, ys in zip(self.xss, self.yss):
            predicted = self.default_model.predict(xs)
            true_vals = ys[1]
            for i in range(len(predicted)):
                self.assertAlmostEqual(predicted[i], true_vals[i], delta=10**-10)

    def test_some_tree(self):
        for xs, ys in zip(self.xss, self.yss):
            true_vals = ys[0]
            predicted = self.a_tree.predict(xs)
            for i in range(len(predicted)):
                self.assertAlmostEqual(predicted[i], true_vals[i], delta=10**-10)
