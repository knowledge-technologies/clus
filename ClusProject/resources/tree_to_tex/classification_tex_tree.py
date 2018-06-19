import tex_tree
import sys


class ClassificationTree(tex_tree.Tree):
    def __init__(self, *args):
        super().__init__(*args)

    def create_empty_tree(self):
        return ClassificationTree()

    def prediction_latex_string(self):
        val = self.prediction[0]
        with_val_ex = int(float(self.prediction[1]))
        all_ex = int(self.prediction[2])
        return "{} ({}/{})".format(val, with_val_ex, all_ex)

    def set_prediction(self, match_object):
        self.prediction = (match_object.group(1), match_object.group(2), match_object.group(3))


def create_tex_file(clus_tree_file, out_tex_file):
    classification_pattern = "\[(.+)\] \[(.+)\]: (.+)"
    tex_tree.create_tex_file(clus_tree_file, out_tex_file, classification_pattern, ClassificationTree())

# A test for ClassificationTree
# create_tex_file("testClassificationTree.txt", "testClassificationTree.tex")

try:
    in_file = sys.argv[1]
    out_file = sys.argv[2]
    create_tex_file(in_file, out_file)
except:
    this_file = __file__
    i = this_file.rfind("/")
    if i < 0:
        i = this_file.rfind("\\")
    print("Something went wrong, the correct usage is:")
    print("python", __file__, "inputFile", "outputFile")
    print("e.g.,\npython", this_file[i + 1:], "C:/Users/Igor/tree.txt", "C:/Users/Jure/tree.tex")
    print("where")
    print("- python refers to Python3")
    print("- intputFile is a path to the file, containing a tree as found in the Clus's .out file")
    print("- outFile is a path to the output file tex tree will be created.")
    raise
 