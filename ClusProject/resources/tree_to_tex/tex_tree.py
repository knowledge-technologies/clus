import re


class Tree:
    def __init__(self, test=None, value=None, left=None, right=None):
        self.test = test
        self.prediction = value  # depents on the target type probably
        self.left = None  # yes branch
        self.right = None  # no branch
        self.parent = None

        if left is not None:
            self.update_left(left)
        if right is not None:
            self.update_right(right)

    def create_empty_tree(self):
        """
        Should return something as Tree(), but in a subclass
        :return:
        """
        raise Exception("This must be implemented by a subclass :)")

    def update_left(self, left):
        self.left = left
        left.parent = self

    def update_right(self, right):
        self.right = right
        right.parent = self

    def is_left(self):
        if self.parent is None:
            return False
        else:
            return self.parent.left == self

    def prediction_latex_string(self):
        """
        This must be implemented by the subclass.
        :return:
        """
        raise Exception("Implement this in your subclass :)")

    def set_prediction(self, match_object):
        """
        Sets the prediction field. This must be implemented
        :param match_object: See the explanation for the parse method.
        :return:
        """
        raise Exception("Implement this in your subclass :)")

    def latex_string(self, depth=0):
        """
        Gives the qtree representation of the tree.
        :param depth:
        :return:
        """
        def nicify(string):
            a = re.sub("_", "\\_", string)
            math = "<>"
            for c in math:
                a = re.sub(c, "$" + c + "$", a)
            return a
        children_description = ["", ""]
        if self.test is None:
            prediction = self.prediction_latex_string()
            node_description = "\\node[leaf]{{{}}}; ".format(prediction)
        else:
            node_description = "\\node[internal]{{{}}};".format(nicify(self.test))
            # children
            children_description[0] = "\\edge node[auto=right,pos=.6]{Y};" + self.left.latex_string(depth + 1)
            children_description[1] = "\\edge node[auto=left,pos=.6]{N};" + self.right.latex_string(depth + 1)
        prependix = ["", "\\Tree "][depth == 0]
        return "{}[.{} {}] ".format(prependix, node_description, "".join(children_description))


def parse(clus_tree_file, prediction_pattern, initial_tree):
    """
    Parses a tree in the clus form.
    :param clus_tree_file:
    :param prediction_pattern: pattern for regular expression such that
    re.search(prediction_pattern, line) is None if line does not describe a leaf.
    Otherwise it is a match object that can be passed to Tree.set_prediction method.
    :param initial_tree: an empty tree
    :return: the corresponding Tree object
    """
    def add_test_or_prediction(a_tree, line):
        hit = re.search(prediction_pattern, line)
        if hit is not None:  # a_tree = 1 Node
            a_tree.set_prediction(hit)
            return True
        else:
            a_tree.test = line.strip()
            return False

    yes_branch = "+--yes:"
    no_branch = "+--no:"
    tree = initial_tree
    current = tree
    with open(clus_tree_file) as f:
        for x in f:
            stripped = re.sub("\|", "", x).strip()  # e.g., convert '|      +--yes: something' to '+--: yes: something'
            if not stripped:
                continue
            is_yes = stripped.startswith(yes_branch)
            is_no = stripped.startswith(no_branch)
            if not (is_yes or is_no):  # the root of the whole tree
                if add_test_or_prediction(current, stripped):
                    current = current.parent
            else:
                child = tree.create_empty_tree()
                if is_yes:
                    current.update_left(child)
                    offset = len(yes_branch)
                else:
                    current.update_right(child)
                    offset = len(no_branch)
                current = child
                if add_test_or_prediction(current, stripped[offset:]):
                    current = current.parent
                while not (current.left is None or current.right is None or current.parent is None):
                    # while both children processed and current != root
                    current = current.parent
    return tree


HEADER = r"""\documentclass{article}
\usepackage[a0paper]{geometry}
\usepackage{tikz}
\usepackage{tikz-qtree}
\usetikzlibrary{positioning,shapes}

\begin{document}
\tikzset{
    treenode/.style = {rounded corners, ellipse,
        draw, align=center,
        top color=white,
        bottom color=blue!20,
        font=\Large},
    internal/.style = {treenode, bottom color=red!30},
    leaf/.style     = {treenode, shape=rectangle}
}

\begin{tikzpicture}%[thick,scale=0.7, every node/.style={scale=0.7}, every edge/.style={scale=0.7}]
[
grow                    = down,
level distance          = 7em,
edge from parent/.style = {draw, -latex},
every node/.style       = {font=\Large},
sloped
]"""

FOOTER = r"""\end{tikzpicture}

\end{document} """


def create_tex_file(clus_tree_file, out_tex_file, prediction_pattern, empty_tree):
    """
    Writes the tex representation of the tree in a text form as printed out by Clus. Depending on the size of the
    tree, you might have to change the line
    \\begin{tikzpicture} to
    \\begin{tikzpicture}[thick,scale=0.7, every node/.style={scale=0.7}, every edge/.style={scale=0.7}]
    (choose the appropriate value instaed of 0.7).
    :param clus_tree_file:
    :param out_tex_file:
    :param prediction_pattern: see parse method for the explanation
    :param empty_tree: an empty representative of the class you want to be the resulting tree of
    :return:
    """
    with open(out_tex_file, "w") as f:
        print(HEADER, file=f)
        print(parse(clus_tree_file, prediction_pattern, empty_tree).latex_string(), file=f)
        print(FOOTER, file=f)
