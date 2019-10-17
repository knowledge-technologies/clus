def aggregate_classification(predictions, sizes=None):
    """
    Finds the majority class over the members of an ensemble.
    :param predictions: A list of single tree predictions. Single tree predictions are given as a list of
    (str, double) tuples. Each tuple belong to one target and is of form (predicted class, probability).
    Probabilities are ignored during the aggregation.
    :param sizes: List of ensemble sizes. If None, this is set to len(predictions).
    :return: A list of predictions for different ensemble sizes. Each element is a list [class1, ...],
    whose i-th element is the class value of the i-th target that got most votes in the ensemble.
    """
    n = len(predictions)
    m = len(predictions[0])
    if sizes is None:
        sizes = [n]
    aggregated = []
    counts = [{} for _ in range(m)]
    size_index = 0
    for i in range(n):
        for j in range(m):
            pred, _ = predictions[i][j]
            if pred not in counts[j]:
                counts[j][pred] = 0
            counts[j][pred] += 1
        if sizes[size_index] == i + 1:
            aggregated.append([max(counts[j], key=lambda prediction: counts[j][prediction]) for j in range(m)])
            size_index += 1
    return aggregated


def aggregate_multi_label(predictions, sizes=None, threshold=None):
    """
    Finds the average probability for each label and ensemble size.
    Possibly, these values are then thresholded into 0/1 predictions.
    :param predictions: For each single tree, a list of pairs (label name, probability) if threshold=None,
    and list of '0's and '1's otherwise.
    :param sizes: List of ensemble sizes. If None, this is set to len(predictions).
    :param threshold: is not None, we return (for a given (label, p) pair) '1' if p >= threshold and '0' otherwise.
    :return: A list of predictions for different ensemble sizes. Each element is a list [element1, ...],
    whose i-th element is i) (label, average probability) pair if threshold=None, and ii) '1' or '0' otherwise.
    """
    n = len(predictions)
    m = len(predictions[0])
    if sizes is None:
        sizes = [n]
    aggregated = []
    probabilities = [[label, 0.0] for label, _ in predictions[0]]
    size_index = 0
    for i in range(n):
        for j in range(m):
            probabilities[j][1] += predictions[i][j][1]
        if sizes[size_index] == i + 1:
            aggregated.append([(label, p / sizes[size_index]) for label, p in probabilities])
            size_index += 1
    if threshold is not None:
        aggregated = [['1' if p >= threshold else '0' for _, p in pairs] for pairs in aggregated]
    return aggregated


def aggregate_regression(predictions, sizes=None):
    """
    Finds the average predicted value for each target and ensemble size.
    :param predictions: For each single tree, a list of floats (predictions).
    :param sizes: List of ensemble sizes. If None, this is set to len(predictions).
    :return: Average predictions over the trees.
    """
    n = len(predictions)
    m = len(predictions[0])
    if sizes is None:
        sizes = [n]
    sums = [0 for _ in range(m)]
    aggregated = []
    size_index = 0
    for i in range(n):
        for j in range(m):
            sums[j] += predictions[i][j]
        if sizes[size_index] == i + 1:
            aggregated.append([sums[j] / sizes[size_index] for j in range(m)])
            size_index += 1
    return aggregated


def aggregation_other():
    return None
