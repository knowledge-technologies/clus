import sys
import re
import argparse


ENSEMBLE = "ensemble"
OBJECT = "object"
FUNCTION = "function"


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


def instances_generator(arff_file, from_inst=0, to_inst=float("inf")):
    def my_eval(attr_ind, attr_value):
        if attributes[attr_ind][1] == "numeric":
            try:
                return float(attr_value)
            except ValueError:
                return attr_value.strip()
        else:
            return attr_value.strip()

    _, attributes, _ = load_nonsparse_arff(arff_file)
    found = 0
    for walk in range(2):
        with open(arff_file) as f:
            for x in f:
                if "@data" in x.lower():
                    break
            if walk == 0:  # count the lines after @data
                instance = -1
                for instance, _ in enumerate(f):
                    pass
                found = instance + 1
                if found == 0:
                    print("Warning:", arff_file, "contains 0 instances.")
                # print("Number of instances is approximately", found)
            else:  # generate the instances
                for instance, x in enumerate(f):
                    if x.strip():
                        if (instance + 1) % instances_step == 0:
                            print("Instances generated:", instance + 1)
                        found += 1
                        if from_inst <= instance:
                            if instance <= to_inst:
                                candidate = [my_eval(i, t) for i, t in enumerate(x.strip().split(","))]
                                yield candidate
                            else:
                                break


def create_predictions(python_ensemble_dir, python_ensemble_file,
                       arff_file,
                       descriptive_attributes_string,
                       key_attribute,
                       target_attributes_string,
                       num_trees,
                       out_files,
                       first_instance,
                       last_instance,
                       model,
                       model_type):
    """
    Computes predictions of the examples and writes everything to a file.

    Parameters
    ----------
    python_ensemble_dir : string
        Path to the directory that contains the file python_ensemble_file, e.g., 'C:/Users/matej/results'
    python_ensemble_file : string
        Name of the file (without .py extension) where ensemble model methods are defined, e.g.,
        'forest_models' for the file C:/Users/matej/results/forest_models.py
    arff_file : string
        Path to the arff file, e.g., 'C:/Users/matej/data/something.arff'
    descriptive_attributes_string : string
        Comma-separated string of intervals or single numbers that define 1-based indices of descriptive attributes,
        e.g., '3,4-42,44,45'
    key_attribute : None or int
        None if there is no key, or the 1-based index of the key attribute otherwise
    target_attributes_string: string
        The analogue of descriptive_attributes_string for target attributes
    num_trees : int
        Number of trees in the ensemble
    out_files : string
        Path to the file where predictions are written to, e.g., 'C:/Users/matej/predictions/experiment1.txt'
    first_instance, last_instance : int
        Predictions are made for the instances with the 0-based indices
        from the interval [first_instance, last_instance].
    model : string
        The name of the model that will be used to make the predictions, e.g., 'ensemble', 'original' und so weiter.
    model_type : string
        Tells whether the models are given as functions or objects. Should not have any influence if the model
        is set to ensemble.
    """

    def get_sublist(indices, whole_list, one_based=True):
        return [whole_list[t - one_based] for t in indices]

    # load ensemble
    sys.path.insert(0, python_ensemble_dir)
    exec("import {}".format(python_ensemble_file))
    # print("Model(s) imported from", python_ensemble_file)
    if descriptive_attributes_string is None:
        descriptive_indices = None
        descriptive_need_conversion = True
    else:
        descriptive_indices = parse_index_intervals(descriptive_attributes_string)
        descriptive_need_conversion = False
    if target_attributes_string is None:
        target_indices = [-1]
        is_target_one_based = False
    else:
        target_indices = parse_index_intervals(target_attributes_string)
        is_target_one_based = True

    target_needs_conversion = min(target_indices) < 0
    if target_needs_conversion and max(target_indices) >= 0:
        raise ValueError("Target indices contain non-negative and negative numbers. Resolve this.")

    examples_generator = instances_generator(arff_file, first_instance, last_instance)
    _, attributes, _ = load_nonsparse_arff(arff_file, False)
    target_attributes = [attr[0] for attr in get_sublist(target_indices, attributes, is_target_one_based)]
    key_header = [] if key_attribute is None else [attributes[key_attribute - 1][0]]
    header = key_header + ["true_" + t for t in target_attributes] + ["predicted_" + t for t in target_attributes]
    out_fs = [open(out_file, "w") for out_file in out_files]
    for f in out_fs:
        print(",".join(header), file=f)

    for example in examples_generator:
        if target_needs_conversion:
            target_indices = [t if t >= 0 else len(example) + t + 1 for t in target_indices]
            target_needs_conversion = False
            # at this point, target indices are positive and 1-based
        if descriptive_need_conversion:
            forbidden = target_indices[:]
            if key_attribute is not None:
                forbidden.append(key_attribute)
            descriptive_indices = [d for d in range(1, 1 + len(example)) if d not in forbidden]
            descriptive_need_conversion = False
            # at this point, descriptive indices are positive and 1-based
        condensed_example = [example[i - 1] for i in descriptive_indices if i <= len(example)]  # base 1 ==> -1
        eval_triplet_ensemble = (python_ensemble_file, num_trees, condensed_example)
        eval_triplet_single = (python_ensemble_file, model, condensed_example)
        if model == ENSEMBLE:
            predictions = eval("{}.ensemble_{}({})".format(*eval_triplet_ensemble))
        elif model_type == OBJECT:
            predictions = [eval("{}.tree_{}.predict({})".format(*eval_triplet_single))]
        else:
            predictions = [eval("{}.{}({})".format(*eval_triplet_single))]
        key_string = "" if key_attribute is None else "{},".format(example[key_attribute - 1])
        true_values = get_sublist(target_indices, example)
        for prediction, f in zip(predictions, out_fs):
            new_line = key_string + ",".join([str(t) for t in true_values + prediction])
            print(new_line, file=f)
    for f in out_fs:
        f.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Option parser',
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument("-dir", "--ensemble_dir", default=None,
                        help="The directory where the ensemble python script with ensemble models is located, "
                             "e.g., C:/Experiments/results/")
    parser.add_argument("-ens", "--ensemble_file", default=None,
                        help='Name name of the ensemble models file, e.g., myForest_models '
                             '(and not C:/Experiments/results/myForest_models.py)')
    parser.add_argument("-a", "--arff_file", default=None,
                        help='Path to the arff file whose instances will the predictions be made for, '
                             'e.g., C:/Experiments/venus_test.arff')
    parser.add_argument("-da", "--descriptive_attributes", default=None,
                        help='Comma separated string of 1-based indices (or intervals thereof) '
                             'that define descriptive attributes, e.g., 2,3,4 or 2-6,8,12-21')
    parser.add_argument("-k", "--key_attribute", default=None,
                        help="1-based index of the key attribute if it exists. "
                             "Otherwise, do not specify this option.")
    parser.add_argument("-ta", "--target_attributes", default=None,
                        help='Comma separated string of 1-based indices (or intervals thereof) '
                             'that define target attributes, e.g., 2,3,4 or 2-6,8,12-21')
    parser.add_argument("-t", "--trees", default=None,
                        help="The number of trees, i.e., the size of the forest, e.g., 100")
    parser.add_argument("-o", "--output_file", default=None,
                        help="Comma separated strings of paths to the output files where predictions are saved to,"
                             " e.g., C:/Experiments/results/predicted100.pred,C:/Experiments/results/predicted200.pred")
    parser.add_argument("-s", "--instances_step", default=1000,
                        help="The 'frequency' of the notifications, how many instances were generated, e.g., 10. "
                             "If set to 10, you will be notified on the 10th, 20th, 30th, ... instance.")
    parser.add_argument("-fi", "--first_instance", default=0,
                        help="0-based index of the first instance that predictions are made for.")
    parser.add_argument("-li", "--last_instance", default=float("inf"),
                        help="0-based index of the last instance that predictions are made for.")
    parser.add_argument("-m", "--model_name", default=ENSEMBLE,
                        help="The name of the predictive model. Should be probably one of the following: "
                             "ensemble (if ensemble was built), "
                             "or original, pruned or default (if a single tree was built)")
    parser.add_argument("-mt", "--model_type", default=OBJECT,
                        help="Tells whether the models are saved as objects or functions. Possible values "
                             "are {} and {}".format(OBJECT, FUNCTION))

    # parser.print_help()
    arguments = parser.parse_args(sys.argv[1:])

    ensemble_directory = arguments.ensemble_dir
    ensemble_script = arguments.ensemble_file
    arff_path = arguments.arff_file
    descriptive_attributes = arguments.descriptive_attributes
    key_attribute_index = arguments.key_attribute
    if key_attribute_index is not None:
        key_attribute_index = int(key_attribute_index)
    target_attributes_str = arguments.target_attributes
    number_of_trees = int(arguments.trees)
    output_files = arguments.output_file.split(",")
    instances_step = int(arguments.instances_step)
    model_name = arguments.model_name
    m_type = arguments.model_type

    first_inst = arguments.first_instance
    last_inst = arguments.last_instance
    if first_inst != 0:
        first_inst = int(first_inst)
    if last_inst != float("inf"):
        last_inst = int(last_inst)

    # print("Start of creating predictions for", model_name)
    create_predictions(ensemble_directory, ensemble_script, arff_path, descriptive_attributes, key_attribute_index,
                       target_attributes_str, number_of_trees, output_files, first_inst, last_inst, model_name,
                       m_type)
