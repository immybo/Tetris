import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * A JPanel to display data from a game of tetris being played.
 * An instance of the game must be provided.
 *
 * @author Robert Campbell
 *
 */
public class GameUI extends JPanel {
	private static Color LABEL_COLOR = Color.BLACK;
	private static Color DATA_COLOR = Color.BLACK;
	private static Color BACKGROUND_COLOR = Color.WHITE;
	// Stores the data from the previous draw iteration to erase it
	private static double oldScore = 0;
	private static double oldLevel = 1;

	Game gameInstance;

	/**
	 * Constructor; creates a new GameUI.
	 *
	 * @param gameInstance The Game instance linked to this GameUI instance.
	 */
	public GameUI(Game gameInstance){
		this.setFocusable(false);
		this.gameInstance = gameInstance;
	}

	/**
	 * Redraws all components.
	 */
	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D) g;

		// Firstly, redraw the background color over the area
		g2d.setColor(BACKGROUND_COLOR);
		g2d.drawRect(0,0,this.getWidth(),this.getHeight());

		// Then, draw the labels and data
		g2d.setColor(LABEL_COLOR);

		g2d.drawString("SCORE", this.getWidth()/2-50, 200);
		g2d.drawString("LEVEL", this.getWidth()/2-50, 100);

		g2d.setColor(DATA_COLOR);

		g2d.drawString(gameInstance.getScore() + "", this.getWidth()/2-50, 230);
		g2d.drawString(gameInstance.getLevel() + "", this.getWidth()/2-50, 130);
	}
}
