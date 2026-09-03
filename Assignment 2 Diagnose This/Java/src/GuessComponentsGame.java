import java.util.*;
import java.util.stream.Collectors;

public class GuessComponentsGame {
    /**
     * Allows the user to choose conflict sets for the game.
     * @return A list of lists of strings, where each inner list represents a conflict set chosen by the user.
     */
    public List<List<String>> chooseComponents() {
        // Print info message
        System.out.println("Choose your conflict sets.\n" +
                "For each set, separate components by using a single space.\n" +
                "Once you are done, type \"STOP\"");
        List<List<String>> chosenConflictSets = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        String prompt;

        // Ask for conflict sets
        while (true) {
            System.out.print("Conflict set >> ");
            prompt = scanner.nextLine();
            if (prompt.equals("STOP")) {
                scanner.close();
                break;
            }
            List<String> conflictSet = Arrays.asList(prompt.split("\\s+"));
            conflictSet = conflictSet.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
            chosenConflictSets.add(conflictSet);
        }

        assert !chosenConflictSets.isEmpty() : "You must choose at least one conflict set!";
        for (List<String> cs : chosenConflictSets) {
            assert !cs.isEmpty() : "You cannot have empty sets!";
        }
        return chosenConflictSets;
    }


    /**
     * Computes a score for a given set of guessed conflict sets.
     * @param hittingSets the ground truth sets
     * @param chosenHittingSets the guessed sets
     * @return a score between 0 and 100 representing the quality of the guess
     */
    public double scoreFunction(List<List<String>> hittingSets, List<List<String>> chosenHittingSets) {
        Set<Integer> matchedGuessed = new HashSet<>();
        double totalScore = 0;

        // Score each ground truth set by best matching guessed set
        for (List<String> hittingSet : hittingSets) {
            double bestScore = 0.0;
            int bestIdx = -1;
            for (int i = 0; i < chosenHittingSets.size(); i++) {
                List<String> chosenHittingSet = chosenHittingSets.get(i);
                double score = jaccardSimilarity(hittingSet, chosenHittingSet);
                if (score > bestScore) {
                    bestScore = score;
                    bestIdx = i;
                }
            }
            totalScore += bestScore;
            if (bestIdx >= 0) {
                matchedGuessed.add(bestIdx);
            }
        }
        
        // Penalize extra/too few sets in guessed list
        int extraSets = Math.abs(chosenHittingSets.size() - matchedGuessed.size());
        double penalty = extraSets / Math.max(hittingSets.size(), 1); // normalize

        // Normalize score by number of ground truth sets and apply penalty
        double finalScore = (totalScore / hittingSets.size()) - penalty;
        finalScore = Math.max(0, finalScore); // ensure score doesn't go below 0

        return finalScore * 100;
    }

    /**
     * Computes the Jaccard similarity between two sets
     * @param set1 the first set
     * @param set2 the second set
     * @return the Jaccard similarity between the two sets
     */
    private static double jaccardSimilarity(List<String> set1, List<String> set2) {
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        if (union.isEmpty()) { // both sets are empty, so they're identical
            return 1.0;
        }
        return (double) intersection.size() / union.size();
    }
}