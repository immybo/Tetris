import java.awt.*;
import java.awt.event.*;
import java.lang.Math;
import java.util.*;
import java.io.*;

import javax.swing.Timer;

/**
 * Handles the top level functions of a game of tetris.
 *
 * @author Robert Campbell
 *
 */
public class Game {
	// As a preface to anybody reading this code,
	// A full line is not actually called a tetris.
	// It's just called a full line.
	// However, I thought it was, and I've left it
	// that way in this code.
	// (A tetris is actually 4 lines at once.)

	/**
	 * STATIC
	 */

	// The default initial dimensions of the game window
	public static final int GAME_AREA_WIDTH = 360;
	public static final int GAME_AREA_HEIGHT = 720;
	// The default initial dimensions of the GUI
	public static final int GUI_WIDTH = 300;
	public static final int GUI_HEIGHT = GAME_AREA_HEIGHT;
	// The amount of 'tiles' in the game and their sizes
	public static final int TILE_SIZE = 30;
	public static final int HORIZONTAL_TILES = GAME_AREA_WIDTH / TILE_SIZE; // Number of tiles depends on area size and tile size
	public static final int VERTICAL_TILES = GAME_AREA_HEIGHT / TILE_SIZE;
	// The amount of milliseconds between downward movements at level one
	public static final int FALL_DELAY = 500;
	// The millisecond value of the fall delay at a given level is:
	// FALL_DELAY * 1/ e^(level*FALL_DECREASE_MULTIPLIER)
	public static final double FALL_DECREASE_MULTIPLIER = 0.01;
	// The multiplier for fall delay given that the down button is pressed
	public static final double DOWN_BUTTON_MULTIPLIER = 0.1;
	// The base score gained from getting a tetris
	public static final double TETRIS_SCORE = 1000;
	// The score multipliers for getting 1, 2, 3 or 4 tetrises at once, respectively
	public static final double[] TETRIS_MULTIPLIERS = { 1, 2, 4, 7 };

	// Possible block initial positions (with half the width added on to the x positions)
	public static final int[][] BLOCK_X_POSITIONS = {
		{ -1,  0,  1,  2 },
		{ -1,  0,  1,  1 },
		{ -1, -1,  0,  1 },
		{  0,  0,  1,  1 },
		{ -1,  0,  0,  1 },
		{ -1,  0,  0,  1 },
		{ -1,  0,  0,  1 }
	};
	public static final int[][] BLOCK_Y_POSITIONS = {
		{  0,  0,  0,  0 },
		{  0,  0,  0,  1 },
		{  0,  1,  0,  0 },
		{  0,  1,  0,  1 },
		{  1,  0,  1,  0 },
		{  0,  0,  1,  0 },
		{  0,  0,  1,  1 }
	};
	public static final int[] BLOCK_ORIGIN_X_POSITIONS = { 0, 1, -1, 0, 0, 0, 0 };
	public static final int[] BLOCK_ORIGIN_Y_POSITIONS = { 0, 0,  0, 1, 1, 1, 0 };

	// Possible colors for blocks (in order, each color corresponds to a block shape)
	public static final Color[] BLOCK_COLORS = { new Color(150,0,0), new Color(150,150,0),  new Color(100,0,100), new Color(0,0,150), new Color(32,178,170), new Color(34,139,34), new Color(150,70,0)};

	/**
	 * FIELDS
	 */

	private int difficulty;
	private int level;
	private double score = 0;
	private GameScreen gameWindow;

	// Stores the type of block in each tile; [0,0] is top left, [max,max] is bottom right
	// We need 2 tiles above to store blocks as they spawn above the top of the visible area
	private int[][] tiles = new int[HORIZONTAL_TILES][VERTICAL_TILES];
	private Block currentBlock;
	
	// The next 7 blocks to drop
	private LinkedList<Integer> nextBlocks = new LinkedList<Integer>();

	// Whether or not the down button is currently pressed down
	public boolean isDownButton = false;

	// Whether or not a new block should be made in the next frame
	private boolean isMakingNewBlock = true;

	// The listener which calls block actions periodically
	private ActionListener blockPerformer;
	// The timer which is used for block actions
	private Timer blockTimer;

	/**
	 * Constructor; creates a new instance of the game on the given difficulty and the given initial level.
	 *
	 * @param difficulty The difficulty for the game instance.
	 * @param initialLevel The level on which the game instance will start,
	 */
	public Game(int difficulty, int initialLevel){
		this.difficulty = difficulty;
		this.level = initialLevel - 1;

		gameWindow = new GameScreen(this);

		// Continuously makes block tasks occur on a timer
		blockPerformer = new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				doBlocks();
			}
		};
		resetBlockTimer();
	}

	/**
	 * Restarts the block timer with a new value
	 */
	public void resetBlockTimer(){
		int timeGap = FALL_DELAY * 1/ (int)Math.pow(Math.E, level*FALL_DECREASE_MULTIPLIER) / difficulty;
		if(isDownButton){ timeGap *= DOWN_BUTTON_MULTIPLIER; }
		if(blockTimer != null){ blockTimer.stop(); }
		blockTimer = new Timer(timeGap, blockPerformer);
		blockTimer.start();
	}

	/**
	 * Asserts that the given lists of tiles:
	 * - Have equal length, and
	 * - Are within the bounds of the game window
	 *
	 * @param x
	 * @param y
	 */
	private void assertValidTiles(int[] x, int[] y){
		// Check to see that the given arrays are the same size
		assert x.length == y.length : "Tile position lists not equal length.";
		// Check to see that the array values are within bounds
		for(int i = 0; i < x.length; i++){
			assert(x[i] < HORIZONTAL_TILES) : "Tile horizontal position out of bounds.";
			assert(y[i] < VERTICAL_TILES) : "Tile vertical position out of bounds.";
		}
	}

	private void doBlocks(){
		// Make a new block if necessary
		if(isMakingNewBlock){
			// Check if there are no remaining blocks in the queue
			if(nextBlocks.isEmpty()){
				// Set up a new randomised list of 0-6
				
				// Build a list of numbers from 0-6 (ordered)
				ArrayList<Integer> nums = new ArrayList<Integer>();
				for(int i = 0; i < 7; i++) nums.add(i);
				
				for(int i = 0; i < 7; i++){
					// Get a random index
					int index = (int)(Math.random()*(7-i));
					// And insert this index from the numbers into the random list
					nextBlocks.push(nums.get(index));
					nums.remove(index);
				}
			}
			
			level++;
			int randomBlockNumber = nextBlocks.pop();

			// Generate a new array for the x positions of the new block
			int[] newBlockXPositions = new int[BLOCK_X_POSITIONS[randomBlockNumber].length];
			// And one for the y positions
			int[] newBlockYPositions = new int[BLOCK_Y_POSITIONS[randomBlockNumber].length];

			// Because the new block must appear in the center of the area, and BLOCK_X_POSITIONS gives values
			// relative to this center, add half of the width of the area to every value in the array.
			// We also have to subtract some from the y positions to make the block initially above the screen
			for(int i = 0; i < BLOCK_X_POSITIONS[randomBlockNumber].length; i++){
				newBlockXPositions[i] = BLOCK_X_POSITIONS[randomBlockNumber][i] + (int)Math.floor(HORIZONTAL_TILES/2);
				newBlockYPositions[i] = BLOCK_Y_POSITIONS[randomBlockNumber][i] - 2;
			}

			// Make the new block
			newBlock(
						// x positions come from the array we just generated
						newBlockXPositions,
						// y positions also come from an array we just generated
						newBlockYPositions,
						// the origin x and y positions have static arrays
						BLOCK_ORIGIN_X_POSITIONS[randomBlockNumber] + (int)Math.floor(HORIZONTAL_TILES/2),
						BLOCK_ORIGIN_Y_POSITIONS[randomBlockNumber],
						// and the block type comes from our random number incremented (as 0 indicates a tile without a block)
						randomBlockNumber+1
					);
			// Wait until the next iteration to move the block
			redraw();
			isMakingNewBlock = false;
			return;
		}

		// Check the last created block
		if(!checkValidFall(currentBlock)) {
			// If it can't go down any further, make a new block!
			isMakingNewBlock = true;



			// Place the current block into the tile area
			for(int i = 0; i < currentBlock.getXPositions().length; i++){
				// If it's above the top edge of the map, then tiles must be filled to the top and the player has lost
				if(currentBlock.getYPositions()[i] < 0){
					loseGame();
					return;
				}
				tiles[ currentBlock.getXPositions()[i] ][ currentBlock.getYPositions()[i] ] = currentBlock.getBlockType();
			}
			// Nullify the current block
			currentBlock = null;
			// And check for any new tetrises
			checkForTetris();
			redraw();
		}
		else{
			isMakingNewBlock = false;

			// Shift it down if it can be shifted down
			currentBlock.shiftDown();
			redraw();
		}
		
		gameWindow.doKeys();
	}

	/**
	 * Ends the game, with the player losing. Returns to the main screen.
	 */
	public void loseGame(){
		// First, check the players score against the highscores list and edit the highscores list if necessary
		try{
			// Put the current highscores into an array
			Scanner s = new Scanner(new File("highscores.txt"));
			double[] highscores = new double[10];
			int i = 0;
			while(s.hasNextDouble()){
				highscores[i] = s.nextDouble();
				i++;
			}
			s.close();

			// Then, find the index in this array where the highscore is
			int highscorePlace = -1;
			for(int j = 0; j < 10; j++){
				if(score > highscores[j]){
					highscorePlace = j;
					break;
				}
			}

			// Only edit the highscores list if the current score is greater than one of the highscores
			if(highscorePlace != -1){
				// Move all of the highscores at and below that place down by one (discarding the last one)
				for(int j = 8; j >= highscorePlace; j--){
					highscores[j+1] = highscores[j];
				}
				// And insert the current score at the specified place
				highscores[highscorePlace] = score;

				// Then, write the new highscores to the highscores list
				PrintStream p = new PrintStream(new File("highscores.txt"));

				for(int j = 0; j < 10; j++){
					p.print(highscores[j] + " ");
				}

				p.close();
			}
		}
		catch(IOException e){
			System.out.println("Could not read from OR write to highscores file. " + e);
		}
		finally{
			blockTimer.stop();
			gameWindow.dispose();
		}
	}

	/**
	 * Checks the gameplay area for any tetrises (full lines).
	 * Removes the lines and lowers all blocks above it if any are found.
	 */
	public void checkForTetris(){
		int numTetrises = 0;
		// Scroll through all rows in the gameplay area
		outerloop:
		for(int i = Game.VERTICAL_TILES-1; i >= 0; i--){
			// Scroll through all tiles in the row, continuing the outer loop if one isn't full
			for(int j = 0; j < Game.HORIZONTAL_TILES; j++){
				if(tiles[j][i] == 0){
					continue outerloop;
				}
			}
			// If all tiles are occupied,
			// Remove all tiles on the row and remove the references to them in their respective blocks
			for(int j = 0; j < Game.HORIZONTAL_TILES; j++){
				tiles[j][i] = 0;
			}
			// Drop all tiles above them down by one
			shiftTilesDown(i);
			// Increment the number of tetrises gotten this turn
			numTetrises++;
			// And start from the beginning again
			i = Game.VERTICAL_TILES;
			// Check for infinite loops
			if(numTetrises > Game.VERTICAL_TILES){
				throw new IllegalStateException("Infinite (or very long) loop encountered when checking for tetrises.");
			}
		}

		// 4 will be the maximum multiplier. I don't think you can get more? This will prevent arrayoutofbounds if you can.
		if(numTetrises > 4){ numTetrises = 4; }
		// Add to the score:
		// The number of tetrises gained * the score for a single tetris * the multiplier for that amount of tetrises * the log of the level+1, rounded down to a multiple of 10
		int tempScore = (int)(numTetrises * TETRIS_SCORE * TETRIS_MULTIPLIERS[numTetrises] * (Math.log(level)+1) / 10);
		score += tempScore*10;
	}

	/**
	 * Shifts all tiles above a given line down by one
	 * @param y The line above which to shift all tiles down
	 */
	private void shiftTilesDown(int y){
		// Scroll through the rows in reverse order (up vertically) so as not to override rows which we drop down in future
		for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
			for(int j = y-1; j >= 0; j--){
				// Do nothing to tiles which don't have anything on them
				if(tiles[i][j] != 0){
					tiles[i][j+1] = tiles[i][j];
					tiles[i][j] = 0;
				}
			}
		}
	}

	/**
	 * Checks to see if a given block is able to fall
	 * @param block The block to be checked
	 * @return Whether or not the fall is valid (true for valid)
	 */
	private boolean checkValidFall(Block block){
		// Check that there is no block below it (otherwise don't shift down)
		// For every point in the block,
		for(int k = 0; k < block.getXPositions().length; k++){
			int checkXPosition = block.getXPositions()[k];
			int checkYPosition = block.getYPositions()[k] + 1;
			if(checkYPosition >= VERTICAL_TILES){
				return false;
			}
			// If this point is above the screen, it must be able to fall
			if(checkYPosition < 0){
				continue;
			}
			if( tiles[checkXPosition][checkYPosition] != 0 && !block.containsPos(checkXPosition,checkYPosition) ){
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks to see if a given list of tiles is completely empty.
	 * @param x An array of tile x positions.
	 * @param y An array of tile y positions.
	 * x and y must be of equal lengths.
	 * @return If any tile contains part of a block, returns false. Otherwise, returns true.
	 */
	public boolean areTilesEmpty(int[] x, int[] y){
		assertValidTiles(x, y);

		// Scroll through tiles and make sure they are 0.
		for(int i = 0; i < x.length; i++){
			// If any tile is full, return false
			if(tiles[ x[i] ][ y[i] ] != 0){
				return false;
			}
		}
		// Every tile must be empty.
		return true;
	}

	/**
	 * Attempts to fill a given list of tiles; creating a new block.
	 * Assumes that all of the given tiles are empty.
	 *
	 * @param x An array of tile x positions.
	 * @param y An array of tile y positions.
	 * @param originX The central x position.
	 * @param originY The central y position.
	 * @param color   The color of the block.
	 */
	public void newBlock(int[] x, int[] y, int originX, int originY, int blockType){
		// Assert that all tiles are initially empty
		assert areTilesEmpty(x, y) : "Not all tiles are empty when attempting to fill them.";

		// Create a new block in the given tiles
		currentBlock = new Block(x, y, originX, originY, blockType);
	}

	/**
	 * Returns the current block
	 */
	public Block getCurrentBlock(){
		return currentBlock;
	}

	/**
	 * Attempts to empty a given list of tiles.
	 *
	 * @param x An array of tile x positions.
	 * @param y An array of tile y positions.
	 */
	public void emptyTiles(int[] x, int[] y){
		// Set all of the given tiles to 0
		for(int i = 0; i < x.length; i++){
			for(int j = 0; j < y.length; j++){
				tiles[i][j] = 0;
			}
		}
	}

	/**
	 * Gets the value of a certain tile
	 */
	public int getTileValue(int x, int y){
		return tiles[x][y];
	}

	/**
	 * Redraws the game
	 */
	public void redraw(){
		gameWindow.redraw();
	}

	/**
	 * Increments the score by the given amount.
	 * Sets the score to 0 if it would go below 0.
	 */
	public void incrementScore(double amount){
		if(amount > score){
			score = 0;
		}
		else{
			score += amount;
		}
	}
	/**
	 * Returns the score value
	 */
	public double getScore(){
		return score;
	}
	/**
	 * Sets the score to a given number
	 */
	public void setScore(double newScore){
		score = newScore;
	}

	/**
	 * Returns the difficulty value
	 */
	public int getDifficulty(){
		return difficulty;
	}
	/**
	 * Sets the difficulty to a given number
	 */
	public void setDifficulty(int newDifficulty){
		difficulty = newDifficulty;
	}

	/**
	 * Returns the current level
	 */
	public int getLevel(){
		return level;
	}
	/**
	 * Sets the level to a given number
	 */
	public void setLevel(int newLevel){
		level = newLevel;
	}

	/**
	 * Moves the last block horizontally one tile
	 */
	public void moveHorizontally(boolean isRight){
		if(currentBlock == null){ return; }
		if(!isValidHorizontal(isRight)){ return; }

		if(isRight){
			currentBlock.shiftRight();
		}
		else{
			currentBlock.shiftLeft();
		}
		redraw();
	}

	/**
	 * Checks to see if the current block can move horizontally
	 * @param isRight True if you're checking to the right, false if not
	 */
	private boolean isValidHorizontal(boolean isRight){
		// Check that there is no block to the right/left of it (otherwise don't shift)
		// For every point in the block, check to see if it's on the right/left of the screen
		for(int k = 0; k < currentBlock.getXPositions().length; k++){
			int checkXPosition;
			if(isRight){ checkXPosition = currentBlock.getXPositions()[k] + 1; }
			else       { checkXPosition = currentBlock.getXPositions()[k] - 1; }
			int checkYPosition = currentBlock.getYPositions()[k];
			if(checkXPosition >= HORIZONTAL_TILES || checkXPosition < 0){
				return false;
			}
			// If it's above the screen and not outside the boundaries, it must be able to move horizontally
			if(checkYPosition < 0){
				continue;
			}
			if( tiles[checkXPosition][checkYPosition] != 0 && !currentBlock.containsPos(checkXPosition, checkYPosition) ){
				return false;
			}
		}
		return true;
	}

	/**
	 * Causes the current block to move down much faster, until haltRushDown() is called.
	 */
	public void rushDown(){
		isDownButton = true;
		resetBlockTimer();
	}
	/**
	 * Causes the current block to halt moving down much faster.
	 */
	public void haltRushDown(){
		isDownButton = false;
		resetBlockTimer();
	}

	/**
	 * Turns the currently selected piece by 90 degrees *if possible* (otherwise does nothing)
	 * @param isClockwise Direction of rotation (true for clockwise)
	 */
	public void turnCurrentPiece(boolean isClockwise){
		if(!pieceCanTurn(isClockwise)){ return; }
		if(currentBlock == null){ return; }
		currentBlock.turn(isClockwise);
		redraw();
	}

	/**
	 * Checks to see whether the currently selected block can turn by 90 degrees in the specified direction
	 * @param isClockwise Direction of rotation (true for clockwise)
	 * @return Whether the block can turn or not
	 */
	private boolean pieceCanTurn(boolean isClockwise){
		if(currentBlock == null){ return false; }

		for(int i = 0; i < currentBlock.getXPositions().length; i++){
			int testY;
			int testX;
			// For each tile contained within the block, get its distance from the origin
			int xDist = currentBlock.getXPositions()[i] - currentBlock.getOriginX();
			int yDist = currentBlock.getYPositions()[i] - currentBlock.getOriginY();

			// Find out where the new tile would be located
			if(isClockwise){
				testX = currentBlock.getOriginX() - yDist;
				testY = currentBlock.getOriginY() + xDist;
			}
			else{
				testX = currentBlock.getOriginX() + yDist;
				testY = currentBlock.getOriginY() - xDist;
			}

			// Check the tile against
			// 1. Vertical game area boundaries
			if(testY < 0 || testY >= Game.VERTICAL_TILES){
				return false;
			}
			// 2. Horizontal game area boundaries
			if(testX < 0 || testX >= Game.HORIZONTAL_TILES){
				return false;
			}
			// 3. Other tiles
			if(tiles[testX][testY] != 0){ return false; }
		}
		return true;
	}
}
