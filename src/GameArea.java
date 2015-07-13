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
		// 'Clear' the graphics first
		g2d.setColor(BACKGROUND_COLOR);
		g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
			for(int j = 0; j < Game.VERTICAL_TILES; j++){
				Block block = gameInstance.getBlockAtTile(i, j);
				if(block != null){
					g2d.setColor(block.getColor());
					g2d.fillRect(i*Game.TILE_SIZE, j*Game.TILE_SIZE, Game.TILE_SIZE, Game.TILE_SIZE);
					
					if(Game.DEBUG){
						System.out.println("Drawing tile at position [" + i*Game.TILE_SIZE + "," + j*Game.TILE_SIZE + "]." + " Color: " + block.getColor() + ".");
					}
				}
			}
		}
	}
}
