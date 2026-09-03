import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Enable assertions
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        loader.setDefaultAssertionStatus(true);

        // Program constants
        final String DOCUMENTPATH = "circuits/circuit3.txt"; // Make sure this is the relative path
        final boolean GAME = true;

        GuessComponentsGame gcg = null;
        List<List<String>> chosenConflictSets = null;

        if (GAME) {
            // If you play the game, choose conflict sets, compute hitting sets
            gcg = new GuessComponentsGame();
            chosenConflictSets = gcg.chooseComponents();
            System.out.println("\nYour chosen conflict sets: " + chosenConflictSets);
            HittingSets chs = new HittingSets(chosenConflictSets);
            System.out.println("Your hitting sets: " + chs.getHittingSets());
            System.out.println("Your minimal hitting sets: " + chs.getMinimalHittingSets() + "\n");
        }

        // Collect conflict sets
        ConflictSetRetriever csr = new ConflictSetRetriever(DOCUMENTPATH);
        List<List<String>> conflictSets = csr.retrieveConflictSets();
        System.out.println("Actual conflict sets: " + conflictSets);

        // Collect minimal hitting sets
        if (conflictSets.isEmpty()) {
            System.out.println("This circuit works correctly, there are no faulty components!");
        } else {
            HittingSets hs = new HittingSets(conflictSets);
            System.out.println("Hitting sets: " + hs.getHittingSets());
            System.out.println("Minimal hitting sets: " + hs.getMinimalHittingSets() + "\n");
        }

        // Give score on similarity between the two sets
        if (GAME) {
            double score = gcg.scoreFunction(conflictSets, chosenConflictSets);
            System.out.printf("Your score: %.2f%%", score);
        }
        csr.getCtx().close();
    }
}
