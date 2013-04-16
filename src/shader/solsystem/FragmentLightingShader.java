package shader.solsystem;

import static opengl.GL.GL_TEXTURE0;
import static opengl.GL.glActiveTexture;
import static opengl.GL.glBindTexture;
import static opengl.GL.glGetUniformLocation;
import static opengl.GL.glUniform1f;
import static opengl.GL.glUniform1i;
import static opengl.GL.glUniform3f;

import opengl.GL;
import opengl.Texture;

import org.lwjgl.util.vector.Vector3f;

import shader.ShaderProgram;


public class FragmentLightingShader extends ShaderProgram {

	private final int MAX_LIGHTS = 8;

	private int m_flDiffuseTexLoc;
    private int m_flSpecularTexLoc;
    private int m_flEyePositionLoc;
    private int m_flkaLoc;
    private int m_flkdLoc;
    private int m_flksLoc;
    private int m_flesLoc;
    private int m_flcaLoc;
    private int m_plIntensityLocs[] = new int[MAX_LIGHTS];
    private int m_plPositionLocs[] = new int[MAX_LIGHTS];
    
    private Vector3f m_flEyePosition;
    private float m_flka;
    private float m_flkd;
    private float m_flks;
    private float m_fles;
    private Vector3f m_flca;
    
    
    private Vector3f m_plLightColors[] = new Vector3f[MAX_LIGHTS];
    private Vector3f m_plLightPositions[] = new Vector3f[MAX_LIGHTS];
    
    private Texture m_diffuseTex;
    private Texture m_specularTex;
	
	public FragmentLightingShader(String name) {
		super(name);
	}
    
	@Override
	public void init() {
		this.createShaderProgram("./shader/FragmentLighting_VS.glsl", "./shader/FragmentLighting_FS.glsl");
		
        m_flDiffuseTexLoc = glGetUniformLocation(m_programId, "diffuseTex");
        m_flSpecularTexLoc = glGetUniformLocation(m_programId, "specularTex");
        m_flEyePositionLoc = glGetUniformLocation(m_programId, "eyePosition");
        m_flkaLoc = glGetUniformLocation(m_programId, "k_a");
        m_flkdLoc = glGetUniformLocation(m_programId, "k_dif");
        m_flksLoc = glGetUniformLocation(m_programId, "k_spec");
        m_flesLoc = glGetUniformLocation(m_programId, "es");
        m_flcaLoc = glGetUniformLocation(m_programId, "c_a");
        
        for (int i = 0; i < MAX_LIGHTS; i++) {
        	m_plIntensityLocs[i] = glGetUniformLocation(m_programId, "plMaxIntensity[" + i + "]");
        }
		
        for (int i = 0; i < MAX_LIGHTS; i++) {
        	m_plPositionLocs[i] = glGetUniformLocation(m_programId, "plPosition[" + i + "]");
        }
	}
	
	public void setEyePosition(Vector3f position) {
		m_flEyePosition = position;
	}
	
	public void setKA(float ka) {
		m_flka = ka;
	}
	
	public void setKD(float kd) {
		m_flkd = kd;
	}
	
	public void setKS(float ks) {
		m_flks = ks;
	}
	
	public void setES(float es) {
		m_fles = es;
	}
	
	public void setCA(Vector3f ca) {
		m_flca = ca;
	}

	public void setCA(float x, float y, float z) {
		m_flca = new Vector3f();
		m_flca.x = x;
		m_flca.y = y;
		m_flca.z = z;
	}
	
	public void setDiffuseTexture(Texture diffuse) {
		m_diffuseTex = diffuse;
		m_diffuseTex.setSlot(1);
	}
	
	public void setSpecularTexture(Texture specular) {
		m_specularTex = specular;
		if (m_specularTex != null) {
			m_specularTex.setSlot(2);
		}
	}
	
	public void setLightPosition(int slot, float x, float y, float z) {
		this.m_plLightPositions[slot].x = x;
		this.m_plLightPositions[slot].y = y;
		this.m_plLightPositions[slot].z = y;
	}
	
	public void setLightPosition(int slot, Vector3f position) {
		this.m_plLightPositions[slot] = position;
	}

	public void setLightColors(int slot, float x, float y, float z) {
		this.m_plLightColors[slot].x = x;
		this.m_plLightColors[slot].y = y;
		this.m_plLightColors[slot].z = y;
	}
	
	public void setLightColor(int slot, Vector3f color) {
		this.m_plLightColors[slot] = color;
	}
	
	@Override
	protected void setStaticUniforms() {
        glUniform1f(m_flkaLoc, m_flka);
        glUniform1f(m_flkdLoc, m_flkd);
        glUniform1f(m_flksLoc, m_flks);
        glUniform1f(m_flesLoc, m_fles);
        glUniform3f(m_flcaLoc, m_flca.x, m_flca.y, m_flca.z);
        for (int i = 0; i < MAX_LIGHTS; i++) {
        	glUniform3f(m_plIntensityLocs[i], m_plLightColors[i].x, m_plLightColors[i].y, m_plLightColors[i].z);
        }
	}

	@Override
	protected void setDynamicUniforms() {
        glUniform3f(m_flEyePositionLoc, m_flEyePosition.x, m_flEyePosition.y, m_flEyePosition.z);
//        System.out.println("Loc: " + m_flEyePositionLoc + " (" + m_flEyePosition.x + ", " + m_flEyePosition.y + ", " + m_flEyePosition.z + ")");
        if (m_diffuseTex != null) {
        	m_diffuseTex.setActive();
        	GL.glUniform1i(m_flDiffuseTexLoc, 1);
        } else {
        	GL.glUniform1i(m_flDiffuseTexLoc, 0);
        }
        if (m_specularTex != null) {
        	m_specularTex.setActive();
        	GL.glUniform1i(m_flSpecularTexLoc, 2);
        } else {
        	GL.glUniform1i(m_flSpecularTexLoc, 0);
        }
        for (int i = 0; i < MAX_LIGHTS; i++) {
        	glUniform3f(m_plPositionLocs[i], m_plLightPositions[i].x, m_plLightPositions[i].y, m_plLightPositions[i].z);
        }
	}

	@Override
	public void deUse() {
		m_staticUniformsSet = false;
	}
	

}
