from heuristics import Heuristic, SimpleHeuristic
from players import PlayerController, HumanPlayer, MinMaxPlayer, AlphaBetaPlayer
from typing import List
import numpy as np


class Board:
    """A n in a row board
    """
    def __init__(self, *args) -> None:
        """Constructor for the Board class

        *args is one of three things:
        - Two integers representing the width and height of the board respectively
            An empty board is created with those dimensions
        - Another board object
            A copy of the board object is created
        - A board state
            A new board object is created with the provided board state

        Raises:
            TypeError: if none of the above mentioned formats are followed
        """
        # Initialising the attributes
        self.width: int
        self.height: int
        self.board_state: np.ndarray
        
        # Creates an empty board with the provided dimensions
        if len(args) == 2:
            assert isinstance(args[0], int) and isinstance(args[1], int)
            self.width, self.height = args
            self.board_state = np.full(args, 0, dtype=int)
        
        # Creates a copy of the provided board
        elif len(args) == 1 and isinstance(args[0], self.__class__):
            other: 'Board' = args[0]          
            self.width = other.width
            self.height = other.height
            self.board_state = other.get_board_state()

        # Creates a new board with the provided board state
        elif len(args) == 1 and isinstance(args[0], np.ndarray):
            state: np.ndarray = args[0]
            self.width = len(state)
            self.height = len(state[0])
            self.board_state = state

        # Raise an error if the parameters don't follow any of the correct formats
        else:
            raise TypeError('Board constructor has received a wrong type as parameter')

    
    def get_value(self, col: int, row: int) -> int:
        """Retrieves the value of a field in the board

        Args:
            col (int): column of the requested field
            row (int): row of the requested field

        Returns:
            int: value of the requested field
        """
        return self.board_state[col, row]


    def get_board_state(self) -> np.ndarray:
        """
        Returns:
            np.ndarray: copy of the board state
        """
        return self.board_state.copy()
    
    
    def play(self, col: int, player_id: int) -> bool:
        """Let player playerId make a move in column 'col'

        Args:
            col (int): column of the action
            player_id (int): player that takes the action

        Returns:
            bool: true if succeeded
        """
        for i, field in enumerate(self.board_state[col][::-1]):
            if field == 0:
                self.board_state[col, self.height - i - 1] = player_id
                return True
        return False
    

    def is_valid(self, col: int) -> bool:
        """Returns if a move is valid

        Args:
            col (int): column of the action

        Returns:
            bool: true if spot is not taken yet
        """
        return self.board_state[col, 0] == 0
    

    def get_new_board(self, col: int, player_id: int) -> 'Board':
        """Gets a new board given a player and their action

        Args:
            col (int): column of the action
            player_id (int): player that takes the action

        Returns:
            Board: a *new* Board object with the resulting state
        """
        state: np.ndarray = self.get_board_state()
        for i, field in enumerate(state[col][::-1]):
            if field == 0:
                state[col, self.height - i - 1] = player_id
                return Board(state)
            
        return Board(state)
    

    def __str__(self) -> str:
        """
        Returns:
            str: a human readable representation of the board
        """
        divider: str = ' '
        divider2: str = ' '
        number_row: str = '|'

        for i in range(self.width):
            divider += '--- '
            divider2 += '=== '
            number_row += f' {i + 1} |'

        output: str = ''

        for i in range(self.height):
            output += f'\n{divider}\n'
            for j in range(self.width):
                node: str = ' '
                if self.board_state[j, i] == 1:
                    node = 'X'
                elif self.board_state[j, i] == 2:
                    node = 'O'

                output += f'| {node} '
            output += '|'
        output += f'\n{divider2}\n{number_row}\n'
        
        return output

