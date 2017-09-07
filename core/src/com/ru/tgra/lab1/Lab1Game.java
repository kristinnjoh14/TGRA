package com.ru.tgra.lab1;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.utils.BufferUtils;


public class Lab1Game extends ApplicationAdapter {
	
	private FloatBuffer vertexBuffer;

	private FloatBuffer modelMatrix;
	private FloatBuffer projectionMatrix;

	private int renderingProgramID;
	private int vertexShaderID;
	private int fragmentShaderID;
	
	private float x1;
	private float y1;
	private float x2;
	private float y2;
	private float x3;
	private float y3;
	private float coordOffset = 50.0f;
	private int xchng;
	private int ychng;
	private int speed = 5;
	private int spd = 7;
	private List<Square> boxlist = new LinkedList<Square>();
	
	private int positionLoc;

	private int modelMatrixLoc;
	private int projectionMatrixLoc;

	private int colorLoc;

	@Override
	public void create () {

		String vertexShaderString;
		String fragmentShaderString;

		x1 = 300;
		y1 = 300;
		xchng = speed;
		ychng = speed;
		x3 = 150;
		y3 = 150;
				
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

		vertexBuffer = BufferUtils.newFloatBuffer(8);
		vertexBuffer.put(array);
		vertexBuffer.rewind();
	}
	
	private void update()
	{
		if(Gdx.input.justTouched())
		{
			x2 = Gdx.input.getX();
			y2 = Gdx.graphics.getHeight() - Gdx.input.getY();
			boxlist.add(new Square(x2 - coordOffset, y2 + coordOffset, x2 + coordOffset, y2 - coordOffset));
		}
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			x3 = x3 - spd;
			if(x3 < 0 + coordOffset)
				x3 = 0 + coordOffset;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			x3 = x3 + spd;
			if(x3 > Gdx.graphics.getWidth() - coordOffset)
				x3 = Gdx.graphics.getWidth() - coordOffset;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
			y3 = y3 + spd;
			if(y3 > Gdx.graphics.getHeight() - coordOffset)
				y3 = Gdx.graphics.getHeight() - coordOffset;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			y3 = y3 - spd;
			if(y3 < 0 + coordOffset)
				y3 = 0 + coordOffset;
		}
		if(x1 >= Gdx.graphics.getWidth() - coordOffset) 
			xchng = - speed;
		if(y1 >= Gdx.graphics.getHeight() - coordOffset) 
			ychng = - speed;
		if(x1 <= 0 + coordOffset) 
			xchng = speed;
		if(y1 <= 0 + coordOffset) 
			ychng = speed;
		x1 = x1 + xchng;
		y1 = y1 + ychng;
	}
	
	private void display()
	{
		if(!boxlist.isEmpty()) {
			for(int i = 0; i < boxlist.size(); i++) {
				vertexBuffer.put(0, boxlist.get(i).x1); //x coordinate 1
				vertexBuffer.put(1, boxlist.get(i).y1); //y coordinate 1
				vertexBuffer.put(2, boxlist.get(i).x1); //x coordinate 2
				vertexBuffer.put(3, boxlist.get(i).y2); //y coordinate 2
				vertexBuffer.put(4, boxlist.get(i).x2); //x coordinate 3
				vertexBuffer.put(5, boxlist.get(i).y1); //y coordinate 3
				vertexBuffer.put(6, boxlist.get(i).x2); //x coordinate 4
				vertexBuffer.put(7, boxlist.get(i).y2); //y coordinate 4
				Gdx.gl.glVertexAttribPointer(positionLoc, 2, GL20.GL_FLOAT, false, 0, vertexBuffer);
				Gdx.gl.glUniform4f(colorLoc, 1.0f, 0.0f, 1.0f, 0.5f);
				Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_STRIP, 0, 4);
			}
		}
		
		vertexBuffer.put(0, x1 - coordOffset); //x coordinate 1
		vertexBuffer.put(1, y1 - coordOffset); //y coordinate 1
		vertexBuffer.put(2, x1 - coordOffset); //x coordinate 2
		vertexBuffer.put(3, y1 + coordOffset); //y coordinate 2
		vertexBuffer.put(4, x1 + coordOffset); //x coordinate 3
		vertexBuffer.put(5, y1 - coordOffset); //y coordinate 3
		vertexBuffer.put(6, x1 + coordOffset); //x coordinate 4
		vertexBuffer.put(7, y1 + coordOffset); //y coordinate 4
		Gdx.gl.glVertexAttribPointer(positionLoc, 2, GL20.GL_FLOAT, false, 0, vertexBuffer);
		Gdx.gl.glUniform4f(colorLoc, 0.0f, 1.0f, 1.0f, 0.5f);
		Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_STRIP, 0, 4);
		
		vertexBuffer.put(0, x3 - coordOffset); //x coordinate 1
		vertexBuffer.put(1, y3 - coordOffset); //y coordinate 1
		vertexBuffer.put(2, x3 - coordOffset); //x coordinate 2
		vertexBuffer.put(3, y3 + coordOffset); //y coordinate 2
		vertexBuffer.put(4, x3 + coordOffset); //x coordinate 3
		vertexBuffer.put(5, y3 - coordOffset); //y coordinate 3
		vertexBuffer.put(6, x3 + coordOffset); //x coordinate 4
		vertexBuffer.put(7, y3 + coordOffset); //y coordinate 4
		Gdx.gl.glVertexAttribPointer(positionLoc, 2, GL20.GL_FLOAT, false, 0, vertexBuffer);
		Gdx.gl.glUniform4f(colorLoc, 0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glDrawArrays(GL20.GL_TRIANGLE_STRIP, 0, 4);
		
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.25f, 0.3f,1.0f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//put the code inside the update and display methods, depending on the nature of the code
		update();
		display();
	}


	private void clearModelMatrix()
	{
		modelMatrix.put(0, 1.0f);
		modelMatrix.put(1, 0.0f);
		modelMatrix.put(2, 0.0f);
		modelMatrix.put(3, 0.0f);
		modelMatrix.put(4, 0.0f);
		modelMatrix.put(5, 1.0f);
		modelMatrix.put(6, 0.0f);
		modelMatrix.put(7, 0.0f);
		modelMatrix.put(8, 0.0f);
		modelMatrix.put(9, 0.0f);
		modelMatrix.put(10, 1.0f);
		modelMatrix.put(11, 0.0f);
		modelMatrix.put(12, 0.0f);
		modelMatrix.put(13, 0.0f);
		modelMatrix.put(14, 0.0f);
		modelMatrix.put(15, 1.0f);

		Gdx.gl.glUniformMatrix4fv(modelMatrixLoc, 1, false, modelMatrix);
	}
	private void setModelMatrixTranslation(float xTranslate, float yTranslate)
	{
		modelMatrix.put(12, xTranslate);
		modelMatrix.put(13, yTranslate);

		Gdx.gl.glUniformMatrix4fv(modelMatrixLoc, 1, false, modelMatrix);
	}
	private void setModelMatrixScale(float xScale, float yScale)
	{
		modelMatrix.put(0, xScale);
		modelMatrix.put(5, yScale);

		Gdx.gl.glUniformMatrix4fv(modelMatrixLoc, 1, false, modelMatrix);
	}
}