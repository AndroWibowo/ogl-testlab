package util;

import opengl.Texture;

import org.lwjgl.util.vector.Matrix4f;

public class Mesh {

	private String m_resource;
	
	private String m_name;
	
	private Geometry m_geo;
	
	private Matrix4f m_modelMatrix;
	
	private Matrix4f m_modelITMatrix;
	
	private Texture m_diffuseTexture;
	
	private Texture m_specularTexture;
	
	public Mesh(String name) {
		setName(name);
	}
	
	public void init() {
		m_modelMatrix = new Matrix4f();
//		m_modelITMatrix = new Matrix4f();
	}
	
	public void setDiffuseTexture(String fileName) {
        m_diffuseTexture = new Texture();
        m_diffuseTexture.init(fileName);
	}
	
	public Texture getDiffuseTexture() {
		return m_diffuseTexture;
	}

	public void setSpecularTexture(String fileName) {
        m_specularTexture = new Texture();
        m_specularTexture.init(fileName);
	}
	
	public Texture getSpecularTexture() {
		return m_specularTexture;
	}

	public Geometry getGeometry() {
		return m_geo;
	}

	public void setGeometry(Geometry m_geo) {
		this.m_geo = m_geo;
	}

	public Matrix4f getModelMatrix() {
		return m_modelMatrix;
	}

	public void setModelMatrix(Matrix4f m_modelMatrix) {
		this.m_modelMatrix = m_modelMatrix;
	}

	public Matrix4f getModelITMatrix() {
		return m_modelITMatrix;
	}

	public void setModelITMatrix(Matrix4f m_modelITMatrix) {
		this.m_modelITMatrix = m_modelITMatrix;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String m_name) {
		this.m_name = m_name;
	}
	
	public void draw() {
		m_geo.draw();
	}
}
