
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
import org.usfirst.frc.team5417.cv2017.MatrixUtilities;
import org.usfirst.frc.team5417.cv2017.NTimesPerSecond;
import org.usfirst.frc.team5417.cv2017.customops.BooleanMatrix;

public class CameraMain {

	private static final double NTSC_FPS = 29.97;

	private static final int WEBCAM_DEVICE_INDEX = 0;
	private static final int captureWidth = 640;
	private static final int captureHeight = 480;
	private static final double fps = NTSC_FPS;

	private static Timer updateTimer;

	static {
		MatrixUtilities.LoadOpenCVLibraries();
	}

	public static void main(String[] args) throws InterruptedException {
		List<BooleanMatrix> horizontalTemplates = new ArrayList<BooleanMatrix>();
		// horizontalTemplates.add(new BooleanMatrix(40, 150, true));
		// horizontalTemplates.add(new BooleanMatrix(20, 150, true));
		horizontalTemplates.add(new BooleanMatrix(13, 50, true));
		horizontalTemplates.add(new BooleanMatrix(7, 50, true));

		List<BooleanMatrix> verticalTemplates = new ArrayList<BooleanMatrix>();
		// verticalTemplates.add(new BooleanMatrix(150, 40, true));
		// verticalTemplates.add(new BooleanMatrix(150, 20, true));
		verticalTemplates.add(new BooleanMatrix(50, 13, true));
		verticalTemplates.add(new BooleanMatrix(50, 7, true));

		List<BooleanMatrix> templatesToUse = horizontalTemplates;

		// Real images HSV
//		ChannelRange hueRange = new ChannelRange(130, 180);
//		ChannelRange satRange = new ChannelRange(.7, 1.0);
//		ChannelRange valRange = new ChannelRange(220, 256);

		ChannelRange hueRange = new ChannelRange(100, 180);
		ChannelRange satRange = new ChannelRange(0.6, 1.0);
		ChannelRange valRange = new ChannelRange(200, 256);

		int dilateErodeKernelSize = 11;
		int removeGroupsSmallerThan = 100;
		double minimumTemplateScale = 0.25, maximumTemplateScale = 4;
		double minimumTemplateMatchPercentage = 0.7;

		CameraSource cameraSource = new CameraSource(WEBCAM_DEVICE_INDEX, captureWidth, captureHeight);
		CV2017Source cv2017Source = new CV2017Source(cameraSource, hueRange, satRange, valRange, dilateErodeKernelSize,
				removeGroupsSmallerThan, minimumTemplateScale, maximumTemplateScale, minimumTemplateMatchPercentage,
				templatesToUse);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				VideoFrame cameraFrame = new VideoFrame("LifeCam", cameraSource);
				cameraFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				cameraFrame.setVisible(true);
				
				VideoFrame cv2017Frame = new VideoFrame("CV2017", cv2017Source);
				cv2017Frame.setVisible(true);

				NTimesPerSecond timesPerSecond = new NTimesPerSecond(fps);
				
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
						
						double actualFps = timesPerSecond.fps();
						
						ComputerVisionResult cvResult = cv2017Source.getLastCvResult();

						cameraFrame.displayFps(actualFps);
						cv2017Frame.displayFps(actualFps);
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
