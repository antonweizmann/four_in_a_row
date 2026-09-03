def reduce(node,probs,evidence=None):
    if not evidence:
        return probs
    columns = probs.columns.tolist()
    try:
        i = columns.index(node)
        reduced = pd.DataFrame(list(filter(lambda x: x[i] == evidence,probs.values)),columns=columns)
        return reduced
    except:
        print("{} is not found in the probabilities".format(node))