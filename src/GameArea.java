import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * The actual area that the game is played in. Handles key events.
 *
 * @author Robert Campbell
 *
 */
public class GameArea extends JPanel implements KeyListener {
	private static Color BACKGROUND_COLOR = Color.WHITE;
	// Stores the tile colors from the last iteration to see which ones have changed
	private Color[][] oldTileColors;

	Game gameInstance;

	public GameArea(Game gameInstance){
		this.setFocusable(true);
		this.gameInstance = gameInstance;
		this.addKeyListener(this);
	}

	public void keyTyped(KeyEvent e){}
	public void keyPressed(KeyEvent e){
		// LEFT ARROW KEY PRESS: Move piece left
		if(e.getKeyCode() == KeyEvent.VK_LEFT){
			gameInstance.moveHorizontally(false);
		}
		// RIGHT ARROW KEY PRESS: Move piece right
		else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
			gameInstance.moveHorizontally(true);
		}
		// DOWN ARROW KEY PRESS: Move piece down faster until release
		else if(e.getKeyCode() == KeyEvent.VK_DOWN){
			gameInstance.rushDown();
		}
		// Q KEY PRESS: Rotate piece anticlockwise
		else if(e.getKeyCode() == KeyEvent.VK_Q){
			gameInstance.turnCurrentPiece(false);
		}
		// E KEY PRESS: Rotate piece clockwise
		else if(e.getKeyCode() == KeyEvent.VK_E){
			gameInstance.turnCurrentPiece(true);
		}
	}
	public void keyReleased(KeyEvent e){
		// DOWN ARROW KEY RELEASE: Stop moving piece down faster
		if(e.getKeyCode() == KeyEvent.VK_DOWN){
			gameInstance.haltRushDown();
		}
	}

	@Override
	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D) g;

		// First, we make an array of tiles and what color they should be
		// This lets repainting be faster as it doesn't have to do any operations beyond accessing this array after clearing the graphics
		// Removing a flickering effect

		// For optimisation, we don't actually need to redraw every tile every frame. Instead, we just check which ones have changed.

		Color[][] tileColors = new Color[Game.HORIZONTAL_TILES][Game.VERTICAL_TILES];

		for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
			for(int j = 0; j < Game.VERTICAL_TILES; j++){
				int tileValue = gameInstance.getTileValue(i,j)-1;
				if(tileValue == -1){
					tileColors[i][j] = BACKGROUND_COLOR;
					continue;
				}
				tileColors[i][j] = Game.BLOCK_COLORS[tileValue];
			}
		}
		// Grab the tiles from the current block as well
		Block currentBlock = gameInstance.getCurrentBlock();
		if(currentBlock != null){
			Color tileValue = currentBlock.getColor();
			for(int i = 0; i < currentBlock.getXPositions().length; i++){
				// If this point is above the screen, don't draw it
				if(currentBlock.getYPositions()[i] < 0){
					continue;
				}
				tileColors[currentBlock.getXPositions()[i]][currentBlock.getYPositions()[i]] = tileValue;
			}
		}

		// If it's the first frame, just draw everything
		if(oldTileColors == null){
			for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
				for(int j = 0; j < Game.VERTICAL_TILES; j++){
					g2d.setColor(tileColors[i][j]);
					g2d.fillRect(i*Game.TILE_SIZE, j*Game.TILE_SIZE, Game.TILE_SIZE, Game.TILE_SIZE);
				}
			}
		}
		// Otherwise, scroll through all tiles and check to see which ones have changed, redrawing only those ones.
		else{
			for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
				for(int j = 0; j < Game.VERTICAL_TILES; j++){
					if(tileColors[i][j] != oldTileColors[i][j]){
						g2d.setColor(tileColors[i][j]);
						g2d.fillRect(i*Game.TILE_SIZE, j*Game.TILE_SIZE, Game.TILE_SIZE, Game.TILE_SIZE);
					}
				}
			}
		}

		// Afterwards, draw a grid for clarity
		g2d.setColor(Color.BLACK);
		for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
			g2d.drawLine(i*Game.TILE_SIZE, 0, i*Game.TILE_SIZE, Game.VERTICAL_TILES*Game.TILE_SIZE);
		}
		for(int i = 0; i < Game.VERTICAL_TILES; i++){
			g2d.drawLine(0, i*Game.TILE_SIZE, Game.HORIZONTAL_TILES*Game.TILE_SIZE, i*Game.TILE_SIZE);
		}

		oldTileColors = tileColors.clone();
	}
}
