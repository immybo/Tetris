import javax.swing.*;
import java.awt.*;

/**
 * The post-main screen graphics of the game,
 * including the GUI and the gameplay area.
 *
 * @author Robert Campbell
 */
public class GameScreen{
	private JFrame frame;
	private JPanel gameArea;
	private JPanel gui;
	private Game gameInstance;

	public GameScreen(Game gameInstance){
		this.gameInstance = gameInstance;

		// Initialise the frame
		frame = new JFrame("TETRIS");
		frame.setSize(Game.GAME_AREA_WIDTH + Game.GUI_WIDTH, Game.GAME_AREA_HEIGHT);
		frame.setLocation(100, 100);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Set up the panels
		gameArea = new GameArea(this.gameInstance);
		gui = new JPanel();
		gameArea.setVisible(true);
		gui.setVisible(true);

		gameArea.setPreferredSize(new Dimension(Game.GAME_AREA_WIDTH, Game.GAME_AREA_HEIGHT));
		gui.setPreferredSize(new Dimension(Game.GUI_WIDTH, Game.GUI_HEIGHT));

		// Add the panels to the frame
		frame.add(gameArea, BorderLayout.WEST);
		frame.add(gui, BorderLayout.EAST);
		// And finally set the frame to be visible
		frame.setVisible(true);

	}

	/**
	 * Repaints all graphics
	 */
	public void repaint(){
		gameArea.repaint();
	}
}
