/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.solsystem;

import static opengl.GL.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import util.Geometry;
import util.GeometryFactory;
import util.Util;

/**
 *
 * @author NMARNIOK
 */
public class RadiantOrb {
    private static final Geometry geo = GeometryFactory.createSphere(1.0f, 32, 16);
    
    private final Matrix4f model = new Matrix4f();
    private final Vector3f position = new Vector3f();
    private final Vector3f color = new Vector3f();
    private float radius = 1.0f;
    private float orbitRadius = 0.0f;
    private float orbitAngle = 0.0f;
    private float orbitTilt = 0.0f;
    private float speed = 0.0f;
    
    public void setColor(Vector3f color) {
        this.color.set(color);
    }
    
    public Vector3f getColor() {
    	return this.color;
    }
    
    public Vector3f getPosition() {
    	return this.position;
    }
    
    public Matrix4f getModel() {
    	return this.model;
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setOrbitRadius(float orbitRadius) {
        this.orbitRadius = orbitRadius;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setOrbitTilt(float orbitTilt) {
        this.orbitTilt = orbitTilt;
    }
    
    public void animate(long millis) {
        this.orbitAngle += 1e-3f * (float)millis * this.speed;
        this.buildModelMatrix();
    }
    
    private void buildModelMatrix() {
        Util.mul(this.model, Util.rotationY(this.orbitAngle, null), Util.rotationX(this.orbitTilt, null), Util.translationZ(this.orbitRadius, null), Util.scale(this.radius, null));
        Util.transformCoord(this.model, new Vector3f(), this.position);
    }
    
    public void draw() {
        RadiantOrb.geo.draw();
    }
}
