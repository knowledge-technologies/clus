# Python stuff for Clus+

As you may know, Clus+ supports Python output in some cases. Currently, the Python-models come in two different formats:

- as function: Every tree is written as a if-else function, something like
```
def tree(xs):
    if xs[0] < 3.3:
        return [2.14, 332.3]
    else:
        if xs[2] == "a":
            return [4.3, 3.2]
        else:
            return [6.66, 6.66]
```
This does not allow for the missing values in `xs`.
- as object: Every tree is represented by a `Tree` object, something like
```
node_no_yes = Node(prediction=[4.3, 3.2])
node_no_no = Node(prediction=[6.66, 6.66])
node_no = Node(test=Test(attribute_index=2, test_function=lambda t: t == "a"), children=[node_no_yes, node_no_no])
node_yes = Node(prediction=[2.14, 332.3])
node = Node(attribute_index=0, test_function=lambda t: t < 3.3), children=[node_yes, node_no])
tree = Tree(root=node)
```
This also supports missing values.

Option that controls the Python-output type is `[Output] PythonOutputType` and can be set to `Function` or `Object`.

# Directory description

This directory contains some usefull scripts that will make the use of Clus-created Python-models easier.
Below, we give a brief description of the files:

- `create_predictions.py`: One you have a random forest or bagging or Extra-trees-ensemble model, you can use this script for making predictions.
   See the documentation there and the commented out example for more details.
- `lower_indentation.py`: Used in the case when the trees are deep and represented as if-else functions. This script rewrites the code, so that
   it defines some subtrees as new functions (and applies this recursively).
   In that way, the depth of the trees (hence the file size) can be substantially reduced.
- `tree_as_object.py`: Defines the classes that are necessary for the trees given as Python-object to work. This is copied to your experiment
  direcory if the optins `[Output]: PythonModelType = Object` is used.
- `trees_as_object_test.py`: Some basic tets for the upper file.