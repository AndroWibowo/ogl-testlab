package util;

import static opengl.GL.GL_ARRAY_BUFFER;
import static opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static opengl.GL.GL_FLOAT;
import static opengl.GL.GL_STATIC_DRAW;
import static opengl.GL.GL_TRIANGLE_STRIP;
import static opengl.GL.RESTART_INDEX;
import static opengl.GL.glBindBuffer;
import static opengl.GL.glBindVertexArray;
import static opengl.GL.glBufferData;
import static opengl.GL.glEnableVertexAttribArray;
import static opengl.GL.glGenBuffers;
import static opengl.GL.glGenVertexArrays;
import static opengl.GL.glVertexAttribPointer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import opengl.GL;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

import shader.ShaderProgram;

/**
 * Stellt Methoden zur Erzeugung von Geometrie bereit.
 * @author Sascha Kolodzey, Nico Marniok
 */
public class GeometryFactory {
    
    /**
     * Erzeugt eine Kugel.
     * @param r Radius der Kugel
     * @param n Anzahl der vertikalen Streifen
     * @param k Anzahl der horizontalen Streifen
     * @param imageFile Pfad zu einer Bilddatei
     * @return Geometrie der Kugel
     */
    public static Geometry createSphere(float r, int n, int k, String imageFile) {
        float[][][] image = Util.getImageContents(imageFile);
        
        FloatBuffer fb = BufferUtils.createFloatBuffer((3+3+4) * (n+1)*(k+1));
        
        float dTheta = Util.PI / (float)k;
        float dPhi = Util.PI_MUL2 / (float)n;
        float theta = 0;
        for(int j=0; j <= k; ++j) {
            float sinTheta = (float)Math.sin(theta);
            float cosTheta = (float)Math.cos(theta);
            float phi = 0;
            for(int i=0; i <= n; ++i) {
                float sinPhi = (float)Math.sin(phi);
                float cosPhi = (float)Math.cos(phi);
                
                // position
                fb.put(r*sinTheta*cosPhi);  
                fb.put(r*cosTheta);
                fb.put(r*sinTheta*sinPhi);
                
                // normal
                fb.put(sinTheta*cosPhi);    
                fb.put(cosTheta);
                fb.put(sinTheta*sinPhi);
                
                fb.put(image[(int)((theta / Util.PI) * (float)image.length) % image.length]
                            [(int)(phi / Util.PI_MUL2 * (float)image[0].length) % image[0].length]);
                fb.put(1.0f);
                
                phi += dPhi;
            }
            theta += dTheta;
        }
        fb.position(0);
        
        IntBuffer ib = BufferUtils.createIntBuffer(k*(2*(n+1)+1));
        for(int j=0; j < k; ++j) {
            for(int i=0; i <= n; ++i) {
                ib.put((j+1)*(n+1) + i);
                ib.put(j*(n+1) + i);
            }
            ib.put(RESTART_INDEX);
        }
        ib.position(0);
        
        Geometry sphere = new Geometry();
        sphere.setIndices(ib, GL_TRIANGLE_STRIP);
        sphere.setVertices(fb);
        sphere.addVertexAttribute(ShaderProgram.ATTR_POS, 3, 0);
        sphere.addVertexAttribute(ShaderProgram.ATTR_NORMAL, 3, 12);
        sphere.addVertexAttribute(ShaderProgram.ATTR_COLOR, 4, 24);
        return sphere;
    }
    
    /**
     * Erzeugt eine Kugel mit Texturekoordinaten und Normalen.
     * @param r Radius der Kugel
     * @param n Anzahl der vertikalen Streifen
     * @param k Anzahl der horizontalen Streifen
     * @return Geometrie der Kugel
     */
    public static Geometry createSphere(float r, int n, int k) {
        FloatBuffer fb = BufferUtils.createFloatBuffer((3+3+2) * (n+1)*(k+1));
        
        float dTheta = Util.PI / (float)k;
        float dPhi = Util.PI_MUL2 / (float)n;
        float theta = 0;
        for(int j=0; j <= k; ++j) {
            float sinTheta = (float)Math.sin(theta);
            float cosTheta = (float)Math.cos(theta);
            float phi = 0;
            for(int i=0; i <= n; ++i) {
                float sinPhi = (float)Math.sin(phi);
                float cosPhi = (float)Math.cos(phi);
                
                // position
                fb.put(r*sinTheta*cosPhi);  
                fb.put(r*cosTheta);
                fb.put(r*sinTheta*sinPhi);
                
                // normal
                fb.put(sinTheta*cosPhi);    
                fb.put(cosTheta);
                fb.put(sinTheta*sinPhi);
                
                // tex coords
                fb.put(phi / Util.PI_MUL2);
                fb.put(theta / Util.PI);
                                
                phi += dPhi;
            }
            theta += dTheta;
        }
        fb.position(0);
        
        IntBuffer ib = BufferUtils.createIntBuffer(k*(2*(n+1)+1));
        for(int j=0; j < k; ++j) {
            for(int i=0; i <= n; ++i) {
                ib.put((j+1)*(n+1) + i);
                ib.put(j*(n+1) + i);
            }
            ib.put(RESTART_INDEX);
        }
        ib.position(0);
        
        Geometry sphere = new Geometry();
        sphere.setIndices(ib, GL_TRIANGLE_STRIP);
        sphere.setVertices(fb);
        sphere.addVertexAttribute(ShaderProgram.ATTR_POS, 3, 0);
        sphere.addVertexAttribute(ShaderProgram.ATTR_NORMAL, 3, 12);
        sphere.addVertexAttribute(ShaderProgram.ATTR_TEX, 2, 24);
        return sphere;
    }   
    
    /**
     * Erzeugt eine Kugel mit Texturekoordinaten und Normalen, die Anhand einer
     * Displacementmap mit Hoeheninformationen behandelt wurde.
     * @param r Der Radius der Kugel
     * @param n Anzahl der vertikalen Streifen
     * @param k Anzahl der horizontalen Streifen
     * @param displaceHeight Maximale Verschiebung anhand der Normalen
     * @param pathToDisplaceImage Pfad zur Bilddatei, die als Displacementmap
     * verwendet werden soll
     * @return Geometrie der Kugel
     */
    public static Geometry createSphereDisplaced(float r, int n, int k, float displaceHeight, String pathToDisplaceImage) {
        float displaceData[][][] = Util.getImageContents(Util.IMAGE_PATH + pathToDisplaceImage);
        int vertexSize = 3+3+2;
        FloatBuffer fb = BufferUtils.createFloatBuffer(vertexSize * (n+1)*(k+1));
        
        float dTheta = Util.PI / (float)k;
        float dPhi = Util.PI_MUL2 / (float)n;
        float theta = 0;
        for(int j=0; j <= k; ++j) {
            float sinTheta = (float)Math.sin(theta);
            float cosTheta = (float)Math.cos(theta);
            float phi = 0;
            for(int i=0; i <= n; ++i) {
                float u = phi / Util.PI_MUL2;
                float v = theta / Util.PI;                        
                float height = displaceData[(int)(v * (float)displaceData.length) % displaceData.length][(int)(u * (float)displaceData[0].length) % displaceData[0].length][0];
                
                float sinPhi = (float)Math.sin(phi);
                float cosPhi = (float)Math.cos(phi);
                
                // position
                fb.put((r + height * displaceHeight)*sinTheta*cosPhi);  
                fb.put((r + height * displaceHeight)*cosTheta);
                fb.put((r + height * displaceHeight)*sinTheta*sinPhi);
                
                // normal
                fb.put(0.0f);    // later...
                fb.put(0.0f);
                fb.put(0.0f);
                
                // tex coords
                fb.put(1.0f - u);
                fb.put(v);
                                
                phi += dPhi;
            }
            theta += dTheta;
        }
        fb.position(0);
        
        IntBuffer ib = BufferUtils.createIntBuffer(k*(2*(n+1)+1));
        for(int j=0; j < k; ++j) {
            for(int i=0; i <= n; ++i) {
                ib.put((j+1)*(n+1) + i);
                ib.put(j*(n+1) + i);
            }
            ib.put(RESTART_INDEX);
        }
        ib.position(0);
        
        int i0 = ib.get(0);
        int i1 = ib.get(1);
        int i2 = ib.get(2);
        int marker = 0;
        for(int i=3; i < ib.capacity(); ++i) {
            Vector3f v0 = new Vector3f(fb.get(vertexSize * i0 + 0), fb.get(vertexSize * i0 + 1), fb.get(vertexSize * i0 + 2));
            Vector3f v1 = new Vector3f(fb.get(vertexSize * i1 + 0), fb.get(vertexSize * i1 + 1), fb.get(vertexSize * i1 + 2));
            Vector3f v2 = new Vector3f(fb.get(vertexSize * i2 + 0), fb.get(vertexSize * i2 + 1), fb.get(vertexSize * i2 + 2));
            
            Vector3f n0 = new Vector3f(fb.get(vertexSize * i0 + 3), fb.get(vertexSize * i0 + 4), fb.get(vertexSize * i0 + 5));
            Vector3f n1 = new Vector3f(fb.get(vertexSize * i1 + 3), fb.get(vertexSize * i1 + 4), fb.get(vertexSize * i1 + 5));
            Vector3f n2 = new Vector3f(fb.get(vertexSize * i2 + 3), fb.get(vertexSize * i2 + 4), fb.get(vertexSize * i2 + 5));
            
            Vector3f a = Vector3f.sub(v1, v0, null);
            Vector3f b = Vector3f.sub(v2, v0, null);
            
            Vector3f normal = Vector3f.cross(a, b, null);
            Vector3f.add(n0, normal, n0);
            Vector3f.add(n1, normal, n1);
            Vector3f.add(n2, normal, n2);
            
            fb.put(vertexSize * i0 + 3, normal.x);
            fb.put(vertexSize * i0 + 4, normal.y);
            fb.put(vertexSize * i0 + 5, normal.z);
            
            fb.put(vertexSize * i1 + 3, normal.x);
            fb.put(vertexSize * i1 + 4, normal.y);
            fb.put(vertexSize * i1 + 5, normal.z);
            
            fb.put(vertexSize * i2 + 3, normal.x);
            fb.put(vertexSize * i2 + 4, normal.y);
            fb.put(vertexSize * i2 + 5, normal.z);
            
            int newIndex = ib.get(i);
            if(newIndex == RESTART_INDEX && i+1 < ib.capacity()) {
                i0 = ib.get(++i);
                i1 = ib.get(++i);
                i2 = ib.get(++i);
                ++marker;
            } else {
                if((i + marker) % 2 == 0) { // triangle strip magic
                    i0 = i2;
                    i2 = newIndex;
                } else {
                    i0 = i1;
                    i1 = newIndex;
                }
            }
        }
        
        for(int i=0; i < fb.capacity(); i += vertexSize) {
            Vector3f normal = new Vector3f();
            fb.position(i + 3);
            normal.load(fb);
            if(normal.lengthSquared() != 0.0f) {
                normal.normalise();
                fb.put(i + 3, normal.x);
                fb.put(i + 4, normal.y);
                fb.put(i + 5, normal.z);
            }
        }
        fb.position(0);
        
        Geometry sphere = new Geometry();
        sphere.setIndices(ib, GL_TRIANGLE_STRIP);
        sphere.setVertices(fb);
        sphere.addVertexAttribute(ShaderProgram.ATTR_POS, 3, 0);
        sphere.addVertexAttribute(ShaderProgram.ATTR_NORMAL, 3, 12);
        sphere.addVertexAttribute(ShaderProgram.ATTR_TEX, 2, 24);
        return sphere;
    }   
    
    /**
     * Erzeugt eine Kugel.
     * @param r Radius der Kugel
     * @param n Anzahl der vertikalen Streifen
     * @param k Anzahl der horizontalen Streifen
     * @param dayImage Pfad zur Bilddatei bei Tag
     * @param nightImage Pfad zur Bilddatei bei Nacht
     * @return Geometrie der Kugel
     */
    public static Geometry createSphere(float r, int n, int k, String dayImage, String nightImage) {
        float[][][] day = Util.getImageContents(dayImage);
        float[][][] night = null;
        if(nightImage != null) {
            night = Util.getImageContents(nightImage);
        }
        
        FloatBuffer fb = BufferUtils.createFloatBuffer((3+3+4+4) * (n+1)*(k+1));
        
        float dTheta = Util.PI / (float)k;
        float dPhi = Util.PI_MUL2 / (float)n;
        float theta = 0;
        for(int j=0; j <= k; ++j) {
            float sinTheta = (float)Math.sin(theta);
            float cosTheta = (float)Math.cos(theta);
            float phi = 0;
            for(int i=0; i <= n; ++i) {
                float sinPhi = (float)Math.sin(phi);
                float cosPhi = (float)Math.cos(phi);
                
                // position
                fb.put(r*sinTheta*cosPhi);  
                fb.put(r*cosTheta);
                fb.put(r*sinTheta*sinPhi);
                
                // normal
                fb.put(sinTheta*cosPhi);    
                fb.put(cosTheta);
                fb.put(sinTheta*sinPhi);
                
                fb.put(day[(int)((theta / Util.PI) * (float)day.length) % day.length]
                          [(int)(phi / Util.PI_MUL2 * (float)day[0].length) % day[0].length]);
                fb.put(1.0f);
                
                if(nightImage == null) {
                    fb.put(new float[] { 0.0f, 0.0f, 0.0f, 1.0f });
                } else if(dayImage.equals(nightImage)) {
                    float color[] = night[(int)((theta / Util.PI) * (float)night.length) % night.length]
                                         [(int)(phi / Util.PI_MUL2 * (float)night[0].length) % night[0].length];
                    fb.put(0.1f * color[0]);
                    fb.put(0.1f * color[1]);
                    fb.put(0.1f * color[2]);
                    fb.put(1.0f);
                } else {
                    fb.put(night[(int)((theta / Util.PI) * (float)night.length) % night.length]
                                [(int)(phi / Util.PI_MUL2 * (float)night[0].length) % night[0].length]);
                    fb.put(1.0f);
                }
                
                phi += dPhi;
            }
            theta += dTheta;
        }
        fb.position(0);
        
        IntBuffer ib = BufferUtils.createIntBuffer(k*(2*(n+1)+1));
        for(int j=0; j < k; ++j) {
            for(int i=0; i <= n; ++i) {
                ib.put((j+1)*(n+1) + i);
                ib.put(j*(n+1) + i);
            }
            ib.put(RESTART_INDEX);
        }
        ib.position(0);
        
        Geometry sphere = new Geometry();
        sphere.setIndices(ib, GL_TRIANGLE_STRIP);
        sphere.setVertices(fb);
        sphere.addVertexAttribute(ShaderProgram.ATTR_POS, 3, 0);
        sphere.addVertexAttribute(ShaderProgram.ATTR_NORMAL, 3, 12);
        sphere.addVertexAttribute(ShaderProgram.ATTR_COLOR, 4, 24);
        sphere.addVertexAttribute(ShaderProgram.ATTR_COLOR2, 4, 40);
        return sphere;
    }
    
    /**
     * Erzeugt ein Vierexk in der xy-Ebene. (4 Indizes)
     * @return VertexArrayObject ID
     */
    public static Geometry createQuad() {        
        // vertexbuffer
        FloatBuffer vertexData = BufferUtils.createFloatBuffer((3+4)*4); // world coords, color
        vertexData.put(new float[] {
            -1.0f, -1.0f, 0.0f,  0.0f, 0.0f, -1.0f,
            +1.0f, -1.0f, 0.0f,  0.0f, 0.0f, -1.0f,
            +1.0f, +1.0f, 0.0f,  0.0f, 0.0f, -1.0f,
            -1.0f, +1.0f, 0.0f,  0.0f, 0.0f, -1.0f
        });
        vertexData.position(0);
        
        IntBuffer ib = BufferUtils.createIntBuffer(6);
        ib.put(2);
        ib.put(1);
        ib.put(0);
        ib.put(3);
        ib.put(2);
        ib.put(0);
        ib.position(0);
                
//        int vertexBufferID = glGenBuffers();
//        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
//        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);       
        
         // vs_in_pos  
//        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
//        glVertexAttribPointer(ShaderProgram.ATTR_POS, 3, GL_FLOAT, false, (3+2)*4, 0);       
        // vs_in_color
//        glEnableVertexAttribArray(ShaderProgram.ATTR_TEX);
//        glVertexAttribPointer(ShaderProgram.ATTR_COLOR, 2, GL_FLOAT, false, (3+2)*4, 3*4);
        
        Geometry quad = new Geometry();
        quad.setIndices(ib, GL.GL_TRIANGLES);
        quad.setVertices(vertexData);
        quad.addVertexAttribute(ShaderProgram.ATTR_POS, 3, 0);
        quad.addVertexAttribute(ShaderProgram.ATTR_NORMAL, 3, 12);
        
        return quad;
    }
    
    /**
     * Erzeugt ein Dreieck in der xy-Ebene. (3 Indizes)
     * @return VertexArrayObject ID
     */
    public static int createTriangle() {
        int vaid = glGenVertexArrays();
        glBindVertexArray(vaid);        
        
        // vertexbuffer
        FloatBuffer vertexData = BufferUtils.createFloatBuffer((3+4)*3); // color, world coords
        vertexData.put(new float[] {
            0.4f, 1.0f, 1.0f, 1.0f,  -1.0f, -1.0f, 0.0f,
            0.4f, 1.0f, 1.0f, 1.0f,  +1.0f, -1.0f, 0.0f,
            0.4f, 0.4f, 1.0f, 1.0f,   0.0f, +1.0f, 0.0f,
        });
        vertexData.position(0);
                
        int vertexBufferID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);       
        
        // vs_in_color
        glEnableVertexAttribArray(ShaderProgram.ATTR_COLOR);
        glVertexAttribPointer(ShaderProgram.ATTR_COLOR, 4, GL_FLOAT, false, (3+4)*4, 0);
         // vs_in_pos  
        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
        glVertexAttribPointer(ShaderProgram.ATTR_POS, 3, GL_FLOAT, false, (3+4)*4, 4*4);
        
        return vaid;        
    }
    
    /**
     * Erzeugt ein gleichmaessiges 2D n-Eck in der xy-Ebene. (n Indizes, als
     * GL_LINE_LOOP)
     * @param n Anzahl der Ecken
     * @return VertexArrayObject ID
     */
    public static int createNGon(int n) {        
        int vaid = glGenVertexArrays();
        glBindVertexArray(vaid);        
        
        // indexbuffer
        IntBuffer indexData = BufferUtils.createIntBuffer(n);
        for(int i=0; i < n; ++i) {
            indexData.put(i);
        }
        indexData.flip();
        
        int indexBufferID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW); 
        
        // vertexbuffer
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(3*n + 3*n); // world coords + normal coords
        double phi = 0;
        double deltaPhi = 2.0*Math.PI / (double)n;
        for(int i=0; i < n; ++i) {
            vertexData.put(0.5f*(float)Math.cos(phi));   // position x
            vertexData.put(0.5f*(float)Math.sin(phi));   // position y
            vertexData.put(0.5f*0.0f);                   // position z
            vertexData.put((float)Math.cos(phi));   // normal x
            vertexData.put((float)Math.sin(phi));   // normal y
            vertexData.put(0.0f);                   // normal z
            phi += deltaPhi;
        }
        vertexData.position(0);
                
        int vertexBufferID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferID);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);       
        
         // vs_in_pos  
        glEnableVertexAttribArray(ShaderProgram.ATTR_POS);
        glVertexAttribPointer(ShaderProgram.ATTR_POS, 3, GL_FLOAT, false, 24, 0);
         // vs_in_normal
        glEnableVertexAttribArray(ShaderProgram.ATTR_NORMAL);
        glVertexAttribPointer(ShaderProgram.ATTR_NORMAL, 3, GL_FLOAT, false, 24, 12);        
        
        return vaid;
    }
}
