package util;

import main.Main;
import main.SolSystem;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Input {

    private static final Vector3f m_moveDir = new Vector3f(0.0f, 0.0f, 0.0f);
    private final Camera m_cam;
    private Main m_mainProgram;
    
    public Main getMainProgram() {
		return m_mainProgram;
	}

	public void setMainProgram(Main m_mainProgram) {
		this.m_mainProgram = m_mainProgram;
	}

	public Input(Camera cam) {
    	m_cam = cam;
    }
    
    /**
     * Behandelt Input und setzt die Kamera entsprechend.
     * @param millis Millisekunden seit dem letzten Aufruf
     */
    public void handleInput(long millis) {
        float moveSpeed = 2e-3f*(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 2.0f : 1.0f)*(float)millis;
        float camSpeed = 5e-3f;
        
        while(Keyboard.next()) {
            if(Keyboard.getEventKeyState()) {
            	int eventKey = Keyboard.getEventKey();
                switch(eventKey) {
                    case Keyboard.KEY_W: m_moveDir.z += 1.0f; break;
                    case Keyboard.KEY_S: m_moveDir.z -= 1.0f; break;
                    case Keyboard.KEY_A: m_moveDir.x += 1.0f; break;
                    case Keyboard.KEY_D: m_moveDir.x -= 1.0f; break;
                    case Keyboard.KEY_SPACE: m_moveDir.y += 1.0f; break;
                    case Keyboard.KEY_C: m_moveDir.y -= 1.0f; break;
                }
            } else {
            	int eventKey = Keyboard.getEventKey();
                switch(eventKey) {
                    case Keyboard.KEY_W: m_moveDir.z -= 1.0f; break;
                    case Keyboard.KEY_S: m_moveDir.z += 1.0f; break;
                    case Keyboard.KEY_A: m_moveDir.x -= 1.0f; break;
                    case Keyboard.KEY_D: m_moveDir.x += 1.0f; break;
                    case Keyboard.KEY_SPACE: m_moveDir.y -= 1.0f; break;
                    case Keyboard.KEY_C: m_moveDir.y += 1.0f; break;
                    case Keyboard.KEY_F1: m_cam.changeProjection(); break;
                    default: m_mainProgram.specialInput(eventKey); break;
                }
            }
        }
        
        m_cam.move(moveSpeed * m_moveDir.z, moveSpeed * m_moveDir.x, moveSpeed * m_moveDir.y);
        
        while(Mouse.next()) {
            if(Mouse.getEventButton() == 0) {
                Mouse.setGrabbed(Mouse.getEventButtonState());
            }
            if(Mouse.isGrabbed()) {
            	m_cam.rotate(-camSpeed*Mouse.getEventDX(), -camSpeed*Mouse.getEventDY());
            }
        }
        
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) m_mainProgram.specialInput(Keyboard.KEY_ESCAPE);
    }
    
	
}
