package NRow;

import NRow.Players.PlayerController;

public class Game {
  private int gameN;
  private PlayerController[] players;
  private Board gameBoard;
  private int winner;

  /**
   * Create a new game
   * @param gameN N in a row required to win
   * @param boardWidth Width of the board
   * @param boardHeight Height of the board
   * @param players List of players
   */
  Game(int gameN, int boardWidth, int boardHeight, PlayerController[] players) {
    assert (boardWidth % 2 != 0) : "Board width must be odd!";
    this.gameN = gameN;
    this.players = players;
    this.gameBoard = new Board(boardWidth, boardHeight);
  }

  /**
   * Starts the game
   * @return the playerId of the winner
   */
  public int startGame() {
    System.out.println("Start game!");
    int currentPlayer = 0;

    while (!this.isOver()) {
      // turn player can make a move
      gameBoard.play(players[currentPlayer].makeMove(gameBoard), players[currentPlayer].playerId);
      
      // other player can make a move now
      currentPlayer = (currentPlayer == 0) ? 1 : 0;
    }


    System.out.println(gameBoard);
    if(winner < 0) {
      System.out.println("Game is a draw!");
    } else {
      System.out.println("Player " + players[winner - 1] + " won!");
    }
    System.out.println("Player " + players[0] + " evaluated a boardstate " + players[0].getEvalCount() + " times.");
    System.out.println("Player " + players[1] + " evaluated a boardstate " + players[1].getEvalCount() + " times.");
    return winner;
  }

  /**
   * Determine whether the game is over
   * @return true if game is over
   */
  public boolean isOver() {
    winner = winning(gameBoard.getBoardState(), this.gameN);
    return winner != 0;
  }

  /**
   * Determines whether a player has won, and if so, which one
   * @param board the board to check
   * @param gameN N in a row required to win
   * @return 1 or 2 if the respective player won, or 0 if neither player has won
   */
  public static int winning(int[][] board, int gameN) {
    int player;

    // Vertical Check
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < (board[i].length - (gameN - 1)); j++) {
        if (board[i][j] != 0) {
          player = board[i][j];
          for (int x = 1; x < gameN; x++) {
            if (board[i][j + x] != player) {
              player = 0;
              break;
            }
          }
          if (player != 0) {
            return player;
          }
        }
      }
    }
    
    // Horizontal Check
    for (int i = 0; i < (board.length - (gameN - 1)); i++) {
      for (int j = 0; j < board[i].length; j++) {
        if (board[i][j] != 0) {
          player = board[i][j];
          for (int x = 1; x < gameN; x++) {
            if (board[i + x][j] != player) {
              player = 0;
              break;
            }
          }
          if (player != 0) {
            return player;
          }
        }
      }
    }
    
    // Ascending Diagonal Check
    for (int i = 0; i < (board.length - (gameN - 1)); i++) {
      for (int j = board[i].length - 1; j >= gameN - 1; j--) {
        if (board[i][j] != 0) {
          player = board[i][j];
          for (int x = 1; x < gameN; x++) {
            if (board[i + x][j - x] != player) {
              player = 0;
              break;
            }
          }
          if (player != 0) {
            return player;
          }
        }
      }
    }
    
    // Descending Diagonal Check
    for (int i = 0; i < (board.length - (gameN - 1)); i++) {
      for (int j = 0; j < (board[i].length - (gameN - 1)); j++) {
        if (board[i][j] != 0) {
          player = board[i][j];
          for (int x = 1; x < gameN; x++) {
            if (board[i + x][j + x] != player) {
              player = 0;
              break;
            }
          }
          if (player != 0) {
            return player;
          }
        }
      }
    }

    // Check for a draw (full board)
    for (int i = 0; i < board.length; i++) {
      if(board[i][0] == 0) {
        return 0; // board is not full, game not over
      }
    }
    
    return -1; // Game is a draw
  }

}
