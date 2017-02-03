import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.usfirst.frc.team5417.cv2017.Stopwatch;

public class CameraSource implements VideoSource {

	private VideoCapture camera;
	private int width;
	private int height;
	
	public CameraSource(int deviceIndex, int width, int height) throws InterruptedException {
		
		this.width = width;
		this.height = height;

		Stopwatch s = Stopwatch.startNew();
		
		this.camera = new VideoCapture(deviceIndex);
		while (false == camera.isOpened()) {
			System.out.println("Waiting for camera to initialize: " + s.getTotalSeconds() + " seconds elapsed.");
			Thread.sleep(100);
		}
		
		this.camera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, this.width);
		this.camera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, this.height);
	}
	
	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public Mat nextFrame() {
		Mat frame = new Mat();
		this.camera.read(frame);
		return frame;
	}

	public void close() {
		this.camera.release();
	}
}
