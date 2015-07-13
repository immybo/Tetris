import java.awt.*;
import java.awt.event.*;
import java.lang.Math;
import java.util.ArrayList;

import javax.swing.Timer;

/**
 * GameWindow is an instance of the game, including the actual
 * game area and the GUI. Contains public methods for manipulations of GUI
 * and game area.
 * 
 * Stores values specific to each game instance, including:
 * - If each tile has a block in it
 * - What the difficulty, level and score are
 * 
 * @author Robert Campbell
 *
 */
public class Game {
	/* BUGLIST
	 * 
	 * CURRENT
	 * 
	 * FIXED
	 * - When a block is rotated, it does not check for collision with other blocks after rotation.
	 * - When a block is rotated, it sometimes stops in midair. This is probably due to queueing rotation and falling;
	 * 		i.e. falling to a spot where it can't fall any further before rotating.
	 */


	public static void main(String[] args){
		new Game(1,1);
	}
	
	/**
	 * STATIC
	 */
	// DEBUG MODE
	public static final boolean DEBUG = false;
	
	// The default initial dimensions of the game window
	public static final int GAME_AREA_WIDTH = 480;
	public static final int GAME_AREA_HEIGHT = 700;
	// The default initial dimensions of the GUI
	public static final int GUI_WIDTH = 300;
	public static final int GUI_HEIGHT = GAME_AREA_HEIGHT;
	// The amount of 'tiles' in the game and their sizes
	public static final int TILE_SIZE = 30;
	public static final int HORIZONTAL_TILES = GAME_AREA_WIDTH / TILE_SIZE; // Number of tiles depends on area size and tile size
	public static final int VERTICAL_TILES = GAME_AREA_HEIGHT / TILE_SIZE - 1;
	// The amount of milliseconds between downward movements at level one
	public static final int FALL_DELAY = 250;
	// The millisecond value of the fall delay at a given level is:
	// FALL_DELAY * 1/ e^(level*FALL_DECREASE_MULTIPLIER)
	public static final double FALL_DECREASE_MULTIPLIER = 0.03;
	// The multiplier for fall delay given that the down button is pressed
	public static final double DOWN_BUTTON_MULTIPLIER = 0.1;
	
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
	private GameWindow gameWindow;
	
	// Stores the block in each tile; [0,0] is top left, [max,max] is bottom right
	private Block[][] tileBlocks = new Block[HORIZONTAL_TILES][VERTICAL_TILES];
	private ArrayList<Block> blocks = new ArrayList<Block>();
	private Block currentBlock;
	
	// Whether or not the down button is currently pressed down
	public static boolean isDownButton = false;
	
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
		this.level = initialLevel;
		
		gameWindow = new GameWindow(this);
		

		// Continuously makes block tasks occur on a timer
		blockPerformer = new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				doBlocks();
			}
		};
		blockTimer = new Timer(FALL_DELAY, blockPerformer);
		blockTimer.start();
	}
	
	/**
	 * Restarts the block timer with a new value
	 */
	public void resetBlockTimer(){
		int timeGap = FALL_DELAY * 1/ (int)Math.pow(Math.E, level*FALL_DECREASE_MULTIPLIER);
		if(isDownButton){ timeGap *= DOWN_BUTTON_MULTIPLIER; }
		blockTimer.stop();
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
			int randomBlockNumber = (int)Math.floor(Math.random() * (BLOCK_X_POSITIONS.length));
			int[] newBlockXPositions = new int[BLOCK_X_POSITIONS[randomBlockNumber].length];
			for(int i = 0; i < BLOCK_X_POSITIONS[randomBlockNumber].length; i++){
				newBlockXPositions[i] = BLOCK_X_POSITIONS[randomBlockNumber][i] + (int)Math.floor(HORIZONTAL_TILES/2);
			}
			newBlock(newBlockXPositions, BLOCK_Y_POSITIONS[randomBlockNumber].clone(), 
			BLOCK_ORIGIN_X_POSITIONS[randomBlockNumber] + (int)Math.floor(HORIZONTAL_TILES/2), 
			BLOCK_ORIGIN_Y_POSITIONS[randomBlockNumber], BLOCK_COLORS[randomBlockNumber]);
			// Set the current block to be this block
			currentBlock = blocks.get(blocks.size()-1);
		}
		
		// Check the last created block
		if(!checkValidFall(currentBlock)) {
			// If it can't go down any further, make a new block!
			isMakingNewBlock = true;
			// Nullify the current block
			currentBlock = null;
			return;
		}
		isMakingNewBlock = false;
		
		// Remove it from tileblocks
		for(int i = 0; i < currentBlock.getXPositions().length; i++){
			int delTileX = currentBlock.getXPositions()[i];
			int delTileY = currentBlock.getYPositions()[i];
			tileBlocks[delTileX][delTileY] = null;
		}
		
		// And shift it down if it can be shifted down
		currentBlock.shiftDown();
		
		// Then re-add it to tileblocks
		for(int i = 0; i < currentBlock.getXPositions().length; i++){
			tileBlocks[ currentBlock.getXPositions()[i] ][ currentBlock.getYPositions()[i] ] = currentBlock;
		}
		
		gameWindow.repaint();
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
			if( tileBlocks[checkXPosition][checkYPosition] != null && tileBlocks[checkXPosition][checkYPosition] != block ){
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
		
		// Scroll through tiles and make sure they are null.
		for(int i = 0; i < x.length; i++){
			// If any tile is full, return false
			if(tileBlocks[ x[i] ][ y[i] ] != null){
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
	public void newBlock(int[] x, int[] y, int originX, int originY, Color color){
		// Assert that all tiles are initially empty
		assert areTilesEmpty(x, y) : "Not all tiles are empty when attempting to fill them.";
		
		// Create a new block in the given tiles
		Block newBlock = new Block(x, y, originX, originY, color);
		
		// Set the tiles to contain the block
		for(int i = 0; i < x.length; i++){
			if(DEBUG){
				System.out.println("Adding tile at position [" + x[i] + "," + y[i] + "].");
			}
			tileBlocks[ x[i] ][ y[i] ] = newBlock;
		}
		
		blocks.add(newBlock);
	}
	
	/**
	 * Attempts to empty a given list of tiles; deleting an existing block.
	 * Assumes that at least one of the given tiles is not empty.
	 * 
	 * @param x An array of tile x positions.
	 * @param y An array of tile y positions.
	 */
	public void emptyTiles(int[] x, int[] y){
		assert !areTilesEmpty(x, y) : "All tiles are empty when attempting to empty them.";
		
		// Find the block corresponding to the given tiles if they have one
		for(int i = 0; i < x.length; i++){
			for(int j = 0; j < y.length; j++){
				Block block = tileBlocks[i][j];
				if(block != null){
					// Remove the tile from the block
					block.removeTile(i, j);
					// Remove reference to the block from that specific tile (nullify the tile)
					tileBlocks[i][j] = null;
				}
			}
		}	
	}
	
	/**
	 * Redraws every block
	 */
	public void redraw(){
		gameWindow.repaint();
	}
	
	/**
	 * Returns the block at a specified tile positions (or null if the block doesn't exist).
	 */
	public Block getBlockAtTile(int x, int y){
		if(tileBlocks[x][y] != null){
			return tileBlocks[x][y];
		}
		return null;
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
		if(blocks.size() == 0){ return; }
		if(!isValidHorizontal(isRight,blocks.size()-1)){ return; }
		if(currentBlock == null){ return; }
	
		for(int i = 0; i < currentBlock.getXPositions().length; i++){
			int delTileX = currentBlock.getXPositions()[i];
			int delTileY = currentBlock.getYPositions()[i];
			tileBlocks[delTileX][delTileY] = null;
		}
	
		if(isRight){
			currentBlock.shiftRight();
		}
		else{
			currentBlock.shiftLeft();
		}
	
		// Then re-add it to tileblocks
		refreshTileBlocks();
	
		gameWindow.repaint();
	}
	
	/**
	 * Refreshes where to draw blocks, must be called before redrawing
	 */
	private void refreshTileBlocks(){
		tileBlocks = new Block[HORIZONTAL_TILES][VERTICAL_TILES];
		for(int i = 0; i < blocks.size(); i++){
			for(int j = 0; j < blocks.get(i).getXPositions().length; j++){
				tileBlocks[ blocks.get(i).getXPositions()[j] ][ blocks.get(i).getYPositions()[j] ] = blocks.get(i);
			}
		}
	}	
	
	/**
	 * Checks to see if a block can move horizontally
	 * @param isRight True if you're checking to the right, false if not
	 * @param i The index of the block to use in the blocks array
	 */
	private boolean isValidHorizontal(boolean isRight, int i){
		// Check that there is no block to the right/left of it (otherwise don't shift)
		// For every point in the block, check to see if it's on the right/left of the screen
		for(int k = 0; k < blocks.get(i).getXPositions().length; k++){
			int checkXPosition;
			if(isRight){ checkXPosition = blocks.get(i).getXPositions()[k] + 1; }
			else       { checkXPosition = blocks.get(i).getXPositions()[k] - 1; }
			int checkYPosition = blocks.get(i).getYPositions()[k];
			if(checkXPosition >= HORIZONTAL_TILES || checkXPosition < 0){
				return false;
			}
			if( tileBlocks[checkXPosition][checkYPosition] != null && tileBlocks[checkXPosition][checkYPosition] != blocks.get(i) ){
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
		refreshTileBlocks();
	}
	
	/**
	 * Checks to see whether the currently selected piece can turn by 90 degrees in the specified direction
	 * @param isClockwise Direction of rotation (true for clockwise)
	 * @return True if the piece 
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
			// 3. Other blocks
			for(Block b: blocks){
				// Obviously we want to skip looking at the block we're evaluating
				if(b == currentBlock){ continue; }
				
				// Go through every tile contained within the other block
				for(int j = 0; j < b.getXPositions().length; j++){
					if(testX == b.getXPositions()[j] && testY == b.getYPositions()[j]){
						return false;
					}
				}
			}
		}
		return true;
	}
}