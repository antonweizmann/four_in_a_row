package NRow.Players;

import java.util.Scanner;

import NRow.Board;
import NRow.Heuristics.Heuristic;

/**
 * Example player implementation controlled by a human (through System.in)
 */
public class HumanPlayer extends PlayerController {
  Scanner scanner = new Scanner(System.in);

  /**
   * Create human player, enabling human computer interaction through the console
   * @param playerId either 1 or 2
   * @param gameN N in a row required to win
   * @param heuristic the heuristic the player should use
   */
  public HumanPlayer(int playerId, int gameN, Heuristic heuristic) {
    super(playerId, gameN, heuristic);
  }

  /**
   * Show the human player the current board and ask them for their next move
   */
  @Override
  public int makeMove(Board board) {
    System.out.println(board);
    
    if (heuristic != null)
      System.out.println("Heuristic: " + heuristic + " calculated the best move is: "
          + (heuristic.getBestAction(playerId, board) + 1));
    
    System.out.println("Player " + this + "\nWhich column would you like to play in?");
    
    int column = scanner.nextInt();
    
    System.out.println("Selected Column: " + column);
    return column - 1;
  }

}
