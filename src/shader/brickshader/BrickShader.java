package shader.brickshader;

import static opengl.GL.glGetUniformLocation;
import static opengl.GL.glUniform3f;
import opengl.GL;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import shader.ShaderProgram;

public class BrickShader extends ShaderProgram {
	
	private Vector3f m_brickColor = new Vector3f();
	private Vector3f m_mortarColor = new Vector3f();
	
	//Horizontal and Vertical Size of a Brick
	private Vector2f m_brickSize = new Vector2f();

	//Percentage of the Brick [0,1]
	private Vector2f m_brickPct = new Vector2f();
	
	private Vector3f m_lightPosition = new Vector3f();
	
	private int m_brickSizeLoc;
    private int m_brickPctLoc;
    private int m_brickColorLoc;
    private int m_mortarColorLoc;
    private int m_lightPositionLoc;
	
	public BrickShader(String name) {
		super(name);
	}

	@Override
	public void init() {
		this.createShaderProgram("./shader/Brick_VS.glsl", "./shader/Brick_FS.glsl");
		
		m_brickSizeLoc = glGetUniformLocation(m_programId, "BrickSize");
		m_brickPctLoc = glGetUniformLocation(m_programId, "BrickPct");
		m_brickColorLoc = glGetUniformLocation(m_programId, "BrickColor");
		m_mortarColorLoc = glGetUniformLocation(m_programId, "MortarColor");
		m_lightPositionLoc = glGetUniformLocation(m_programId, "LightPosition");
	}

	@Override
	protected void setStaticUniforms() {
		GL.glUniform2f(m_brickSizeLoc, m_brickSize.x, m_brickSize.y);
		GL.glUniform2f(m_brickPctLoc, m_brickPct.x, m_brickPct.y);
		glUniform3f(m_brickColorLoc, m_brickColor.x, m_brickColor.y, m_brickColor.z);
		glUniform3f(m_mortarColorLoc, m_mortarColor.x, m_mortarColor.y, m_mortarColor.z);
	}
	
	public void setBrickColor(float r, float g, float b) {
		this.m_brickColor.x = r;
		this.m_brickColor.y = g;
		this.m_brickColor.z = b;
	}
	
	public void setMortarColor(float r, float g, float b) {
		this.m_mortarColor.x = r;
		this.m_mortarColor.y = g;
		this.m_mortarColor.z = b;
	}
	
	public void setLightPosition(float x, float y, float z) {
		this.m_lightPosition.x = x;
		this.m_lightPosition.y = y;
		this.m_lightPosition.z = z;
	}
	
	public void setBrickSize(float x, float y) {
		this.m_brickSize.x = x;
		this.m_brickSize.y = y;
	}
	
	public void setBrickPct(float x, float y) {
		this.m_brickPct.x = x;
		this.m_brickPct.y = y;
	}

	@Override
	protected void setDynamicUniforms() {
		glUniform3f(m_lightPositionLoc, m_lightPosition.x, m_lightPosition.y, m_lightPosition.z);		
	}

	@Override
	public void deUse() {
		// TODO Auto-generated method stub
		
	}

}
