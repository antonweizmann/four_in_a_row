package NRow;

import java.util.Arrays;

public class Board {
  public final int width;
  public final int height;
  private int[][] boardState; // 2D integer array containing 0's, or else playerId's in case a spot has been claimed

  /**
   * Constructor for creating a new empty board
   * @param width
   * @param height
   */
  Board(int width, int height) {
    this.width = width;
    this.height = height;
    this.boardState = new int[width][height];
  }

  /**
   * Constructor for cloning a board based on another board class
   * @param other
   */
  public Board(Board other) {
    this.width = other.width;
    this.height = other.height;
    this.boardState = other.getBoardState();
  }

  /**
   * Constructor for cloning a board based on a boardstate
   */ 
  public Board(int[][] state) {
    this.width = state.length;
    this.height = state[0].length;
    this.boardState = state;
  }

  /**
   * @param x
   * @param y
   * @return The value of a certain coordinate in the board
   */
  public int getValue(int x, int y) {
    return boardState[x][y];
  }

  /**
   * @return Cloned int array of the board state
   */
  public int[][] getBoardState() {
    return Arrays.stream(boardState).map(int[]::clone).toArray(int[][]::new);
  }

  /**
   * Let player playerId make a move in column x 
   * @param x
   * @param playerId
   * @return true if succeeded
   */
  public boolean play(int x, int playerId) {
    for (int i = this.boardState[0].length - 1; i >= 0; i--) {
      if (this.boardState[x][i] == 0) {
        this.boardState[x][i] = playerId;
        return true;
      }
    }
    return false;
  }

  /**
   * Returns if a move is valid
   * @param x column of the action
   * @return true if spot is not taken yet
   */
  public boolean isValid(int x) {
    return getBoardState()[x][0] == 0;
  }

  /**
   * Gets a new board given a player and their action
   * @param x column of the action
   * @param playerId player that takes the action
   * @return a *new* Board object with the resulting state
   */
  public Board getNewBoard(int x, int playerId) {
    int[][] newBoardState = getBoardState();
    for(int i = newBoardState[0].length-1; i >= 0 ; i--) {
      if(newBoardState[x][i] == 0) {
        newBoardState[x][i] = playerId;
        return new Board(newBoardState);
      }
    }
    return new Board(newBoardState);
  }

  /**
   * Draw a human readable representation of the board
   */
  @Override
  public String toString() {
    String divider = " ";
    String divider2 = " ";
    String numberRow = "|";
    
    for (int i = 0; i < boardState.length; i++) {
      divider += "--- ";
      divider2 += "=== ";
      numberRow += " " + (i + 1) + " |";
    }

    String output = "";
    
    for (int i = 0; i < boardState[0].length; i++) {
      output += "\n" + divider + "\n";
      for (int j = 0; j < boardState.length; j++) {
        String node = " ";
        if (boardState[j][i] == 1) {
          node = "X";
        } else if (boardState[j][i] == 2) {
          node = "O";
        }
        output += "| " + node + " ";
      }
      output += "|";
    }
    output += "\n" + divider2 + "\n" + numberRow + "\n";
    
    return output;
  }
}
