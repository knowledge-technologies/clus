# -*- coding: utf-8 -*-
"""
Created on Mon Mar 26 14:30:05 2018

@author: matej
"""
import random


random.seed(123)


class Statistics:
    """
    Superclass for the statistics that correspond to different learning tasks, e.g., (multi-target) regression.

    For a concrete implementation of the methods listed below, see `RegressionStat`.

    Methods
    -------
    fresh_stats(number_targets)
        Creates a new objects with neutral values of fields, i.e., the values that this statistic would have
        if the corresponding leaf node was empty.
    add_another_stats(other, other_weight)
        Adds up or somehow joins two different predictions into one.
    stats_to_predictions()
        Converts statistics to predictions, e.g., in the classification case,
        this would compute the most frequent class from the class counts.
        In the regression case, this simply returns current averages.
    get_nb_targets()
        Returns the number of targets.
    """

    def fresh_stats(self, number_targets):
        raise NotImplementedError("Implement this in subclass")

    def add_another_stats(self, other, other_weight):
        raise NotImplementedError("Implement this in subclass")

    def stats_to_predictions(self):
        raise NotImplementedError("Implement this in subclass")

    def get_nb_targets(self):
        raise NotImplementedError("Implement this in subclass")


class RegressionStat(Statistics):
    """
    Implementation of `Statistics` for (multi-target) regression task.
    """

    def __init__(self, predicted_values):
        """
        Constructor for this class. Initializes the only field (stats),
        which is a list whose i-th element stores the current mean of
        the i-th target.

        Parameters
        ----------
        predicted_values : list
            List of floats that represent the current means for each target.
        """
        self.stats = predicted_values

    def __repr__(self):
        return str(self.stats)

    def fresh_stats(self, number_targets):
        """
        Creates a `RegressionStat` object with zeros as current prediction for each target.
        """
        return RegressionStat([0 for _ in range(number_targets)])

    def add_another_stats(self, other, other_weight):
        """
        Adds the prediction :math:`o_i` of the other `RegressionStat` object to the current prediction :math:`c_i`,
        for all i, :math:`0\leq i < n`, where :math:`n` is the number of targets. New predictions are defined as
        :math:`w\; o_i + c_i`, where :math:`i` is given as parameter `other_weight`.

        Parameters
        ----------
        other : RegressionStat
            Another regression statistic
        other_weight: float
            Weight for the other statistic.
        """
        for i in range(len(self.stats)):
            self.stats[i] += other.stats[i] * other_weight

    def stats_to_predictions(self):
        """
        Computes predictions.

        Returns
        ------
        list
            This method simply returns `self.stat`.
        """
        return self.stats

    def get_nb_targets(self):
        """
        Computes the number of targets.

        Returns
        -------
        The length of `self.stats`.
        """
        return len(self.stats)


class ClassificationStat(Statistics):
    """
    Implementation of `Statistics` for (multi-target) classification task.
    That includes (hierarchical) multi-label classification.
    """

    def __init__(self, predicted_values):
        """
        Constructor for this class. Initializes the only field (stats),
        which is a list whose i-th element stores the current mean of
        the i-th target.

        Parameters
        ----------
        predicted_values : list
            List of pairs (majority class, probability) that represent the current means for each target.
        """
        self.stats = predicted_values

    def __repr__(self):
        return str(self.stats)

    def fresh_stats(self, number_targets):
        """
        Creates a `ClassificationStat` object with zeros as current prediction for each target.
        """
        return ClassificationStat([(None, -1.0) for _ in range(number_targets)])

    def add_another_stats(self, other, other_weight):
        """
        Finds the majority class between the current and other prediction.
        Compares the thresholds. The other threshold is multiplied by the other_weight.

        Parameters
        ----------
        other : ClassificationStat
            Another classification statistic
        other_weight: float
            Weight for the other statistic.
        """
        for i in range(len(self.stats)):
            p0 = self.stats[i][1]
            p1 = other.stats[i][1]
            if p0 < p1 * other_weight:
                self.stats[i] = (other.stats[i][0], p1 * other_weight)

    def stats_to_predictions(self):
        """
        Computes predictions.

        Returns
        ------
        list
            This method simply returns `self.stat`.
        """
        return self.stats

    def get_nb_targets(self):
        """
        Computes the number of targets.

        Returns
        -------
        The length of `self.stats`.
        """
        return len(self.stats)


class BinaryNodeTest:
    """
    Class that implements tests like
    :math:`x_4 < 3.3` or
    :math:`x_{32} \in \{a, b, d\}` or
    :math:`x_{0} == f`.
    """

    def __init__(self, descriptive_index, test_function):
        """
        Parameters
        ----
        descriptive_index : int
            The 0-based index of the attribute that appears in the test.
        test_function : a function
            Something equivalent to `lambda t: t < 3.3` or `lambda t: t in ['a', 'b', 'd']` or `lambda t: t == 'f'` etc.
        """
        self.descriptive_index = descriptive_index
        self.test_function = test_function

    def __repr__(self):
        return "Test(index: {})".format(self.descriptive_index)

    def which_branch(self, xs, missing_value):
        """
        Computes the branch that example should follow further.

        Parameters
        ----------
        xs : list
            List of descriptive values, whose `self.descriptive_index`-th component
            is used in the evaluation of the `self.test_function`.
        missing_value :
            Something that represents missing value, e.g., `"?"`.

        Returns
        -------
        int or None
            If the `self.descriptive_index`-th component equals `missing_value`, None is returned. Otherwise,
            we return `0` if the test evaluates to True, and `1` otherwise.
        """
        i = self.descriptive_index
        yes_branch = 0
        no_branch = 1
        return None if xs[i] == missing_value else [no_branch, yes_branch][self.test_function(xs[i])]


class TreeNode:
    unknown_branch = -1

    def __init__(self,
                 children=None,
                 branch_frequencies=None,
                 test=None,
                 prediction_statistics=None):
        """
        Parameters
        ----------
        children : a list of `TreeNode`
            a (possibly empty) list of children nodes
        branch_frequencies : a list of `float`
            A list of the same length as children. The i-th element tells what proportion of the instances
            from self go to the i-the child, hence sum(branch_frequencies) = 1  if the list is not empty.
        test : `BinaryNodeTest` or None
            Used when making predictions. For the leaves, this should be `None`.
        prediction_statistics : `Statistics`
            From this field, prediction are made. For the internal nodes, this should be `None`.

        Raises
        ----
        AssertionError
            when `children` and `branch_frequencies` are not of the same length, or
            when `not XOR(self.test is None, self.prediction_statistics is None)`
        """
        self.children = children if children is not None else []
        self.branch_frequencies = branch_frequencies if branch_frequencies is not None else []
        self.test = test
        self.prediction_statistics = prediction_statistics

        assert len(self.children) == len(self.branch_frequencies)
        assert int(self.test is None) + int(self.prediction_statistics is None) == 1

        self.the_branch = TreeNode.unknown_branch  # Which branch to follow when predicting

        # Statistics that are used in the internal nodes for predictions of a current tuple.
        # The value of this field should always be None, except for the time when the predictions
        # are made from the tree which the self belongs to.
        self.temp_statistics = None

    def __repr__(self):
        def helper(x):
            return "None" if x is None else str(x)

        return "Node(test: {}, stat: {}, temp_stat: {})".format(helper(self.test),
                                                                helper(self.prediction_statistics),
                                                                helper(self.temp_statistics))

    def is_leaf(self):
        """
        Tells whether the list of children is empty (self is a leaf) or not (self is an internal node): checks whether
        `self.children` is empty.

        Returns
        -------
        bool
        """
        return not self.children

    def set_temp_statistics(self, statistics):
        self.temp_statistics = statistics

    def reset_temp_statistics(self):
        self.set_temp_statistics(None)


class Tree:
    """
    Class that implements binary decision trees.
    """

    def __init__(self, root):
        """
        Parameters
        ----------
        root : `BinaryTreeNode`
            The root of the tree.
        """
        self.root = root

    def predict(self, xs, missing_value="?", randomize_unknown=False):
        """
        Predicts the target value(s) that correspond to the descriptive values `xs`.
        If the value of the attribute that is needed by a test in the tree is missing,
        prediction for this example would be the average of predictions of the branches
        that go from this node. The predictions of different branches are weighted with
        `TreeNode.branch_frequencies`.

        Parameters
        ----------
        xs : list
            A data example which predictions are made for, e.g., `[31.0, "male", 31459.2, "brown"]`.
        missing_value : str
            A thing that represents the missing value, `"?"` by default.
        randomize_unknown : bool
            Whether a random branch should be chosen or not when the value of the attribute in test is missing.

        Returns
        -------
        Prediction for the target value(s) for the example `xs`.
        """
        my_stack = [self.root]
        nb_targets = None
        while my_stack:
            current = my_stack[-1]
            has_been_processed = False
            if current.is_leaf():
                if nb_targets is None:
                    nb_targets = current.prediction_statistics.get_nb_targets()
                current.set_temp_statistics(current.prediction_statistics.fresh_stats(nb_targets))
                current.temp_statistics.add_another_stats(current.prediction_statistics, 1.0)
                has_been_processed = True
            else:
                # find branch
                if current.the_branch == TreeNode.unknown_branch:
                    the_branch = current.test.which_branch(xs, missing_value)
                    current.the_branch = the_branch
                else:
                    the_branch = current.the_branch
                # missing values corrections
                if the_branch is not None:  # known value
                    children_to_process = [current.children[the_branch]]
                else:  # missing value --> average of children or randomized with respect to branch frequencies
                    if randomize_unknown:
                        the_branch = int(random.random() > current.branch_frequencies[0])
                        current.the_branch = the_branch
                        children_to_process = [current.children[the_branch]]
                    else:
                        children_to_process = current.children
                all_children_processed = True
                for child in children_to_process:
                    if child.temp_statistics is None:
                        all_children_processed = False
                        break
                # compute predictions if possible
                if all_children_processed:
                    # compute predictions
                    current.set_temp_statistics(children_to_process[0].temp_statistics.fresh_stats(nb_targets))
                    if the_branch is not None:
                        current.temp_statistics.add_another_stats(current.children[the_branch].temp_statistics, 1.0)
                    else:
                        for child, branch_freq in zip(current.children, current.branch_frequencies):
                            current.temp_statistics.add_another_stats(child.temp_statistics, branch_freq)
                    # leave no trace
                    for child in children_to_process:
                        child.reset_temp_statistics()
                    has_been_processed = True
                    current.the_branch = TreeNode.unknown_branch
                else:
                    # go deeper in the tree
                    for child in children_to_process:
                        my_stack.append(child)
            if has_been_processed:
                my_stack.pop()
        prediction = self.root.temp_statistics.stats_to_predictions()
        self.root.reset_temp_statistics()
        return prediction
