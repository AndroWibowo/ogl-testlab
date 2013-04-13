package shader;

import static opengl.GL.GL_FRAGMENT_SHADER;
import static opengl.GL.GL_VERTEX_SHADER;
import static opengl.GL.glAttachShader;
import static opengl.GL.glBindAttribLocation;
import static opengl.GL.glCompileShader;
import static opengl.GL.glCreateProgram;
import static opengl.GL.glCreateShader;
import static opengl.GL.glGetProgramInfoLog;
import static opengl.GL.glGetShaderInfoLog;
import static opengl.GL.glGetUniformLocation;
import static opengl.GL.glLinkProgram;
import static opengl.GL.glShaderSource;
import static opengl.GL.glUseProgram;

import opengl.GL;

import org.lwjgl.util.vector.Matrix4f;

import util.Util;

public abstract class ShaderProgram {

    /**
     * Attribut Index von positionMC
     */
    public static final int ATTR_POS = 0;

    /**
     * Attribut Index von normalMC
     */
    public static final int ATTR_NORMAL = 1;

    /**
     * Attribut Index von vertexColor
     */
    public static final int ATTR_COLOR = 2;
    
    /**
     * Attribut Index von vertexColor2
     */
    public static final int ATTR_COLOR2 = 3;
    
    /**
     * Attribut Index von vertexColor2
     */
    public static final int ATTR_TEX = 4;
	
	protected int m_programId;
	
	protected boolean m_staticUniformsSet;
	
	protected boolean m_initialized;
	
	/**
	 * common uniforms
	 */
    protected int m_modelLoc;
    protected int m_modelITLoc;
    protected int m_viewProjLoc;
    
    protected Matrix4f m_model;
    protected Matrix4f m_modelIT;
    protected Matrix4f m_viewProj;
	
	public ShaderProgram() {
		this.m_programId = -1;
		this.m_staticUniformsSet = false;
	}
	
	public abstract void init();
	
	protected abstract void setStaticUniforms();

	protected abstract void setDynamicUniforms();
	
	public void use() {
		GL.glUseProgram(m_programId);
        if (m_staticUniformsSet == false) {
        	setStaticUniforms();
        	m_staticUniformsSet = true;
        }
        setStandardUniforms();
        setDynamicUniforms(); 
	}
	
	public abstract void deUse();

	protected void setStandardUniforms() {
		if (m_model != null) {
			GL.matrix2uniform(m_model, m_modelLoc);
		}
		if (m_modelIT != null) {
			GL.matrix2uniform(m_modelIT, m_modelITLoc);
		}
		if (m_viewProj != null) {
			GL.matrix2uniform(m_viewProj, m_viewProjLoc);
		}
	}
	
	public void setModelMatrix(Matrix4f model) {
		m_model = model;
	}
	
	public void setModelITMatrix(Matrix4f modelIT) {
		m_modelIT = modelIT;
	}
	
	public void setViewProj(Matrix4f viewProj) {
		m_viewProj = viewProj;
	}
	
	public void setStandardMatrices(Matrix4f model, Matrix4f modelIT, Matrix4f viewProj) {
		m_model = model;
		m_modelIT = modelIT;
		m_viewProj = viewProj;
	}
	
    /**
     * Erzeugt ein ShaderProgram aus einem Vertex- und Fragmentshader.
     * @param vs Pfad zum Vertexshader
     * @param fs Pfad zum Fragmentshader
     * @return ShaderProgram ID
     */
    protected void createShaderProgram(String vs, String fs) {
    	m_programId = glCreateProgram();
        
        int vsID = glCreateShader(GL_VERTEX_SHADER);
        int fsID = glCreateShader(GL_FRAGMENT_SHADER);
        
        glAttachShader(m_programId, vsID);
        glAttachShader(m_programId, fsID);
        
        String vertexShaderContents = Util.getFileContents(vs);
        String fragmentShaderContents = Util.getFileContents(fs);
        
        glShaderSource(vsID, vertexShaderContents);
        glShaderSource(fsID, fragmentShaderContents);
        
        glCompileShader(vsID);
        glCompileShader(fsID);
        
        String log;
        log = glGetShaderInfoLog(vsID, 1024);
        System.out.print(log);
        log = glGetShaderInfoLog(fsID, 1024);
        System.out.print(log);
        
        glBindAttribLocation(m_programId, ATTR_POS, "positionMC");
        glBindAttribLocation(m_programId, ATTR_NORMAL, "normalMC");        
        glBindAttribLocation(m_programId, ATTR_COLOR, "vertexColor");
        glBindAttribLocation(m_programId, ATTR_COLOR2, "vertexColor2");
        glBindAttribLocation(m_programId, ATTR_TEX, "vertexTexCoords");
        
        glLinkProgram(m_programId);
        
        glUseProgram(m_programId);        
        m_modelLoc = glGetUniformLocation(m_programId, "model");
        m_viewProjLoc = glGetUniformLocation(m_programId, "viewProj");
        m_modelITLoc = glGetUniformLocation(m_programId, "modelIT");
        
        log = glGetProgramInfoLog(m_programId, 1024);
        System.out.print(log);
    }  
	
	
}
