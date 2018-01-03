import tex_tree


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


if 0:
    create_tex_file("testClassificationTree.txt", "testClassificationTree.tex")
