package wbif.sjx.TrackAnalysis.Plot3D.Utils;


import wbif.sjx.TrackAnalysis.Plot3D.Math.vectors.Vector2f;
import wbif.sjx.TrackAnalysis.Plot3D.Math.vectors.Vector3f;
import wbif.sjx.TrackAnalysis.Plot3D.Math.vectors.Vector3i;
import wbif.sjx.TrackAnalysis.Plot3D.Math.vectors.Vector4f;
import wbif.sjx.common.Object.*;
import wbif.sjx.common.Object.Point;

import java.awt.*;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;

public class DataTypeUtils {
	private DataTypeUtils(){}
	
	public static ByteBuffer toByteBuffer(byte[] array){
		ByteBuffer result = ByteBuffer.allocateDirect(array.length).order(ByteOrder.nativeOrder());
		result.put(array).flip();
		return result;
	}

    public static byte[] toByteArray(ByteBuffer byteBuffer){
        byte[] byteArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(byteArray);
        return byteArray;
    }
	
	public static FloatBuffer toFloatBuffer(float[] array){
		FloatBuffer result = ByteBuffer.allocateDirect(array.length << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
		result.put(array).flip();
		return result;
	}
	
	public static IntBuffer toIntBuffer(int[] array){
		IntBuffer result = ByteBuffer.allocateDirect(array.length << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
		result.put(array).flip();
		return result;
	}

	public static int[] toIntArray(IntBuffer intBuffer){
        int[] intArray = new int[intBuffer.remaining()];
        intBuffer.get(intArray);
        return intArray;
    }

	public static float[] toFloatArray(float[][] array){
		float[] result = new float[array.length * array[0].length];
		for (int row = 0; row < array.length; row++) {
			for (int col = 0; col < array[0].length; col++) {
				result[row + col * array[0].length] = array[row][col];
			}
		}
		return result;
	}

	public static float[] toFloatArray(Vector4f[] array){
		float[] result = new float[array.length * 4];
		for(int i = 0; i < array.length; i++){
			result[i * 4    ] = array[i].getX();
			result[i * 4 + 1] = array[i].getY();
			result[i * 4 + 2] = array[i].getZ();
			result[i * 4 + 3] = array[i].getW();
		}
		return result;
	}

	public static float[] toFloatArray(Vector3f[] array){
		float[] result = new float[array.length * 3];
		for(int i = 0; i < array.length; i++){
			result[i * 3    ] = array[i].getX();
			result[i * 3 + 1] = array[i].getY();
			result[i * 3 + 2] = array[i].getZ() ;
		}
		return result;
	}

	public static float[] toFloatArray(Vector2f[] array){
		float[] result = new float[array.length * 2];
		for(int i = 0; i < array.length; i++){
			result[i * 2    ] = array[i].x;
			result[i * 2 + 1] = array[i].y;
		}
		return result;
	}

	public static int[] toIntArray(Vector3i[] array){
		int[] result = new int[array.length * 3];
		for(int i = 0; i < array.length; i++){
			result[i * 3    ] = array[i].x;
			result[i * 3 + 1] = array[i].y;
			result[i * 3 + 2] = array[i].z;
		}
		return result;
	}

	public static char[] toCharArray(String string){
		char[] result = new char[string.length()];
		for(int i = 0; i < string.length(); i++){
			result[i] = string.charAt(i);
		}
		return result;
	}

	public static Vector3f[] flipArray(Vector3f[] vecs){
		int length = vecs.length;
		Vector3f[] result = new Vector3f[length];

		for(int i = 0; i < length; i++){
			result[i] = vecs[length - i - 1];
		}

		return result;
	}

	public static Vector4f toOpenGlColour(Color color){
		return new Vector4f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
	}

	public static Vector3f toVector3f(Point point){
	    if(point != null) {
            return new Vector3f((float) point.getX().floatValue(), (float) point.getZ().floatValue(), (float) point.getY().floatValue());
        }else {
	        return new Vector3f();
        }
	}

	public static String loadAsString(String fileName) throws Exception {
		String result;
		try {
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			InputStream in = classloader.getResourceAsStream(fileName);
			Scanner scanner = new Scanner(in, "UTF-8");
			result = scanner.useDelimiter("\\A").next();
		}catch (Exception e){
			return null;
		}
		return result;
	}
}
