from random import random, seed


def generate_multilabel(num_feats, num_targets, num_inst):
    """
    Generates an mlc arff: first num_feats // 2 are nominal atributes, the second half are numeric.
    Targets: i-th of first num_targets // 2 equals the  i-th nominal attribute, the rest are defined via the last
    num_targets // 2 numeric attributes as target = attribute >= 1
    """
    seed(12345)
    file = "testMlc"
    new_file = "{}.arff".format(file)


    matrix = [[0.0 for j in range(num_feats + num_targets)] for i in range(num_inst)]
    for i in range(num_inst):
            matrix[i][:num_feats] = [round(2 * random(), 4) for _ in range(num_feats)]
            nominal = num_feats // 2
            matrix[i][:nominal] = [int(t) for t in matrix[i][:nominal]]
            via_nominal = num_targets // 2
            via_numeric = num_targets - via_nominal
            matrix[i][num_feats:num_feats + via_nominal] = [matrix[i][j] for j in range(via_nominal)]
            matrix[i][num_feats + via_nominal:] = [int(matrix[i][j - num_targets]) for j in range(-via_numeric, 0)]
    with open(new_file, "w") as f:
        print("% How this was generated:", file=f)
        print("% - {} random features from interval [0, 2)".format(num_feats), file=f)
        print("% - first {0} features: first {0} features > 1".format(nominal), file=f)
        print("% - first {0} targets: equal first {0} features".format(via_numeric), file=f)
        print("% - last {0} targets: last {0} features > 1".format(via_nominal), file=f)
        print("@relation mlcTest", file=f)
        for i in range(1, nominal + 1):
            print("@attribute feat{} {{0,1}}".format(i), file=f)
        for i in range(nominal + 1, num_feats + 1):
            print("@attribute feat{} numeric".format(i), file=f)
        for i in range(num_targets):            
            print("@attribute tar{} {{0,1}}".format(i + 1), file=f)
        print("@data", file=f)
        for x in matrix:
            print(",".join([str(t) for t in x]), file=f)


def generate_mtr(num_feats, num_targets, num_inst):
    seed(123)
    file = "testMTR"
    new_file = "{}.arff".format(file)
    matrix = [[0.0 for j in range(num_feats + num_targets)] for i in range(num_inst)]
    for i in range(num_inst):
            matrix[i][:num_feats] = [round(2 * random(), 4) for _ in range(num_feats)]
            nominal = num_feats // 2
            matrix[i][:nominal] = [int(t) for t in matrix[i][:nominal]]
            via_nominal = num_targets // 2
            via_numeric = num_targets - via_nominal
            matrix[i][num_feats:] = [(matrix[i][0] + matrix[i][num_feats - 1]) / 2 for j in range(num_targets)]
    with open(new_file, "w") as f:
        print("% How this was generated:", file=f)
        print("% - {} random features from interval [0, 2)".format(num_feats), file=f)
        print("% - first {0} features: first {0} features > 1".format(nominal), file=f)
        print("% - targets: average of 1st and LAst feature".format(via_numeric), file=f)
        print("@relation mtrTest", file=f)
        for i in range(1, nominal + 1):
            print("@attribute feat{} {{0,1}}".format(i), file=f)
        for i in range(nominal + 1, num_feats + 1):
            print("@attribute feat{} numeric".format(i), file=f)
        for i in range(num_targets):            
            print("@attribute tar{} numeric".format(i + 1), file=f)
        print("@data", file=f)
        for x in matrix:
            print(",".join([str(t) for t in x]), file=f)


def generate_mix(num_feats, num_targets, num_inst):
    seed(321)
    file = "testMixed"
    new_file = "{}.arff".format(file)
    matrix = [[0.0 for j in range(num_feats + num_targets)] for i in range(num_inst)]
    for i in range(num_inst):
            matrix[i][:num_feats] = [round(2 * random(), 4) for _ in range(num_feats)]
            nominal = num_feats // 2
            matrix[i][:nominal] = [int(t) for t in matrix[i][:nominal]]
            via_nominal = num_targets // 2
            via_numeric = num_targets - via_nominal
            matrix[i][num_feats:num_feats + via_nominal] = [matrix[i][j] for j in range(via_nominal)]
            matrix[i][num_feats + via_nominal:] = [matrix[i][j - num_targets] for j in range(-via_numeric, 0)]
    with open(new_file, "w") as f:
        print("% How this was generated:", file=f)
        print("% - {} random features from interval [0, 2)".format(num_feats), file=f)
        print("% - first {0} features: first {0} features > 1".format(nominal), file=f)
        print("% - first {0} targets: equal first {0} features".format(via_numeric), file=f)
        print("% - last {0} targets: equal last {0} features".format(via_nominal), file=f)
        print("@relation mixTest", file=f)
        for i in range(1, nominal + 1):
            print("@attribute feat{} {{0,1}}".format(i), file=f)
        for i in range(nominal + 1, num_feats + 1):
            print("@attribute feat{} numeric".format(i), file=f)
        for i in range(via_nominal):            
            print("@attribute tar{} {{0,1}}".format(i + 1), file=f)
        for i in range(via_numeric):            
            print("@attribute tar{} numeric".format(i + 1 + via_nominal), file=f)
        print("@data", file=f)
        for x in matrix:
            print(",".join([str(t) for t in x]), file=f)


if 0:
    generate_multilabel(7, 4, 13)
if 0:
    generate_mtr(7, 4, 13)
if 1:
    generate_mix(7, 4, 13)
