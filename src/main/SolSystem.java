/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import static opengl.GL.GL_BACK;
import static opengl.GL.GL_BLEND;
import static opengl.GL.GL_CCW;
import static opengl.GL.GL_COLOR_BUFFER_BIT;
import static opengl.GL.GL_CULL_FACE;
import static opengl.GL.GL_DEPTH_BUFFER_BIT;
import static opengl.GL.GL_DEPTH_TEST;
import static opengl.GL.GL_FILL;
import static opengl.GL.GL_FRONT_AND_BACK;
import static opengl.GL.GL_LINE;
import static opengl.GL.GL_ONE;
import static opengl.GL.GL_ONE_MINUS_SRC_COLOR;
import static opengl.GL.destroy;
import static opengl.GL.glBlendFunc;
import static opengl.GL.glClear;
import static opengl.GL.glClearColor;
import static opengl.GL.glCullFace;
import static opengl.GL.glDisable;
import static opengl.GL.glEnable;
import static opengl.GL.glFrontFace;
import static opengl.GL.glPolygonMode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.solsystem.RadiantOrb;
import opengl.GL;
import opengl.Texture;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import shader.solsystem.FragmentLightingShader;
import shader.solsystem.OrbShader;
import util.Camera;
import util.Geometry;
import util.GeometryFactory;
import util.Input;
import util.Mesh;
import util.Util;

/**
 *
 * @author Sascha Kolodzey, Nico Marniok
 */
public class SolSystem extends Main {
    private static final int MAX_ORBS = 8;
    
    // shader programs

    
    private OrbShader m_orbShader;
    
    private FragmentLightingShader m_flShader;

    
    // scene data
    private Geometry m_earth = null;
    private Geometry m_moon = null;
    private Geometry m_clouds = null;
    private int m_earthFineness = 0;
    private RadiantOrb m_orbs[] = new RadiantOrb[MAX_ORBS];

    // current configurations
    private boolean m_bContinue = true;
    private boolean m_culling = true;
    private boolean m_wireframe = true;
    
    private List<Mesh> m_actors = new ArrayList<Mesh>();
    
    // control
    private final Camera m_cam = new Camera();
    private final Input m_input = new Input(m_cam);
    
    // animation params
    private float m_ingameTime = 0;
    private float m_ingameTimePerSecond = 1.0f;
    
    // uniform data
    private final Vector3f m_inverseLightDirection = new Vector3f();
    
    // temp data
    private final Matrix4f m_moonRotation = new Matrix4f();
    private final Matrix4f m_moonTilt = new Matrix4f();
    private final Matrix4f m_moonTranslation = new Matrix4f();
    
    public static void main(String[] argv) {
        try {
            GL.init();
//            OpenCL.init();
            
            glEnable(GL_CULL_FACE);
            glFrontFace(GL_CCW);
            glCullFace(GL_BACK);
            glEnable(GL_DEPTH_TEST);
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_COLOR);
            
            SolSystem testLab = new SolSystem();
            
            testLab.init();            
            testLab.render();
            
            destroy();
        } catch (LWJGLException ex) {
            Logger.getLogger(SolSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void init() {
    	m_flShader = new FragmentLightingShader("FragmentLightingShader");
        
        m_flShader.setViewProj(m_cam.getViewProjMatrix());
                   
        m_flShader.setEyePosition(m_cam.getCamPos());
        m_flShader.setKA(0.05f);
        m_flShader.setKD(0.8f);
        m_flShader.setKS(0.3f);
        m_flShader.setES(16.0f);
        m_flShader.setCA(1.0f, 1.0f, 1.0f);
        m_flShader.init();

        
        for(int i=0; i < MAX_ORBS; ++i) {
        	m_orbs[i] = new RadiantOrb();
        	m_orbs[i].setRadius(0.2f);
        	m_orbs[i].setOrbitRadius(1.25f + (float)Math.random());
        	m_orbs[i].setOrbitTilt(Util.PI_DIV4 - (float)Math.random() * Util.PI_DIV2);
        	m_orbs[i].setSpeed((float)Math.random());
        	m_orbs[i].setColor(new Vector3f((float)Math.random(), (float)Math.random(), (float)Math.random()));
        	m_flShader.setLightColor(i, m_orbs[i].getColor());
        	m_flShader.setLightPosition(i, m_orbs[i].getPosition());
        }

        m_orbShader = new OrbShader("OrbShader");
        m_orbShader.setViewProj(m_cam.getViewProjMatrix());
        m_orbShader.init();
        
        m_inverseLightDirection.set(1.0f, 0.2f, 0.0f);
        m_inverseLightDirection.normalise();
        
        changeFineness(32);

        Mesh earth = new Mesh("earth");
        earth.init();
        earth.setGeometry(m_earth);
        earth.setDiffuseTexture("earth.jpg");
        earth.setSpecularTexture("earth_spec.jpg");
        Mesh moon = new Mesh("moon");
        moon.init();
        moon.setGeometry(m_moon);
        moon.setDiffuseTexture("moon.jpg");
        Mesh clouds = new Mesh("clouds");
        clouds.init();
        clouds.setGeometry(m_clouds);
        clouds.setDiffuseTexture("clouds.jpg");

        m_actors.add(earth);
        m_actors.add(clouds);
        m_actors.add(moon);
        
        m_cam.move(-5.0f, 0.0f, 0.0f);
        
        Util.translationX(3.0f, m_moonTranslation);
        Util.rotationX((float)Math.toRadians(15.0), m_moonTilt);
        
        m_input.setMainProgram(this);
    }
    
    public void render() throws LWJGLException {
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
            animate(millis);
            
            // clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            for (Mesh actor : m_actors) {
	            m_flShader.setModelMatrix(actor.getModelMatrix());
	            m_flShader.setModelITMatrix(actor.getModelITMatrix());
	            
	            m_flShader.setDiffuseTexture(actor.getDiffuseTexture());
	            m_flShader.setSpecularTexture(actor.getSpecularTexture());
	            m_flShader.use();
	            
	            if (actor.getName().equals("clouds")) {
	            	glEnable(GL_BLEND);
	            }
	            actor.draw();
	            if (actor.getName().equals("clouds")) {
	            	glDisable(GL_BLEND);
	            }
            }

            // orbs
            for(int i=0; i < MAX_ORBS; ++i) {
            	m_orbShader.setModelMatrix(m_orbs[i].getModel());
            	m_orbShader.setColor(m_orbs[i].getColor());
            	m_orbShader.use();
                m_orbs[i].draw();
            } 
            
            // present screen
            Display.update();
            Display.sync(60);
        }
    }
    
    @Override
    public void specialInput(int key) {
    	switch (key) {
	    	case Keyboard.KEY_UP: changeFineness(2 * m_earthFineness); break;
	        case Keyboard.KEY_DOWN: changeFineness(m_earthFineness / 2); break;
	        case Keyboard.KEY_LEFT:
	            if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
	                m_ingameTimePerSecond = 0.0f;
	            } else {
	                m_ingameTimePerSecond = Math.max(1.0f / 64.0f, 0.5f * m_ingameTimePerSecond);
	            }
	            break;
	        case Keyboard.KEY_RIGHT:
	            if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
	                m_ingameTimePerSecond = 1.0f;
	            } else {
	                m_ingameTimePerSecond = Math.min(64.0f, 2.0f * m_ingameTimePerSecond);
	            }
	            break;
	        case Keyboard.KEY_F2: glPolygonMode(GL_FRONT_AND_BACK, (m_wireframe ^= true) ? GL_FILL : GL_LINE); break;
	        case Keyboard.KEY_F3: if(m_culling ^= true) glEnable(GL_CULL_FACE); else glDisable(GL_CULL_FACE); break;
	        case Keyboard.KEY_ESCAPE: m_bContinue = false;
    	}
    }
    
    /**
     * Aktualisiert Model Matrizen der Erde und des Mondes.
     * @param millis Millisekunden, die seit dem letzten Aufruf vergangen sind.
     */
    private void animate(long millis) {
        // update ingame time properly
    	
        m_ingameTime += m_ingameTimePerSecond * 1e-3f * (float)millis;
        float earthRotationAngle = Util.PI_MUL2 * m_ingameTime;
        float cloudsRotationAngle = earthRotationAngle * 0.7f;
        float moonRotationAngle = earthRotationAngle / 27.0f;
        
        for (Mesh actor : m_actors) {
        	String actorName = actor.getName();
        	if (actorName.equals("earth")) {
	        		Util.rotationY(earthRotationAngle, actor.getModelMatrix());
        	} else if (actorName.equals("clouds")) {
	        		Util.rotationY(cloudsRotationAngle, actor.getModelMatrix());
        	} else if (actorName.equals("moon")) {
	        		Util.rotationY(moonRotationAngle, m_moonRotation);
	        		Util.mul(actor.getModelMatrix(), m_moonTilt, m_moonRotation, m_moonTranslation);
        	}

        	for(int i=0; i < MAX_ORBS; ++i) {
	            m_orbs[i].animate(millis);
	        }
	        
        }
    }
    
    /**
     * Aendert die Feinheit der Kugelannaeherung der Erde und des Mondes.
     * @param newFineness die neue Feinheit
     */
    private void changeFineness(int newFineness) {
        if(newFineness >= 4 && newFineness <= 8192) {
            if(m_earth != null) {
                m_earth.delete();
            }
            if(m_moon != null) {
                m_moon.delete();
            }
            m_earth = GeometryFactory.createSphereDisplaced(1.0f, newFineness, newFineness/2, 0.1f, "earth_height.jpg");
            m_clouds = GeometryFactory.createSphere(1.09f, newFineness/2, newFineness/4);
            m_moon = GeometryFactory.createSphere(0.5f, newFineness/2, newFineness/4);
            m_earthFineness = newFineness;
        }
    

}
}
