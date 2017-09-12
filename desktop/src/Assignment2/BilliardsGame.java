package Assignment2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.BufferUtils;

import java.awt.geom.Point2D;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BilliardsGame extends ApplicationAdapter {
	private FloatBuffer vertexBuffer;

	private FloatBuffer modelMatrix;
	private FloatBuffer projectionMatrix;

	private int renderingProgramID;
	private int vertexShaderID;
	private int fragmentShaderID;
	
	private int positionLoc;

	private int modelMatrixLoc;
	private int projectionMatrixLoc;

	private int colorLoc;
	
	////////////////////////
	
	private float ballRadius;
	private Point2D startOfBallPyramid;
	private Point2D[] balls = new Point2D[15];
	private int[] ballColors = new int[15];
	private Point2D whiteBall;
	private Point2D[] gameBoard = new Point2D[2];
	private Point2D[] wholeTable = new Point2D[2];
	private Point2D[] woodenTable = new Point2D[2];
	private Point2D[] holes = new Point2D[6];
	
	private Vector2[] ballMovements = new Vector2[15];
	private Vector2 whiteMovement;
	private float kFriction = 0.4f;
	private int sFriction = 5;
	
	//private SpriteBatch batch = new SpriteBatch();
	//private Texture txt = new Texture ("magic_staff-256.png");
	//private Sprite cue = new Sprite(txt);
	
	@Override
	public void create() {
		String vertexShaderString;
		String fragmentShaderString;
		
		vertexShaderString = Gdx.files.internal("shaders/simple2D.vert").readString();
		fragmentShaderString =  Gdx.files.internal("shaders/simple2D.frag").readString();

		vertexShaderID = Gdx.gl.glCreateShader(GL20.GL_VERTEX_SHADER);
		fragmentShaderID = Gdx.gl.glCreateShader(GL20.GL_FRAGMENT_SHADER);
	
		Gdx.gl.glShaderSource(vertexShaderID, vertexShaderString);
		Gdx.gl.glShaderSource(fragmentShaderID, fragmentShaderString);
	
		Gdx.gl.glCompileShader(vertexShaderID);
		Gdx.gl.glCompileShader(fragmentShaderID);

		renderingProgramID = Gdx.gl.glCreateProgram();
	
		Gdx.gl.glAttachShader(renderingProgramID, vertexShaderID);
		Gdx.gl.glAttachShader(renderingProgramID, fragmentShaderID);
	
		Gdx.gl.glLinkProgram(renderingProgramID);

		positionLoc				= Gdx.gl.glGetAttribLocation(renderingProgramID, "a_position");
		Gdx.gl.glEnableVertexAttribArray(positionLoc);

		modelMatrixLoc			= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_modelMatrix");
		projectionMatrixLoc	= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_projectionMatrix");

		colorLoc				= Gdx.gl.glGetUniformLocation(renderingProgramID, "u_color");

		Gdx.gl.glUseProgram(renderingProgramID);
		
		float[] pm = new float[16];

		pm[0] = 2.0f / Gdx.graphics.getWidth(); pm[4] = 0.0f; pm[8] = 0.0f; pm[12] = -1.0f;
		pm[1] = 0.0f; pm[5] = 2.0f / Gdx.graphics.getHeight(); pm[9] = 0.0f; pm[13] = -1.0f;
		pm[2] = 0.0f; pm[6] = 0.0f; pm[10] = 1.0f; pm[14] = 0.0f;
		pm[3] = 0.0f; pm[7] = 0.0f; pm[11] = 0.0f; pm[15] = 1.0f;

		projectionMatrix = BufferUtils.newFloatBuffer(16);
		projectionMatrix.put(pm);
		projectionMatrix.rewind();
		Gdx.gl.glUniformMatrix4fv(projectionMatrixLoc, 1, false, projectionMatrix);


		float[] mm = new float[16];

		mm[0] = 1.0f; mm[4] = 0.0f; mm[8] = 0.0f; mm[12] = 0.0f;
		mm[1] = 0.0f; mm[5] = 1.0f; mm[9] = 0.0f; mm[13] = 0.0f;
		mm[2] = 0.0f; mm[6] = 0.0f; mm[10] = 1.0f; mm[14] = 0.0f;
		mm[3] = 0.0f; mm[7] = 0.0f; mm[11] = 0.0f; mm[15] = 1.0f;

		modelMatrix = BufferUtils.newFloatBuffer(16);
		modelMatrix.put(mm);
		modelMatrix.rewind();

		Gdx.gl.glUniformMatrix4fv(modelMatrixLoc, 1, false, modelMatrix);

		//COLOR IS SET HERE
		Gdx.gl.glUniform4f(colorLoc, 0.7f, 0.2f, 0, 1);


		//VERTEX ARRAY IS FILLED HERE
		float[] array = {-50.0f, -50.0f,
						-50.0f, 50.0f,
						50.0f, -50.0f,
						50.0f, 50.0f};

		vertexBuffer = BufferUtils.newFloatBuffer(400);
		vertexBuffer.put(array);
		vertexBuffer.rewind();
				
		setupTable();
		setupBalls();
	}
	
	public void setupTable() {

		//Create larger table
		woodenTable[0] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() - 1.1*Gdx.graphics.getHeight()/4), (0));
		woodenTable[1] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() + 1.1*Gdx.graphics.getHeight()/4), Gdx.graphics.getHeight());
		//Create table edges
		wholeTable[0] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() - 15*Gdx.graphics.getHeight()/64), (0 + 3*Gdx.graphics.getHeight()/64));
		wholeTable[1] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() + 15*Gdx.graphics.getHeight()/64), Gdx.graphics.getHeight() - 3*Gdx.graphics.getHeight()/64);
		//Create billiard table of size 3/4 display height * 3/8 display height
		gameBoard[0] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() - 7*Gdx.graphics.getHeight()/32), (0 + Gdx.graphics.getHeight()/16));
		gameBoard[1] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() + 7*Gdx.graphics.getHeight()/32), Gdx.graphics.getHeight() - Gdx.graphics.getHeight()/16);
		//Create the holes at the corners and the middle of the long edges
		holes[0] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() - 15*Gdx.graphics.getHeight()/64), (0 + 3*Gdx.graphics.getHeight()/64));
		holes[1] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() - 15*Gdx.graphics.getHeight()/64), Gdx.graphics.getHeight()/2);
		holes[2] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() + 15*Gdx.graphics.getHeight()/64), (Gdx.graphics.getHeight()/2));
		holes[3] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() - 15*Gdx.graphics.getHeight()/64), Gdx.graphics.getHeight() - 3*Gdx.graphics.getHeight()/64);
		holes[4] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() + 15*Gdx.graphics.getHeight()/64), (0 + 3*Gdx.graphics.getHeight()/64));
		holes[5] = new Point2D.Float((float)(0.5*Gdx.graphics.getWidth() + 15*Gdx.graphics.getHeight()/64), Gdx.graphics.getHeight() - 3*Gdx.graphics.getHeight()/64);
	}
	
	public void setupBalls() {
		ballRadius = (float) 7/500*Gdx.graphics.getHeight();
		
		double sixtyDegrees = Math.PI/3;
		startOfBallPyramid = new Point2D.Float((float)(Gdx.graphics.getWidth()/2 - 4*ballRadius), Gdx.graphics.getHeight()/2 + 7*Gdx.graphics.getHeight()/32 + 8*ballRadius);
		whiteBall = new Point2D.Float((float) Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2 - 7*Gdx.graphics.getHeight()/32);
		//TODO: refactor into a clever loop
		balls[0] = new Point2D.Float((float) startOfBallPyramid.getX(), (float) startOfBallPyramid.getY());
		float firstRowY = (float) balls[0].getY();
		for(int i = 1; i < 5; i++) {
			balls[i] = new Point2D.Float((float) balls[i-1].getX() + 2*ballRadius, firstRowY);
		}
		balls[5] = new Point2D.Float((float) balls[0].getX()+ballRadius, (float) (balls[0].getY()-2*Math.sin(sixtyDegrees)*ballRadius));
		float secondRowY = (float) balls[5].getY();
		for(int i = 6; i < 9; i++) {
			balls[i] = new Point2D.Float((float) balls[i-1].getX() + 2*ballRadius, secondRowY);
		}
		balls[9] = new Point2D.Float((float) balls[5].getX()+ballRadius, (float) (balls[5].getY()-2*Math.sin(sixtyDegrees)*ballRadius));
		float thirdRowY = (float) balls[9].getY();
		for(int i = 10; i < 12; i++) {
			balls[i] = new Point2D.Float((float) balls[i-1].getX()+2*ballRadius, thirdRowY);
		}
		balls[12] = new Point2D.Float((float) balls[9].getX()+ballRadius, (float) (balls[9].getY()-2*Math.sin(sixtyDegrees)*ballRadius));
		balls[13] = new Point2D.Float((float) balls[12].getX()+2*ballRadius,(float)  balls[12].getY());
		balls[14] = new Point2D.Float((float) balls[12].getX()+ballRadius, (float) (balls[12].getY()-2*Math.sin(sixtyDegrees)*ballRadius));
		assignBalls();
		
		whiteMovement = new Vector2(0,200);
		for(int i = 0; i < 15; i++) {
			ballMovements[i] = new Vector2(0,0);
		}
	}
	
	public void assignBalls() {
		Random rand = new Random();
		ballColors[0] = rand.nextInt(2);
		ballColors[4] = (ballColors[0]+1)%2;
		ballColors[10] = 2;
		ballColors[14] = 1;
		int reds = 5;
		int blues = 6;
		for(int i = 1; i < 4; i++) {
			int ran = rand.nextInt(2);
			if(ran == 1 && reds > 0) {
				ballColors[i] = 1;
				reds--;
			} else if(blues > 0){
				ballColors[i] = 0;
				blues--;
			} else { ballColors[i] = 1; }
		}
		for(int i = 5; i < 10; i++) {
			int ran = rand.nextInt(2);
			if(ran == 1 && reds > 0) {
				ballColors[i] = 1;
				reds--;
			} else if(blues > 0){
				ballColors[i] = 0;
				blues--;
			} else { ballColors[i] = 1; }
		}
		for(int i = 11; i < 14; i++) {
			int ran = rand.nextInt(2);
			if(ran == 1 && reds > 0) {
				ballColors[i] = 1;
				reds--;
			} else if(blues > 0){
				ballColors[i] = 0;
				blues--;
			} else { ballColors[i] = 1; }
		}
	}
	
	public void setColor(int color) {
		if(color == 1) {
			Gdx.gl.glUniform4f(colorLoc, 1.0f, 0.0f, 0.0f, 1f);
		} else if(color == 2) {
			Gdx.gl.glUniform4f(colorLoc, 0.0f, 0.0f, 0.0f, 1f);
		} else {
			Gdx.gl.glUniform4f(colorLoc, 0.0f, 0.0f, 1.0f, 1f);
		}
	}
	
	public void drawCircle(Point2D pos, float radius) {
		int vertexNumber = 30;
		int sideCount = vertexNumber - 2;
		double twoPi = 2*Math.PI;
		float x = (float)pos.getX();
		float y = (float)pos.getY();
		float xVertices[] = new float[vertexNumber];
		float yVertices[] = new float[vertexNumber];
		xVertices[0] = x;
		yVertices[0] = y;
		for(int i = 1; i < vertexNumber; i++) {
			xVertices[i] = (float)(x + (radius * Math.cos(i*twoPi/sideCount)));
			yVertices[i] = (float)(y + (radius * Math.sin(i*twoPi/sideCount)));
		}
		for(int i = 0; i < vertexNumber; i++) {
			vertexBuffer.put(i*2, xVertices[i]);
			vertexBuffer.put(i*2 + 1, yVertices[i]);
		}
		Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_FAN, 0, vertexNumber);
 	}

	public void displayTable() {
		//display table edges
				vertexBuffer.put(0, (float)woodenTable[0].getX()); //x coordinate 1
				vertexBuffer.put(1, (float)woodenTable[0].getY()); //y coordinate 1
				vertexBuffer.put(2, (float)woodenTable[0].getX()); //x coordinate 2
				vertexBuffer.put(3, (float)woodenTable[1].getY()); //y coordinate 2
				vertexBuffer.put(4, (float)woodenTable[1].getX()); //x coordinate 3
				vertexBuffer.put(5, (float)woodenTable[0].getY()); //y coordinate 3
				vertexBuffer.put(6, (float)woodenTable[1].getX()); //x coordinate 4
				vertexBuffer.put(7, (float)woodenTable[1].getY()); //y coordinate 4
				Gdx.gl.glVertexAttribPointer(positionLoc, 2, GL20.GL_FLOAT, false, 0, vertexBuffer);
				Gdx.gl.glUniform4f(colorLoc, 0.6f, 0.35f, 0.1f, 0.5f);
				Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_STRIP, 0, 4);
				//display table edges
				vertexBuffer.put(0, (float)wholeTable[0].getX()); //x coordinate 1
				vertexBuffer.put(1, (float)wholeTable[0].getY()); //y coordinate 1
				vertexBuffer.put(2, (float)wholeTable[0].getX()); //x coordinate 2
				vertexBuffer.put(3, (float)wholeTable[1].getY()); //y coordinate 2
				vertexBuffer.put(4, (float)wholeTable[1].getX()); //x coordinate 3
				vertexBuffer.put(5, (float)wholeTable[0].getY()); //y coordinate 3
				vertexBuffer.put(6, (float)wholeTable[1].getX()); //x coordinate 4
				vertexBuffer.put(7, (float)wholeTable[1].getY()); //y coordinate 4
				Gdx.gl.glVertexAttribPointer(positionLoc, 2, GL20.GL_FLOAT, false, 0, vertexBuffer);
				Gdx.gl.glUniform4f(colorLoc, 0.2f, 0.50f, 0.1f, 0.5f);
				Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_STRIP, 0, 4);
				//display table
				vertexBuffer.put(0, (float)gameBoard[0].getX()); //x coordinate 1
				vertexBuffer.put(1, (float)gameBoard[0].getY()); //y coordinate 1
				vertexBuffer.put(2, (float)gameBoard[0].getX()); //x coordinate 2
				vertexBuffer.put(3, (float)gameBoard[1].getY()); //y coordinate 2
				vertexBuffer.put(4, (float)gameBoard[1].getX()); //x coordinate 3
				vertexBuffer.put(5, (float)gameBoard[0].getY()); //y coordinate 3
				vertexBuffer.put(6, (float)gameBoard[1].getX()); //x coordinate 4
				vertexBuffer.put(7, (float)gameBoard[1].getY()); //y coordinate 4
				Gdx.gl.glVertexAttribPointer(positionLoc, 2, GL20.GL_FLOAT, false, 0, vertexBuffer);
				Gdx.gl.glUniform4f(colorLoc, 0.3f, 0.75f, 0.1f, 0.5f);
				Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_STRIP, 0, 4);
				Gdx.gl.glUniform4f(colorLoc, 0.0f, 0.0f, 0.0f, 1f);
				for(int i = 0; i < holes.length; i++) {
					drawCircle(holes[i], (float)(ballRadius*1.1));
				}
				
				
	}
	
	public void displayBalls() {
		for(int i = 0; i < 15; i++) {
			setColor(ballColors[i]);
			drawCircle(balls[i], ballRadius);
		}
		//White Ball
		Gdx.gl.glUniform4f(colorLoc, 1.0f, 1.0f, 1.0f, 1f);
		drawCircle(whiteBall, ballRadius);
	}
	
	///////////////////
	
	public void moveBalls() {
		float delta = Gdx.graphics.getDeltaTime();
		whiteBall = new Point2D.Float((float) (whiteBall.getX() + whiteMovement.x*delta), (float) (whiteBall.getY() + whiteMovement.y*delta));
		Vector2 v = new Vector2(-whiteMovement.x,-whiteMovement.y);
		if(whiteMovement.len() > sFriction) {
			v.scl(kFriction*delta);
			whiteMovement.add(v);
		}
		else
			whiteMovement.set(0, 0);
		for(int i = 0; i < 15; i++) {
			balls[i] = new Point2D.Float((float) (balls[i].getX() + ballMovements[i].x*delta),(float) balls[i].getY() + ballMovements[i].y*delta);
			Vector2 g = new Vector2(-ballMovements[i].x, -ballMovements[i].y);
			if(ballMovements[i].len() > sFriction) {
				g.scl(kFriction*delta);
				ballMovements[i].add(g);
			}
			else
				ballMovements[i].set(0, 0);
		}
	}
	
	public void update() {
		int mouseX = Gdx.input.getX();
		int mouseY = Gdx.input.getY();
		moveBalls();
	}
	
	public void display() {
		displayTable();
		displayBalls();
	}
	
	public void render() {
		update();
		display();
	}
}
