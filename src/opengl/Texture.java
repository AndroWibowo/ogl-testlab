package opengl;

import static opengl.GL.GL_FLOAT;
import static opengl.GL.GL_R8;
import static opengl.GL.GL_RED;
import static opengl.GL.GL_RG;
import static opengl.GL.GL_RG8;
import static opengl.GL.GL_RGB;
import static opengl.GL.GL_RGB8;
import static opengl.GL.GL_RGBA;
import static opengl.GL.GL_RGBA8;
import static opengl.GL.GL_TEXTURE_2D;
import static opengl.GL.glBindTexture;
import static opengl.GL.glGenTextures;
import static opengl.GL.glGenerateMipmap;
import static opengl.GL.glTexImage2D;
import util.Util;
import util.Util.ImageContents;


public class Texture {
	
	private int m_handle;
	
	private int m_slot;
	
	private String m_fileName;
	
	public Texture () {
		
	}
	
	public void init(String fileName) {
		m_fileName = fileName;
		m_handle = generateTexture(m_fileName);
	}
	
	public void setActive () {
		GL.glActiveTexture(GL.GL_TEXTURE0 + m_slot);
		GL.glBindTexture(GL.GL_TEXTURE_2D, m_handle);
//		System.out.println("SLot: " + m_slot + "  Handle: " + m_handle); 
	}
	
	public int getHandle() {
		return m_handle;
	}
	
	public void setSlot(int slot) {
		m_slot = slot;
	}
	
    
    /**
     * Laedt eine Bilddatei und erzeugt daraus eine OpenGL Textur
     * @param filename Pfad zu einer Bilddatei
     * @return ID der erstellten Textur
     */
    private int generateTexture(String filename) {
        ImageContents contents = Util.loadImage(filename);
        int internalFormat = 0, format = 0;
        switch(contents.colorComponents) {
            case 1: internalFormat = GL_R8; format = GL_RED; break;
            case 2: internalFormat = GL_RG8; format = GL_RG; break;
            case 3: internalFormat = GL_RGB8; format = GL_RGB; break;
            case 4: internalFormat = GL_RGBA8; format = GL_RGBA; break;
            default: throw new UnsupportedOperationException("illegal cout of color components");
        }
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, contents.width, contents.height, 0, format, GL_FLOAT, contents.data);
        glGenerateMipmap(GL_TEXTURE_2D);
        return texture;
    }

}
