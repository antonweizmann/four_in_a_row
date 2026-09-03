from board import *
from app import *
from players import *
from heuristics import *

if __name__ == "__main__":
	testB = Board(5, 5)
	print(testB)
	gameT = start_game(3, testB, )
