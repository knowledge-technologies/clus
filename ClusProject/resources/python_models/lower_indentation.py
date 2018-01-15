import sys


def lower_indentation(py_file, out_file, steps):
    """
    It turns out that our trees can be too deep for Python. This method transforms every method of the form

    def my_name(xs):
        if some test:
            <something>
        else:
            <something else>

    in the input file to

    def my_name(xs):
        if some_test:
            return my_name_b0(xs)
        else:
            return my_name_b1(xs)

    def my_name_b0(xs):
        <something>

    def my_name_b1(xs):
        <something else>

    The new branch functions are transformed recursively. The upper example is for the case when steps = 1.
    If this value is higher, then a new branch function is created on the depth step.

    :param py_file: input python script
    :param out_file: output python script
    :param steps: maximal depth allowed
    :return:
    """
    def indented_line(pair):
        return "{}{}".format(pair[0]*"\t", pair[1])

    def process_one(lines, name):
        def new_name(orig, i):
            return "{}_b{}".format(orig, i)
        print("def {}(xs):".format(name), file=f)
        too_deep = []
        i = 0
        while i < len(lines):
            line = lines[i]
            if line[0] <= steps:
                print(indented_line(line), file=f)
                i += 1
            else:
                line = [line[0], "return {}(xs)".format(new_name(name, len(too_deep)))]
                print(indented_line(line), file=f)
                too_deep.append([])
                while i < len(lines) and lines[i][0] > steps:
                    line = lines[i]
                    too_deep[-1].append([line[0] - steps, line[1]])
                    i += 1
        print("", file=f)
        for i, sublist in enumerate(too_deep):
            subname = new_name(name, i)
            process_one(too_deep[i], subname)

    function_name = None
    list_of_lines = []
    should_process = False
    with open(out_file, "w") as f:
        with open(py_file) as g:
            for x in g:
                if x.startswith("def "):
                    function_name = x[x.find("def ") + 4:x.find("(")]  # 'def something(xs)' ---> 'something'
                    list_of_lines = []
                    should_process = True
                else:
                    # assumes tabs as indent indicators
                    indent = x.count("\t")
                    if indent == 0:
                        if should_process:  # the end of previous function
                            process_one(list_of_lines, function_name)
                            should_process = False
                        print(x.strip(), file=f)
                    else:
                        list_of_lines.append([indent, x.strip()])
            if should_process:
                process_one(list_of_lines, function_name)


print("Usage: python lower_indentation.py path/to/input/py/script path/to/output/py/script max_depth")
in_f = sys.argv[1]
out_f = sys.argv[2]
depth = int(sys.argv[3])
lower_indentation(in_f, out_f, depth)
