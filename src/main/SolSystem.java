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
import util.Util;

/**
 *
 * @author Sascha Kolodzey, Nico Marniok
 */
public class SolSystem extends Main {
    private static final int MAX_ORBS = 8;
    
    // shader programs

    
    private OrbShader orbShader;
    
    private FragmentLightingShader flShader;

    
    // scene data
    private Geometry earth = null;
    private Geometry moon = null;
    private Geometry clouds = null;
    private int earthFineness = 0;
    private RadiantOrb orbs[] = new RadiantOrb[MAX_ORBS];

    // current configurations
    private boolean bContinue = true;
    private boolean culling = true;
    private boolean wireframe = true;
    
    // control

    private final Camera cam = new Camera();
    private final Input input = new Input(cam);
    
    // animation params
    private float ingameTime = 0;
    private float ingameTimePerSecond = 1.0f;
    
    // uniform data
    private final Matrix4f earthModelMatrix = new Matrix4f();
    private final Matrix4f moonModelMatrix = new Matrix4f();
    private final Matrix4f cloudsModelMatrix = new Matrix4f();
    private final Vector3f inverseLightDirection = new Vector3f();
    private Texture earthTexture;
    private Texture earthSpecularTexture;
    private Texture moonTexture;
    private Texture cloudsTexture;
    
    // temp data
    private final Matrix4f moonRotation = new Matrix4f();
    private final Matrix4f moonTilt = new Matrix4f();
    private final Matrix4f moonTranslation = new Matrix4f();
    
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
    	flShader = new FragmentLightingShader();
        
        flShader.setViewProj(cam.getViewProjMatrix());
                   
        flShader.setEyePosition(cam.getCamPos());
        flShader.setKA(0.05f);
        flShader.setKD(0.8f);
        flShader.setKS(0.3f);
        flShader.setES(16.0f);
        flShader.setCA(1.0f, 1.0f, 1.0f);
        flShader.init();

        
        for(int i=0; i < MAX_ORBS; ++i) {
        	orbs[i] = new RadiantOrb();
        	orbs[i].setRadius(0.2f);
        	orbs[i].setOrbitRadius(1.25f + (float)Math.random());
        	orbs[i].setOrbitTilt(Util.PI_DIV4 - (float)Math.random() * Util.PI_DIV2);
        	orbs[i].setSpeed((float)Math.random());
        	orbs[i].setColor(new Vector3f((float)Math.random(), (float)Math.random(), (float)Math.random()));
        	flShader.setLightColor(i, orbs[i].getColor());
        	flShader.setLightPosition(i, orbs[i].getPosition());
        }

        orbShader = new OrbShader();
        orbShader.setViewProj(cam.getViewProjMatrix());
        orbShader.init();
        
        inverseLightDirection.set(1.0f, 0.2f, 0.0f);
        inverseLightDirection.normalise();
        
        earthTexture = new Texture();
        earthTexture.init("earth.jpg");
        earthSpecularTexture = new Texture();
        earthSpecularTexture.init("earth_spec.jpg");
        moonTexture = new Texture();
        moonTexture.init("moon.jpg");
        cloudsTexture = new Texture();
        cloudsTexture.init("clouds.jpg");
        
        
        cam.move(-5.0f, 0.0f, 0.0f);
        changeFineness(32);
        
        Util.translationX(3.0f, moonTranslation);
        Util.rotationX((float)Math.toRadians(15.0), moonTilt);
        
        input.setMainProgram(this);
    }
    
    public void render() throws LWJGLException {
        glClearColor(0.1f, 0.0f, 0.0f, 1.0f); // background color: dark red
        
        long last = System.currentTimeMillis();
        long now, millis;
        long frameTimeDelta = 0;
        int frames = 0;
        while(bContinue && !Display.isCloseRequested()) {
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
            input.handleInput(millis);
            animate(millis);
            
            // clear screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            // earth
            flShader.setModelMatrix(earthModelMatrix);
            flShader.setModelITMatrix(earthModelMatrix);
            
            flShader.setDiffuseTexture(earthTexture);
            flShader.setSpecularTexture(earthSpecularTexture);
            flShader.use();
            earth.draw();

            // moon
            flShader.setModelMatrix(moonModelMatrix);
            flShader.setModelITMatrix(moonModelMatrix);
            
            flShader.setDiffuseTexture(moonTexture);
            flShader.setSpecularTexture(null);
            flShader.use();
            moon.draw();
            
            // clouds
            flShader.setModelMatrix(cloudsModelMatrix);
            flShader.setModelITMatrix(cloudsModelMatrix);
            flShader.setDiffuseTexture(cloudsTexture);
            flShader.setSpecularTexture(null);
            flShader.use();
            
            glEnable(GL_BLEND);
            clouds.draw();
            glDisable(GL_BLEND);

            // orbs
            for(int i=0; i < MAX_ORBS; ++i) {
            	orbShader.setModelMatrix(orbs[i].getModel());
            	orbShader.setColor(orbs[i].getColor());
            	orbShader.use();
                orbs[i].draw();
            } 
            
            // present screen
            Display.update();
            Display.sync(60);
        }
    }
    
    @Override
    public void specialInput(int key) {
    	switch (key) {
	    	case Keyboard.KEY_UP: changeFineness(2 * earthFineness); break;
	        case Keyboard.KEY_DOWN: changeFineness(earthFineness / 2); break;
	        case Keyboard.KEY_LEFT:
	            if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
	                ingameTimePerSecond = 0.0f;
	            } else {
	                ingameTimePerSecond = Math.max(1.0f / 64.0f, 0.5f * ingameTimePerSecond);
	            }
	            break;
	        case Keyboard.KEY_RIGHT:
	            if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
	                ingameTimePerSecond = 1.0f;
	            } else {
	                ingameTimePerSecond = Math.min(64.0f, 2.0f * ingameTimePerSecond);
	            }
	            break;
	        case Keyboard.KEY_F2: glPolygonMode(GL_FRONT_AND_BACK, (wireframe ^= true) ? GL_FILL : GL_LINE); break;
	        case Keyboard.KEY_F3: if(culling ^= true) glEnable(GL_CULL_FACE); else glDisable(GL_CULL_FACE); break;
	        case Keyboard.KEY_ESCAPE: bContinue = false;
    	}
    }
    
    /**
     * Aktualisiert Model Matrizen der Erde und des Mondes.
     * @param millis Millisekunden, die seit dem letzten Aufruf vergangen sind.
     */
    private void animate(long millis) {
        // update ingame time properly
        ingameTime += ingameTimePerSecond * 1e-3f * (float)millis;
        
        // earth
        float earthRotationAngle = Util.PI_MUL2 * ingameTime;
        Util.rotationY(earthRotationAngle, earthModelMatrix);
        
        // clouds
        float cloudsRotationAngle = earthRotationAngle * 0.7f;
        Util.rotationY(cloudsRotationAngle, cloudsModelMatrix);
        
        // moon
        float moonRotationAngle = earthRotationAngle / 27.0f;
        Util.rotationY(moonRotationAngle, moonRotation);
        Util.mul(moonModelMatrix, moonTilt, moonRotation, moonTranslation);
        
        // orbs
        for(int i=0; i < MAX_ORBS; ++i) {
            orbs[i].animate(millis);
        }
    }
    
    /**
     * Aendert die Feinheit der Kugelannaeherung der Erde und des Mondes.
     * @param newFineness die neue Feinheit
     */
    private void changeFineness(int newFineness) {
        if(newFineness >= 4 && newFineness <= 8192) {
            if(earth != null) {
                earth.delete();
            }
            if(moon != null) {
                moon.delete();
            }
            earth = GeometryFactory.createSphereDisplaced(1.0f, newFineness, newFineness/2, 0.1f, "earth_height.jpg");
            clouds = GeometryFactory.createSphere(1.09f, newFineness/2, newFineness/4);
            moon = GeometryFactory.createSphere(0.5f, newFineness/2, newFineness/4);
            earthFineness = newFineness;
        }
    

}
}
