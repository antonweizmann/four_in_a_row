import re


def choose_components():
    """
    UI loop for letting the user choose their own components.
    """
    print("Choose your conflict sets.\n"
          "Separate components within a conflict set by using a space.\n"
          "Make separate conflict sets by hitting the enter key.\n"
          "Once you are done, type \"STOP\"")
    chosen_conflict_sets = []
    prompt = ""
    while prompt != "STOP":
        prompt = input("Conflict set >> ")
        if "STOP" in prompt.upper():
            break
        chosen_conflict_set = re.split(r" +", prompt)
        chosen_conflict_set = list(filter(len, chosen_conflict_set))
        chosen_conflict_sets.append(chosen_conflict_set)
    if len(chosen_conflict_sets) == 0:
        chosen_conflict_sets = [[]]
    return chosen_conflict_sets


def jaccard_similarity(set1, set2):
    """
    Jaccard similarity function of two sets.
    Takes into account empty sets.

    :param set1: list of sets to compare to the other list of sets
    :param set2: list of sets to compare to the other list of sets
    :return: the Jaccard similarity score
    """
    return len(set1 & set2) / len(set1 | set2) if set1 | set2 else 1.0


def score_function(hitting_sets, chosen_hitting_sets):
    """
    Custom scoring function for comparing the chosen hitting sets to the ground
    truth hitting sets.
    Finds the most similar sets by finding the most similar set of each ground
    truth set. Penalizes for any extra/too little sets in the list.

    :param hitting_sets: list of lists of hitting sets.
    :param chosen_hitting_sets: list of lists of hitting sets resulting from
                                chosen conflict sets.
    :return: the score as float (between 0-100)
    """
    hitting_sets = [set(s) for s in hitting_sets]
    chosen_hitting_sets = [set(s) for s in chosen_hitting_sets]

    matched_guessed = set()
    total_score = 0

    # Score each ground truth set by best matching guessed set
    for hitting_set in hitting_sets:
        best_score = 0
        best_idx = None
        for idx, chosen_hitting_set in enumerate(chosen_hitting_sets):
            score = jaccard_similarity(hitting_set, chosen_hitting_set)
            if score > best_score:
                best_score = score
                best_idx = idx
        total_score += best_score
        if best_idx is not None:
            matched_guessed.add(best_idx)

    # Penalize extra/too few sets in guessed list
    extra_sets = abs(len(chosen_hitting_sets) - len(matched_guessed))
    penalty = extra_sets / max(len(hitting_sets), 1)  # normalize

    # Normalize score by number of ground truth sets and apply penalty
    final_score = (total_score / len(hitting_sets)) - penalty
    final_score = max(0, final_score)  # ensure score doesn't go below 0

    return final_score * 100

