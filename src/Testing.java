import java.awt.Color;

/**
 * Code for testing the tetris game. Call Testing.runTests() to run all tests.
 *
 * @author Robert Campbell
 *
 */
public class Testing {
	/**
	 * Runs all tests for the tetris game.
	 */
	public static void runTests(){
		testGame();
		testBlock();
		System.out.println("All tests completed.");
	}

	/**
	 * Tests the game window class by itself
	 */
	public static void testGame(){
		// Create a new Game object
		Game w = new Game(1, 1);

		// Set various parameters in the Game and read them
		w.setScore(1000);
		assert w.getScore() == 1000 : "GUI score setting was not handled appropriately.";
		w.setDifficulty(1);
		assert w.getDifficulty() == 1 : "GUI difficulty setting was not handled appropriately.";
		w.setLevel(100);
		assert w.getLevel() == 100 : "GUI level setting was not handled appropriately.";

		// Increment and decrement score and read them
		w.incrementScore(10);
		assert w.getScore() == 1010 : "GUI score was not incremented appropriately.";
		w.incrementScore(-20);
		assert w.getScore() == 990 : "GUI score was not decremented appropriately.";
		w.incrementScore(-1000);
		// Score should reset to 0 if decremented below 0
		assert w.getScore() == 0 : "GUI score was not decremented below 0 appropriately.";

		// Get some tiles to make sure they're not filled
		assert w.getBlockAtTile(Game.HORIZONTAL_TILES-1, Game.VERTICAL_TILES-1) == null : "Game area was not created blank at [max,max].";
		assert w.getBlockAtTile(0, 0) == null : "Game area was not created blank at [0,0].";
		assert w.getBlockAtTile(Game.HORIZONTAL_TILES-1, 0) == null : "Game area was not created blank at [max,0].";
		assert w.getBlockAtTile(0, Game.VERTICAL_TILES-1) == null : "Game area was not created blank at [0,max].";

		// Fill some tiles and make sure they're filled
		int[] fillX = { 0, 0, Game.HORIZONTAL_TILES-1, Game.HORIZONTAL_TILES-1 };
		int[] fillY = { 0, Game.VERTICAL_TILES-1, Game.VERTICAL_TILES-1, 0 };
		w.newBlock(fillX, fillY, 0, 0, Color.BLACK);

		assert w.getBlockAtTile(0,0) != null : "Tile was not created properly at [0,0].";
		assert w.getBlockAtTile(0, Game.VERTICAL_TILES-1) != null : "Tile was not created properly at [0,max].";
		assert w.getBlockAtTile(Game.HORIZONTAL_TILES-1, 0) != null : "Tile was not created properly at [max,0].";
		assert w.getBlockAtTile(Game.HORIZONTAL_TILES-1, Game.VERTICAL_TILES-1) != null : "Tile was not created properly at [max,max].";

		// Empty the tiles and make sure they're empty
		w.emptyTiles(fillX, fillY);

		assert w.getBlockAtTile(0,0) == null : "Tile was not emptied properly at [0,0].";
		assert w.getBlockAtTile(0, Game.VERTICAL_TILES-1) == null : "Tile was not emptied properly at [0,max].";
		assert w.getBlockAtTile(Game.HORIZONTAL_TILES-1, 0) == null : "Tile was not emptied properly at [max,0].";
		assert w.getBlockAtTile(Game.HORIZONTAL_TILES-1, Game.VERTICAL_TILES-1) == null : "Tile was not emptied properly at [max,max].";

		// Test tetris checking
		int[] lineX = new int[Game.HORIZONTAL_TILES];
		int[] lineY = new int[Game.HORIZONTAL_TILES];
		for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
			lineX[i] = i;
		}
		w.newBlock(lineX, lineY, (int)Game.HORIZONTAL_TILES/2, 0, Color.BLACK);

		for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
			assert w.getBlockAtTile(i, 0) != null : "Block was not created properly along the top row.";
		}
		w.checkForTetris();

		// Check for non-empty tiles - after checking for the tetris, there should be no filled tiles anywhere
		for(int i = 0; i < Game.VERTICAL_TILES; i++){
			for(int j = 0; j < Game.HORIZONTAL_TILES; j++){
				assert w.getBlockAtTile(i, j) == null : "Block was erroneously present at (" + i + "," + j + ") after checking for and removing a tetris in line 0.";
			}
		}

		// Make sure that blocks drop down properly after deleting a tetris

		// Create a line of full tiles on the bottom row
		lineY = new int[Game.HORIZONTAL_TILES];
		for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
			lineY[i] = Game.VERTICAL_TILES - 1;
		}
		w.newBlock(lineX, lineY, (int)Game.HORIZONTAL_TILES/2, Game.VERTICAL_TILES-1, Color.BLACK);

		// Create a block above the line of full tiles
		int[] blockY = {Game.VERTICAL_TILES-2, Game.VERTICAL_TILES-2, Game.VERTICAL_TILES-3, Game.VERTICAL_TILES-3};
		int[] blockX = {5, 6, 5, 6};
		w.newBlock(blockX, blockY, Game.VERTICAL_TILES-2, 5, Color.BLACK);

		for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
			assert w.getBlockAtTile(i, Game.VERTICAL_TILES-1) != null : "Block was not created properly along the bottom row.";
		}
		for(int i = 0; i < 4; i++){
			assert w.getBlockAtTile(blockX[i], blockY[i]) != null : "Block was not created properly above the bottom row.";
		}

		// Remove the tetris
		w.checkForTetris();

		// Check to make sure that the block has dropped down properly
		for(int i = 0; i < 4; i++){
			assert w.getBlockAtTile(blockX[i], blockY[i]+1) != null : "Tetris did not result in appropriate relocation of block from ("+blockX[i]+","+blockY[i]+") down one tile.";
		}
		for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
			for(int j = 0; j < Game.VERTICAL_TILES; j++){
				for(int k = 0; k < 4; k++){
					if((i == blockX[k]) && (j == blockY[k])){
						break;
					}
					assert w.getBlockAtTile(i,j) == null : "Tetris erroneously resulted in a block at ("+i+","+j+").";
				}
			}
		}
	}

	/**
	 * Tests the various methods of the block class
	 */
	public static void testBlock(){
		// Construct a new block
		int[] posArray = {1, 2, 3};
		Block b = new Block(posArray, posArray, 2, 2, Color.BLACK);

		// Assert the initial values of the block
		assert b.getColor() == Color.BLACK : "Block.getColor did not return matching color.";
		assert b.getXPositions() == posArray : "Block.getXPositions did not return matching array.";
		assert b.getYPositions() == posArray : "Block.getYPositions did not return matching array.";
		assert b.getOriginX() == 2 : "Block.getOriginX did not return matching value.";
		assert b.getOriginY() == 2 : "Block.getOriginY did not return matching value.";

		// Test that the block turns correctly
		b.turn(true); // Turn the block clockwise
		int[] turnedX = {3, 2, 1};
		int[] turnedY = {1, 2, 3};
		assert b.getXPositions() == turnedX : "Block did not turn 90 degrees clockwise correctly; non-matching x positions.";
		assert b.getYPositions() == turnedY : "Block did not turn 90 degrees clockwise correctly; non-matching y positions.";

		b.turn(false);
		assert b.getXPositions() == posArray : "Block did not turn 90 degrees anticlockwise correctly; non-matching x positions.";
		assert b.getYPositions() == posArray : "Block did not turn 90 degrees anticlockwise correctly; non-matching y positions.";

		// Attempt to remove and then re-add a tile
		b.removeTile(3,3);
		assert b.getXPositions().length == 2 : "Block did not remove tile correctly; resulting array was inappropriate length.";
		b.addTile(3,3);
		assert b.getXPositions().length == 3 : "Block did not re-add tile correctly; resulting array was inappropriate length.";
		assert b.getXPositions()[2] == 3 : "Block did not re-add tile correctly; resulting array did not contain new tile's x position.";
		assert b.getYPositions()[2] == 3 : "Block did not re-add tile correctly; resulting array did not contain new tile's y position.";

		// Attempt to shift the block around in all directions
		b.shiftRight();
		for(int i = 0; i < 3; i++){
			assert b.getXPositions()[i] == posArray[i]+1 : "Block did not shift right correctly; x positions were not incremented correctly.";
			assert b.getYPositions()[i] == posArray[i] : "Block did not shift right correctly; y positions did not remain static.";
		}
		b.shiftLeft();
		assert b.getXPositions() == posArray : "Block did not shift left correctly; x positions were not decremented correctly.";
		assert b.getYPositions() == posArray : "Block did not shift left correctly; y positions did not remain static.";
		b.shiftDown();
		for(int i = 0; i < 3; i++){
			assert b.getXPositions()[i] == posArray[i] : "Block did not shift down correctly; x positions did not remain static.";
			assert b.getYPositions()[i] == posArray[i]+1 : "Block did not shift down correctly; y positions were not incremented correctly.";
		}

		// Attempt to shift a tile down
		b.shiftTileDown(3,4);
		assert b.getXPositions()[2] == 3 : "Block did not shift tile down correctly; x position did not remain static.";
		assert b.getYPositions()[2] == 5 : "Block did not shift tile down correctly; y position was not correctly incremented.";

	}
}
