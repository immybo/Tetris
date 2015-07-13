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
	}
}
