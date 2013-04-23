package main;

import static opengl.GL.GL_BACK;
import static opengl.GL.GL_COLOR_BUFFER_BIT;
import static opengl.GL.GL_CULL_FACE;
import static opengl.GL.GL_DEPTH_BUFFER_BIT;
import static opengl.GL.GL_DEPTH_TEST;
import static opengl.GL.GL_ONE;
import static opengl.GL.GL_ONE_MINUS_SRC_COLOR;
import static opengl.GL.destroy;
import static opengl.GL.glBlendFunc;
import static opengl.GL.glClear;
import static opengl.GL.glClearColor;
import static opengl.GL.glCullFace;
import static opengl.GL.glEnable;
import static opengl.GL.glFrontFace;

import java.util.logging.Level;
import java.util.logging.Logger;

import opengl.GL;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import shader.brickshader.BrickShader;
import util.Camera;
import util.GeometryFactory;
import util.Input;
import util.Mesh;
import util.Util;

public class Chapter6_BrickShader extends Main {

	private Mesh m_wall;
	
	private BrickShader m_brickShader;
	
    // current configurations
    private boolean m_bContinue = true;
    
    // control
    private final Camera m_cam = new Camera();
    private final Input m_input = new Input(m_cam);
	
	public static void main(String[] args) {
        try {
            GL.init();
            
            glEnable(GL_CULL_FACE);
            glFrontFace(GL.GL_CCW);
//            glFrontFace(GL.GL_CCW);
            glCullFace(GL_BACK);
            glEnable(GL_DEPTH_TEST);
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_COLOR);
            
            Chapter6_BrickShader brickShader = new Chapter6_BrickShader();
            
            brickShader.init();            
            brickShader.render();
            
            destroy();
        } catch (LWJGLException ex) {
            Logger.getLogger(SolSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	@Override
	public void specialInput(int key) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void init() {
        m_wall = new Mesh("Brick");
        m_wall.init();
        m_wall.setGeometry(GeometryFactory.createQuad());
//        m_wall.setGeometry(GeometryFactory.createSphere(3, 100, 150));
        
        m_brickShader = new BrickShader("BrickShader");
        m_brickShader.setView(m_cam.getView());
        m_brickShader.setProjection(m_cam.getProjection());
        
      
        m_brickShader.setBrickColor(1.0f, 0.0f, 0.0f);
    	
        m_brickShader.setMortarColor(0.0f, 1.0f, 0.0f);
    	
        m_brickShader.setLightPosition(0.5f, 0.5f, 0.5f);
    	
        m_brickShader.setBrickSize(0.5f, 0.2f);
    	
        m_brickShader.setBrickPct(0.95f, 0.9f);
        
        m_brickShader.init();
	}

	@Override
	protected void render() {
		// TODO Auto-generated method stub
glClearColor(0.1f, 0.0f, 0.0f, 1.0f); // background color: dark red
        
        long last = System.currentTimeMillis();
        long now, millis;
        long frameTimeDelta = 0;
        int frames = 0;
        while(m_bContinue && !Display.isCloseRequested()) {
            // time handling
            now = System.currentTimeMillis();
            millis = now - last;
            last = now;     
            frameTimeDelta += millis;
            ++frames;
            if(frameTimeDelta > 1000) {
                System.out.println(1e3f * (float)frames / (float)frameTimeDelta + " FPS");
                frameTimeDelta -= 1000;
                frames = 0;
            }
            
            // input and animation
            m_input.handleInput(millis);
            
            // clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
        	m_brickShader.setModelMatrix(m_wall.getModelMatrix());
        	m_brickShader.setModelITMatrix(m_wall.getModelITMatrix());

        	//m_brickShader.setDiffuseTexture(m_wall.getDiffuseTexture());
        	//m_brickShader.setSpecularTexture(m_wall.getSpecularTexture());
        	
        	Vector3f lightPos = new Vector3f(2.0f, 0.0f, -2.0f);
        	Util.transformCoord(m_cam.getView(), lightPos, lightPos);
            m_brickShader.setLightPosition(lightPos.x, lightPos.y, lightPos.z);

        	
        	m_brickShader.use();
            
        	m_wall.draw();
            
            // present screen
            Display.update();
            Display.sync(60);
        }
	}

}
