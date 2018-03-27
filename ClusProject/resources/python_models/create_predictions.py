import sys
import re


def load_nonsparse_arff(arff_file, load_data=False):
    """
    Returns relation, list of attributes and, if wanted, data table.

    Parameters
    ----------
    arff_file : string
        A path to the file, e.g., C:/Users/aneta/iris.arff
    load_data : bool
        False by default. If True, the data matrix is also loaded.

    Returns
    -------
    relation (string), attributes (list whose elements are of form [attribute name, attribute values]) and,
    if wanted, data table (list whose elements correspond to instances which are presented as lists of string values).
    """
    relation = None
    attributes = []
    data = []

    with open(arff_file) as f:
        for x in f:
            y = x.lower()
            if y.startswith("@data"):
                break
            elif y.startswith("@attribute"):
                name_values = re.search(" ([^ ]+) +(.+)", x)
                name = name_values.group(1)
                values = name_values.group(2)
                attributes.append([name, values])
            elif y.startswith("@relation"):
                relation = x[x.find(" "):].strip()
        assert relation is not None
        if load_data:
            for x in f:
                y = x.strip()
                if y:
                    data.append(y.split(","))
    return relation, attributes, data


def parse_index_interval(interval):
    if not interval:
        return []
    elif "-" not in interval:
        return parse_index_interval("{0}-{0}".format(interval))
    else:
        a, b = interval.split("-")
        return [i for i in range(int(a), int(b) + 1)]


def parse_index_intervals(intervals):
    return sorted([i for interval in intervals.split(",") for i in parse_index_interval(interval)])


def instances_generator(arff_file):
    def my_eval(value):
        try:
            return float(value)
        except ValueError:
            return value.strip()
    found = 0
    with open(arff_file) as f:
        for x in f:
            if "@data" in x.lower():
                break
        for x in f:
            if x.strip():
                candidate = [my_eval(t) for t in x.strip().split(",")]
                found += 1
                yield candidate
    print("Number of instances found:", found)


def create_predictions(python_ensemble_dir, python_ensemble_file,
                       arff_file,
                       descriptive_attributes_string,
                       key_attribute,
                       target_attributes_string,
                       num_trees,
                       out_file):
    """
    Computes predictions of the examples and writes everything to a file.
    :param python_ensemble_dir: Path to the directory that contains the file python_ensemble_file, e.g.,
    C:/Users/matej/results
    :param python_ensemble_file: Name of the file (without .py exentsion) where ensemble model methods are defined,
    e.g., forest_models for the file C:/Users/matej/results/forest_models.py
    :param arff_file: Path to the arff file, e.g., C:/Users/matej/data/something.arff
    :param descriptive_attributes_string: comma-separated string of intervals or single numbers that defined the
    1-based indices of descriptive attributes, e.g., 3,4-42,44,45
    :param key_attribute: None (if there is no key) or the 1-based index of the key
    :param target_attributes_string: the analogue of descriptive_attributes_string for target attributes
    :param num_trees: number of trees in the ensemble
    :param out_file: Path to the file where predictions are written to, e.g., C:/Users/matej/predictions/experiment1.txt
    :return:
    """
    def get_sublist(indices, whole_list, one_based=True):
        return [whole_list[t - one_based] for t in indices]

    sys.path.insert(0, python_ensemble_dir)
    exec("import {}".format(python_ensemble_file))
    print("Ensemble imported from", python_ensemble_file)
    descriptive_indices = parse_index_intervals(descriptive_attributes_string)
    target_indices = parse_index_interval(target_attributes_string)

    examples_generator = instances_generator(arff_file)
    _, attributes, _ = load_nonsparse_arff(arff_file, False)
    target_attributes = [attr[0] for attr in get_sublist(target_indices, attributes)]
    key_header = [] if key_attribute is None else [attributes[key_attribute - 1][0]]
    header = key_header + ["true_" + t for t in target_attributes] + ["predicted_" + t for t in target_attributes]
    with open(out_file, "w") as f:
        print(",".join(header), file=f)
        for example in examples_generator:
            condensed_example = [example[i - 1] for i in descriptive_indices if i <= len(example)]  # base 1 ==> -1
            prediction = eval("{}.ensemble_{}({})".format(python_ensemble_file, num_trees, condensed_example))
            key_string = "" if key_attribute is None else "{},".format(example[key_attribute - 1])
            true_values = get_sublist(target_indices, example)
            new_line = key_string + ",".join([str(t) for t in true_values + prediction])
            print(new_line, file=f)


