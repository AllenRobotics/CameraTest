import org.opencv.core.Mat;

public interface VideoSource {

	public int getWidth();
	public int getHeight();
	public Mat nextFrame(); 
	
}
