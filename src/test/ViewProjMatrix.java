package test;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import util.Util;

public class ViewProjMatrix {

	public static void main(String[] args) {
		Vector3f camPos = new Vector3f(1f, 5.0f, 3.0f);
		Vector3f viewDir = new Vector3f(0.0f, 4.0f, 1.0f);
		Vector3f upDir = new Vector3f(0.0f, 1.0f, 0.0f);
		
		Vector3f lookAt = Vector3f.add(camPos, viewDir, null);
		
//		Matrix4f view = Util.lookAtLH3(camPos, lookAt, upDir, null);
		
//		System.out.println("View Matrix = " + view.toString());
		
		Matrix4f perspective = Util.frustum(-1.0f, 1.0f, -1.0f, 1.0f, -10.0f, 10.0f, null);
		System.out.println("Proj Matrix = " + perspective.toString());
	}

}
