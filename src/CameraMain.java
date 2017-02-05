
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;
import org.usfirst.frc.team5417.cv2017.ChannelRange;
import org.usfirst.frc.team5417.cv2017.ComputerVisionResult;
import org.usfirst.frc.team5417.cv2017.ImageReader;
import org.usfirst.frc.team5417.cv2017.OpenCV;
import org.usfirst.frc.team5417.cv2017.NTimesPerSecond;
import org.usfirst.frc.team5417.cv2017.Stopwatch;
import org.usfirst.frc.team5417.cv2017.opencvops.BooleanMatrix;

public class CameraMain {

	private static final double NTSC_FPS = 29.97;

	private static final int WEBCAM_DEVICE_INDEX = 0;
	private static final int captureWidth = 320;
	private static final int captureHeight = 240;
	private static final double fps = NTSC_FPS;

	private static Timer updateTimer;
	
	private static double lastFrameTimeSeconds;

	static {
		OpenCV.LoadLibraries();
	}

	public static void main(String[] args) throws InterruptedException {
		List<BooleanMatrix> horizontalTemplates = new ArrayList<BooleanMatrix>();
		// horizontalTemplates.add(new BooleanMatrix(40, 150, true));
		// horizontalTemplates.add(new BooleanMatrix(20, 150, true));
		horizontalTemplates.add(new BooleanMatrix(20, 75, true));
		horizontalTemplates.add(new BooleanMatrix(10, 75, true));

		List<BooleanMatrix> verticalTemplates = new ArrayList<BooleanMatrix>();
		// verticalTemplates.add(new BooleanMatrix(150, 60, true));
		verticalTemplates.add(new BooleanMatrix(75, 30, true));

		List<BooleanMatrix> templatesToUse = verticalTemplates;
		double[] gearLookUpTable = {
				500,	//0 feet
				199,	//1 foot
				110,
				77.5,
				57,
				45.5,
				39,
				33,
				29,
				25		//9 feet
		};
		
		double[] lookUpTableToUse = gearLookUpTable;

		// Real images HSV
//		ChannelRange hueRange = new ChannelRange(150, 200);
//		ChannelRange satRange = new ChannelRange(0.2, 1.0);
//		ChannelRange valRange = new ChannelRange(180, 256);

		// Test images HSV
		ChannelRange hueRange = new ChannelRange(122, 150);
		ChannelRange satRange = new ChannelRange(0.4, 0.8);
		ChannelRange valRange = new ChannelRange(220, 256);

		int dilateErodeKernelSize = 7;
		int removeGroupsSmallerThan = 12;
		int numberOfScaleFactors = 10;
		double minimumTemplateMatchPercentage = 0.7;

		CameraSource cameraSource = new CameraSource(WEBCAM_DEVICE_INDEX, captureWidth, captureHeight);
		CV2017Source cv2017Source = new CV2017Source(cameraSource, hueRange, satRange, valRange, dilateErodeKernelSize,
				removeGroupsSmallerThan, numberOfScaleFactors, minimumTemplateMatchPercentage,
				templatesToUse, lookUpTableToUse);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				VideoFrame cameraFrame = new VideoFrame("LifeCam", cameraSource);
				cameraFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				cameraFrame.setVisible(true);
				
				VideoFrame cv2017Frame = new VideoFrame("CV2017", cv2017Source);
				cv2017Frame.setVisible(true);

				NTimesPerSecond timesPerSecond = new NTimesPerSecond(fps);
				Stopwatch now = Stopwatch.startNew();
				lastFrameTimeSeconds = now.getTotalSeconds();
				
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						cameraSource.close();
					}
				});

				ActionListener updater = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cameraFrame.repaint();
						cv2017Frame.repaint();
						
						double currentFrameTimeSeconds = now.getTotalSeconds();
						double elapsedSeconds = currentFrameTimeSeconds - lastFrameTimeSeconds;
						lastFrameTimeSeconds = currentFrameTimeSeconds;
						
						double instantaneousFps = 1.0 / elapsedSeconds;
						
						ComputerVisionResult cvResult = cv2017Source.getLastCvResult();

						cameraFrame.displayFps(instantaneousFps);
						cv2017Frame.displayFps(instantaneousFps);
						if (cvResult != null && cvResult.didSucceed) {
							cv2017Frame.displayDistance(cvResult.distance);
							cv2017Frame.displayTargetPoint(cvResult.targetPoint);
						}
						
						updateTimer.setDelay(timesPerSecond.nextDelayMs());
						updateTimer.restart();
					}
				};

				timesPerSecond.start();

				updateTimer = new Timer((int) (1000.0 / fps), updater);
				updateTimer.start();
			}
		});
	}
}
