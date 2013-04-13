package shader.solsystem;

import static opengl.GL.glGetUniformLocation;
import static opengl.GL.glUniform3f;

import org.lwjgl.util.vector.Vector3f;

import shader.ShaderProgram;


public class OrbShader extends ShaderProgram {

	private int m_colorLoc;
	
	private Vector3f m_color;
	
	public void setColor(Vector3f color) {
		m_color = color;
	}

	public void setColor(float r, float g, float b) {
		m_color = new Vector3f();
		m_color.x = r;
		m_color.y = g;
		m_color.z = b;
	}
	
	public OrbShader() {
		super();
	}
	
	@Override
	public void init() {
		this.createShaderProgram("./shader/Orb_VS.glsl", "./shader/Orb_FS.glsl");
		m_colorLoc = glGetUniformLocation(m_programId, "color");
	}

	@Override
	protected void setStaticUniforms() {
	}

	@Override
	protected void setDynamicUniforms() {
		glUniform3f(m_colorLoc, m_color.x, m_color.y, m_color.z);
	}

	@Override
	public void deUse() {
		// TODO Auto-generated method stub

	}

}
