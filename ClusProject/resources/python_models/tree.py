# -*- coding: utf-8 -*-
"""
Created on Mon Mar 26 14:30:05 2018

@author: matej
"""

class Statistics:
    def fresh_stats(self, number_targets):
        raise NotImplementedError("Implement this in subclass")
    
    def add_another_stats(self, other, other_weight):
        raise NotImplementedError("Implement this in subclass")
        
    def stats_to_predictions(self):
        raise NotImplementedError("Implement this in subclass")
        
    def get_nb_targets(self):
        raise NotImplementedError("Implement this in subclass")


class RegressionStat(Statistics):
    def __init__(self, predicted_values):
        self.stats = predicted_values

    def __repr__(self):
        return str(self.stats)
        
    def fresh_stats(self, number_targets):
        return RegressionStat([0 for _ in range(number_targets)])
            
    def add_another_stats(self, other, other_weight):
        for i in range(len(self.stats)):
            self.stats[i] += other.stats[i] * other_weight
            
    def stats_to_predictions(self):
        return self.stats
    
    def get_nb_targets(self):
        return len(self.stats)
            
            
class BinaryNodeTest:
    def __init__(self, descriptive_index, test_function):
        """
        Constructor for the tests like x4 < 3.3 or x32 in ['a', 'b', 'd'] or x31 == 'f'
        
        Parameters
        ----
        descriptive_index : int
            The index of the attribute in the test.
        test_function : a function
            Something equivalent to lambda t: t < 3.3 or lambda t: t in ['a', 'b', 'c'] etc.
        """
        self.descriptive_index = descriptive_index
        self.test_function = test_function

    def __repr__(self):
        return "Test(index: {})".format(self.descriptive_index)
        
    def which_branch(self, xs, missing_value):
        i = self.descriptive_index
        yes_branch = 0
        no_branch = 1
        return None if xs[i] == missing_value else [no_branch, yes_branch][self.test_function(xs[i])]


class TreeNode:
    def __init__(self,
                 children=[],
                 branch_frequencies=[],
                 test=None,
                 prediction_statistics=None):
        """
        Constructor for this class.
        
        Parameters
        ----------
        children : a list of `TreeNode`
            a (possibly empty) list of children nodes
        branch_frequencies : a list of `float`
            A list of the same lenght as children. The i-th element tells what proportion of the instances
            from self go to the i-the child, hence sum(branch_frequencies) = 1  if the list is not empty.
        test : `BinaryNodeTest`
            Used when making predictions
        prediction_statistics : a list of `Statistics`
            A list of values from which the prediction is made.
        
        Returns
        -------
        TreeNode
        
        Raises
        ----
        AssertionError
            when children and branch_frequencies are not of the same length, or
            when not XOR(self.test is None, self.prediction_statistics is None)
        """
        self.children = children
        self.branch_frequencies = branch_frequencies
        self.test = test
        self.prediction_statistics = prediction_statistics
        
        assert len(self.children) == len(self.branch_frequencies)
        assert int(self.test is None) + int(self.prediction_statistics is None) == 1
        
        self.temp_statistics = None

    def __repr__(self):
        def pomo(x):
            return "None" if x is None else str(x)
        return "Node: test: {}, stat: {}, temp_stat: {}".format(pomo(self.test), pomo(self.prediction_statistics), pomo(self.temp_statistics))
        
    def is_leaf(self):
        """
        Tells whether the list of children is empty (self is a leaf) or not (self is an internal node)
        
        Returns
        ----
        bool
        """
        return not self.children

    def set_temp_statistics(self, statistics):
        self.temp_statistics = statistics
        
    def reset_temp_statistics(self):
        self.set_temp_statistics(None)


class Tree:
    def __init__(self, root):
        self.root = root
        
    def predict(self, xs, missing_value="?"):
        my_stack = [self.root]
        nb_targets = None
        while my_stack:
            current = my_stack[-1]
            has_been_processed = False
            if current.is_leaf():
                nb_targets = current.prediction_statistics.get_nb_targets()
                current.set_temp_statistics(current.prediction_statistics.fresh_stats(nb_targets))
                current.temp_statistics.add_another_stats(current.prediction_statistics, 1.0)
                has_been_processed = True
            else:
                # find branch
                the_branch = current.test.which_branch(xs, missing_value)
                if the_branch is not None:
                    # known value
                    children_to_process = [current.children[the_branch]]
                else:
                    # missing value
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
                else:
                    # go deeper in the tree
                    for child in children_to_process:
                        my_stack.append(child)
            if has_been_processed:
                my_stack.pop()
        prediction = self.root.temp_statistics.stats_to_predictions()
        self.root.reset_temp_statistics()
        return prediction
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
    