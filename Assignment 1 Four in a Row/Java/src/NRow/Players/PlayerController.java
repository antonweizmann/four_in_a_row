package NRow.Players;

import NRow.Board;
import NRow.Heuristics.Heuristic;

/**
 * Abstract class defining Player
 */
public abstract class PlayerController {
  public final int playerId;
  protected int gameN;
  protected Heuristic heuristic;

  /**
   * Create human player, enabling human computer interaction through the console
   * @param playerId can take values 1 or 2 (0 = empty)
   * @param gameN N in a row required to win
   * @param heuristic the heuristic the player should use
   */
  public PlayerController(int playerId, int gameN, Heuristic heuristic) {
    this.playerId = playerId;
    this.gameN = gameN;
    this.heuristic = heuristic;
  }

  /**
   * @return The amount of times the heuristic was used to evaluate a boardstate
   */
  public int getEvalCount() {
    return heuristic.getEvalCount();
  }

  /**
   * Get a nice String representation for displaying the board
   */
  @Override
  public String toString() {
    return (playerId == 2) ? "O" : "X"; 
  }

  /**
   * Implement this method in the player classes
   * @param board the current board
   * @return column integer the player chose
   */
  public abstract int makeMove(Board board);
}
