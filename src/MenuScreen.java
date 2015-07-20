import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

/**
 * Draws and defines the methods of the first screen which the player
 * will see; allowing them to choose difficulty and initial level before
 * starting a game and view highscores. The player will leave back to
 * this screen after finishing a game.
 *
 * @author campberobe1
 *
 */
public class MenuScreen implements ActionListener {
	// STATIC
	private static final int MENU_SCREEN_WIDTH = 500;
	private static final int MENU_SCREEN_HEIGHT = 500;

	// NON-STATIC
	private JFrame frame;
	private JPanel menuArea;

	private JSlider difficultySlider;
	private JSlider initialLevelSlider;
	private JButton highscoreButton;
	private JButton startGame;

	public static void main(String[] args){
		new MenuScreen();
	}

	public MenuScreen(){
		// Initialise the frame
		frame = new JFrame("Tetris Menu");
		frame.setSize(MenuScreen.MENU_SCREEN_WIDTH, MenuScreen.MENU_SCREEN_HEIGHT);
		frame.setLocation(100, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		// Descriptor label
		JLabel difficultySliderLabel = new JLabel("Difficulty (1-5)",(int) Component.CENTER_ALIGNMENT);
		// New slider from 1-5 with default value 3
		difficultySlider = new JSlider(1,5,3);

		// Level label
		JLabel initialLevelSliderLabel = new JLabel("Initial Level (1-100)",(int) Component.CENTER_ALIGNMENT);
		// Slider from 1-100 with default value 1
		initialLevelSlider = new JSlider(1,100,1);

		// Button to access highscores
		highscoreButton = new JButton("Highscores");
		highscoreButton.setActionCommand("displayHighscores");
		highscoreButton.addActionListener(this);

		// Button to start the game with the specified initial level and difficulty setting
		startGame = new JButton("Start Game");
		startGame.setActionCommand("startGame");
		startGame.addActionListener(this);

		// Align all content in the center
		difficultySlider.setAlignmentX(Component.CENTER_ALIGNMENT);
		initialLevelSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
		highscoreButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		startGame.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Add the content to the frame
		frame.add(difficultySliderLabel);
		frame.add(difficultySlider);
		frame.add(initialLevelSliderLabel);
		frame.add(initialLevelSlider);
		frame.add(highscoreButton);
		frame.add(startGame);

		// And finally set the frame to be visible
		frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e){
		// Highscore button
		if("displayHighscores".equals(e.getActionCommand())){
			displayHighscores();
		}

		else if("startGame".equals(e.getActionCommand())){
			startGame();
		}
	}

	private void displayHighscores(){
		JFrame highscoreFrame = new JFrame("Highscores");
		highscoreFrame.setSize(120,240);
		highscoreFrame.setLocation(100, 100);
		highscoreFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		highscoreFrame.setLayout(new GridLayout(0,1));

		// Create a scanner to read from the highscores file
		int[] highscores = new int[10];
		try{
			Scanner s = new Scanner(new File("highscores"));
			for(int i = 0; i < 10; i++){
				if(s.hasNextInt()){
					highscores[i] = s.nextInt();
				}
			}
		}
		catch(IOException e){
			System.out.println("Failed to read from highscores file. "+e);
		}

		for(int i = 0; i < 10; i++){
			highscoreFrame.add(new JLabel((i+1)+". "+highscores[i]));
		}

		highscoreFrame.setVisible(true);
	}

	private void startGame(){
		new Game(difficultySlider.getValue(), initialLevelSlider.getValue());
	}
}
