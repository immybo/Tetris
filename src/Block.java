import java.awt.Color;

/**
 * A collection of tiles that forms a block;
 * stores which tiles it corresponds to and what color it is.
 *
 * @author Robert Campbell
 */
public class Block {
	private Color color;
	// All co-ordinates of tiles which this block contains
	private int[] xPositions;
	private int[] yPositions;
	// The point around which this block rotates
	private int originX;
	private int originY;

	/**
	 * Constructor; creates a new Block
	 * @param x The initial x positions of the block's tiles
	 * @param y The initial y positions of the block's tiles
	 * @param originX The center x position of the block
	 * @param originY The center y position of the block
	 * @param color The colour of the block
	 * Assumes that both arrays are the same size, otherwise throws an exception
	 */
	public Block(int[] x, int[] y, int originX, int originY, Color color){
		xPositions = x;
		yPositions = y;
		this.originX = originX;
		this.originY = originY;
		this.color = color;
		assert xPositions.length == yPositions.length : "Uneven position counts were given on creation of a block.";
	}

	/**
	 * Shifts each block down one tile
	 */
	public void shiftDown(){
		for(int i = 0; i < yPositions.length; i++){
			yPositions[i]++;
		}
		originY++;
	}

	/**
	 * Shifts each block left one tile
	 */
	public void shiftLeft(){
		for(int i = 0; i < xPositions.length; i++){
			xPositions[i]--;
		}
		originX--;
	}

	/**
	 * Shifts each block right one tile
	 */
	public void shiftRight(){
		for(int i = 0; i < xPositions.length; i++){
			xPositions[i]++;
		}
		originX++;
	}

	/**
	 * Removes the given tile position from the block
	 * Assumes that the tile position exists on the block
	 */
	public void removeTile(int x, int y){
		// DOES NOT CAUSE BUG

		int[] newXPositions = new int[xPositions.length-1];
		int[] newYPositions = new int[yPositions.length-1];
		int removePos = -1;
		for(int i = 0; i < xPositions.length-1; i++){
			if(xPositions[i] == x && yPositions[i] == y){
				removePos = i;
			}
		}
		assert removePos != -1 : "Remove position wasn't set!";
		for(int i = 0; i < newXPositions.length; i++){
			if(i == removePos){
				continue;
			}
			newXPositions[i] = xPositions[i];
			newYPositions[i] = yPositions[i];
		}

		xPositions = newXPositions.clone();
		yPositions = newYPositions.clone();
	}

	/**
	 * Adds the given tile position to the block
	 */
	public void addTile(int x, int y){
		int[] newXPositions = new int[xPositions.length+1];
		int[] newYPositions = new int[yPositions.length+1];
		for(int i = 0; i < xPositions.length; i++){
			newXPositions[i] = xPositions[i];
			newYPositions[i] = yPositions[i];
		}
		newXPositions[newXPositions.length-1] = x;
		newYPositions[newYPositions.length-1] = y;
		xPositions = newXPositions.clone();
		yPositions = newYPositions.clone();
	}

	/**
	 * Shifts the given tile down by one space
	 */
	public void shiftTileDown(int x, int y){
		for(int i = 0; i < xPositions.length; i++){
			if(xPositions[i] == x && yPositions[i] == y){
				yPositions[i]++;
			}
		}
	}

	/**
	 * Returns the color of the block
	 */
	public Color getColor(){
		return color;
	}
	/**
	 * Returns an array of x positions of tiles that the block contains
	 */
	public int[] getXPositions(){
		return xPositions;
	}
	/**
	 * Returns an array of y positions of tiles that the block contains
	 */
	public int[] getYPositions(){
		return yPositions;
	}
	/**
	 * Returns the horizontal position of the center of rotation for this block
	 */
	public int getOriginX(){
		return originX;
	}
	/**
	 * Returns the vertical position of the center of rotation for this block
	 */
	public int getOriginY(){
		return originY;
	}

	/**
	 * Rotates the block in the specified direction
	 */
	public void turn(boolean isClockwise){
		int[] newXPositions = new int[xPositions.length];
		int[] newYPositions = new int[yPositions.length];
		for(int i = 0; i < xPositions.length; i++){
			int xDist = xPositions[i] - originX;
			int yDist = yPositions[i] - originY;
			if(isClockwise){
				// To rotate clockwise, the new y position of a tile will be its old x position,
				// and the new x position of a tile will be the negative of its old y position.
				newXPositions[i] = originX - yDist;
				newYPositions[i] = originY + xDist;
			}
			else{
				// To rotate anticlockwise, the new y position of the tile will be the negative of its old x position,
				// and the new y position of a tile will be its old y position.
				newXPositions[i] = originX + yDist;
				newYPositions[i] = originY - xDist;
			}
		}
		xPositions = newXPositions;
		yPositions = newYPositions;
	}
}
