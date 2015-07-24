import java.nio.*;
import java.io.*;
import java.awt.*;
import java.util.*;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.glfw.GLFWvidmode;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * The post-main screen graphics of the game,
 * including the GUI and the gameplay area.
 *
 * @author Robert Campbell
 */
public class GameScreen{
	private GLFWErrorCallback errorCallback = errorCallbackPrint(System.err);
	private GLFWKeyCallback keyCallback;

	private int width = Game.GAME_AREA_WIDTH;
	private int height = Game.GAME_AREA_HEIGHT;

	public static Color BACKGROUND_COLOR = Color.WHITE;
	public static Color BORDER_COLOR = Color.BLACK;

	ByteBuffer vidMode;

	private long glWindow;

	private Game gameInstance;

	// Keys used for movement and rotation
	private final int KEY_MOVE_DOWN = GLFW_KEY_DOWN;
	private final int KEY_MOVE_LEFT = GLFW_KEY_LEFT;
	private final int KEY_MOVE_RIGHT = GLFW_KEY_RIGHT;
	private final int KEY_ROTATE_RIGHT = GLFW_KEY_E;
	private final int KEY_ROTATE_LEFT = GLFW_KEY_Q;
	private final int KEY_END_GAME = GLFW_KEY_ESCAPE;
	// The key that has been pressed down, indicating the key action that should be taken
	int currentAction = 0;
	// Whether the key for the current action is still down
	boolean currentActionKey = false;
	// Whether the current action has been completed at least once
	boolean currentActionCompleted = false;


	public GameScreen(Game gameInstance){
		this.gameInstance = gameInstance;
		initialise();
	}

	/**
	 * Initialises the game area and UI area
	 */
	private void initialise(){
		// Firstly, allow errors to be given to System.err
		glfwSetErrorCallback(errorCallback);

		// Initialise GLFW before we do anything (and throw an exception if it fails)
		if(glfwInit() != GL_TRUE) { throw new IllegalStateException("Unable to initialize GLFW"); }

		// Set the properties (hints) of our window
		glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
		glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 1);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

		// Create the window with the specified width, height and title
		glWindow = glfwCreateWindow(width, height, "TETRIS", MemoryUtil.NULL, MemoryUtil.NULL);

		// Set up a key listener
		glfwSetKeyCallback(glWindow, keyCallback = new GLFWKeyCallback(){
			public void invoke(long window, int key, int scancode, int action, int mods){
				// If a key has been pressed down,
				if(action == GLFW_PRESS){
					// Set the action to take
					currentAction = key;
					// Set that the key is currently pressed down
					currentActionKey = true;
					// Reset the completion status of the action
					currentActionCompleted = false;
				}
				else if(action == GLFW_RELEASE){
					// Set that the key is no longer pressed down
					currentActionKey = false;
				}
			}

		});

		// Get the resolution of the primary monitor and set the window to be in the center
		vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(glWindow, (GLFWvidmode.width(vidMode)-width)/2, (GLFWvidmode.height(vidMode)-height)/2);

		glfwMakeContextCurrent(glWindow);
		GLContext.createFromCurrent();

		glfwSwapInterval(1);

		glfwShowWindow(glWindow);

	}

	/**
	 * Displays in text that the player has lost, and provides them with a key to restart the game
	 */
	public void loseGame(){
		//TODO Complete has lost method, implement text
	}

	/**
	 * Checks for the queued key action and calls the required method based on it.
	 */
	public void doKeys(){
		// Make sure that the window shouldn't have been closed
		if(glfwWindowShouldClose(glWindow) == GL_FALSE){
			// If the key is no longer down and the action has already been done, don't do anything
			if(!currentActionKey && currentActionCompleted){ currentAction = 0; }

			// Otherwise, complete the appropriate action
			switch(currentAction){
			case KEY_MOVE_LEFT:
				gameInstance.moveHorizontally(false);
				break;
			case KEY_MOVE_RIGHT:
				gameInstance.moveHorizontally(true);
				break;
			case KEY_ROTATE_RIGHT:
				gameInstance.turnCurrentPiece(true);
				break;
			case KEY_ROTATE_LEFT:
				gameInstance.turnCurrentPiece(false);
				break;
			case KEY_MOVE_DOWN:
				gameInstance.rushDown();
				break;
			case KEY_END_GAME:
				gameInstance.loseGame();
				dispose();
				break;
			default:
				gameInstance.haltRushDown();
				break;
			}

			// And change the completed flag to true (note that this flag is unimportant if currentActon == 0)
			currentActionCompleted = true;
		}
		else{
			// Stop the game
			gameInstance.loseGame();
		}
	}

	/**
	 * Redraws the relevant areas
	 */
	public void redraw(){
		// Make sure that the window shouldn't have been closed
		if(glfwWindowShouldClose(glWindow) == GL_FALSE){
			// Set the 'default' color
			glClearColor(BACKGROUND_COLOR.getRed(),BACKGROUND_COLOR.getBlue(),BACKGROUND_COLOR.getGreen(),BACKGROUND_COLOR.getAlpha());
			// And clear the screen
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			// Redraws the relevant areas
			redrawGameArea();
			redrawUI();

			glfwSwapBuffers(glWindow);
			// Checks for key events
			glfwPollEvents();
		}
		else{
			// Stop the game
			gameInstance.loseGame();
		}
	}

	/**
	 * Disposes of GLFW assets
	 */
	public void dispose(){
		glfwDestroyWindow(glWindow);
		glfwTerminate();
	}

	/**
	 * Defines redrawing the game area
	 */
	private void redrawGameArea(){
		// Colors which each tile will be
		Color[][] tileColors = new Color[Game.HORIZONTAL_TILES][Game.VERTICAL_TILES];

		// Find the color that each tile is meant to be and set the relevant spot in tileColors to it
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

		// Grab the tiles from the current block as well, since they can't be found with gameInstance.getTileValue(x,y)
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

		for(int i = 0; i < Game.HORIZONTAL_TILES; i++){
			for(int j = 0; j < Game.VERTICAL_TILES; j++){
				drawRect(i*Game.TILE_SIZE, j*Game.TILE_SIZE, Game.TILE_SIZE, Game.TILE_SIZE, tileColors[i][j], BORDER_COLOR);
			}
		}
	}


	/**
	 * Defines redrawing the UI
	 */
	private void redrawUI(){
		// TODO Drawing text
		/*font.drawString(Game.GAME_AREA_WIDTH + 100, 200, "SCORE");
		font.drawString(Game.GAME_AREA_WIDTH + 100, 230, gameInstance.getScore()+"");
		font.drawString(Game.GAME_AREA_WIDTH + 100, 300, "LEVEL");
		font.drawString(Game.GAME_AREA_WIDTH + 100, 330, gameInstance.getLevel()+"");*/
	}


	/**
	 * Draws a rectangle with the specified parameters.
	 * @param x The left-most x position of the rectangle.
	 * @param y The top-most y position of the rectangle.
	 * @param width The width of the rectangle.
	 * @param height The height of the rectangle.
	 * @param fillColor The color that the rectangle will be filled with.
	 * @param borderColor The color of the border of the rectangle.
	 */
	private void drawRect(int x, int y, int width, int height, Color fillColor, Color borderColor){
		// Translate the co-ordinates to be appropriate for OpenGL
		float tX = getFloatX(x);
		float tY = getFloatY(y);

		// Find the right and bottom most positions of the rectangle
		int x2 = x + width;
		int y2 = y + height;

		// Translate the right and bottom most positions to be appropriate for OpenGL
		float tX2 = getFloatX(x2);
		float tY2 = getFloatY(y2);


		// Set the color to the fill color and draw the filled rectangle
		glColor3f(fillColor.getRGBComponents(null)[0], fillColor.getRGBComponents(null)[1], fillColor.getRGBComponents(null)[2]);
		glBegin(GL_QUADS);
			glVertex2f(tX, tY);
			glVertex2f(tX, tY2);
			glVertex2f(tX2, tY2);
			glVertex2f(tX2, tY);
		glEnd();

		// Then set the color to the border color and draw a bunch of lines around the rectangle area
		glColor3f(borderColor.getRGBComponents(null)[0], borderColor.getRGBComponents(null)[1], borderColor.getRGBComponents(null)[2]);
		glBegin(GL_LINE_LOOP);
			glVertex2f(tX, tY);
			glVertex2f(tX, tY2);
			glVertex2f(tX2, tY2);
			glVertex2f(tX2, tY);
		glEnd();
	}

	/**
	 * Converts the specified x value to a float between -1 and 1;
	 * ordinates in OpenGL are between these values.
	 */
	private float getFloatX(int x){
		return ((float)(x*2) / this.width - 1);
	}
	/**
	 * Converts the specified y value to a float between -1 and 1;
	 * ordinates in OpenGL are between these values.
	 */
	private float getFloatY(int y){
		return ((float)(y*2) / this.height - 1) *-1;
	}
}
