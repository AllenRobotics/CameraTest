import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.usfirst.frc.team5417.cv2017.ChannelRange;
import org.usfirst.frc.team5417.cv2017.ComputerVision2017;
import org.usfirst.frc.team5417.cv2017.ComputerVisionResult;
import org.usfirst.frc.team5417.cv2017.ImageReader;
import org.usfirst.frc.team5417.cv2017.opencvops.BooleanMatrix;

public class CV2017Source implements VideoSource {

	private int width;
	private int height;

	private Mat currentFrame;
	
	private ChannelRange c1Range, c2Range, c3Range;
	private int dilateErodeKernelSize;
	private int removeGroupsSmallerThan;
	private int numberOfScaleFactors;
	private double minimumTemplateMatchPercentage;
	private List<BooleanMatrix> templatesToUse;
	private double[] distanceLookUpTable;

	private ComputerVisionResult lastCvResult;

	public CV2017Source(int width, int height, ChannelRange c1Range, ChannelRange c2Range, ChannelRange c3Range,
			int dilateErodeKernelSize, int removeGroupsSmallerThan, int numberOfScaleFactors,
			double minimumTemplateMatchPercentage, List<BooleanMatrix> templatesToUse, double[] distanceLookUpTable) {

		this.width = width;
		this.height = height;
		
		this.c1Range = c1Range;
		this.c2Range = c2Range;
		this.c3Range = c3Range;
		this.dilateErodeKernelSize = dilateErodeKernelSize;
		this.removeGroupsSmallerThan = removeGroupsSmallerThan;
		this.numberOfScaleFactors = numberOfScaleFactors;
		this.minimumTemplateMatchPercentage = minimumTemplateMatchPercentage;
		this.templatesToUse = templatesToUse;
		this.distanceLookUpTable = distanceLookUpTable;

	}

	public void setTemplatesToUse(List<BooleanMatrix> templatesToUse) {
		this.templatesToUse = templatesToUse;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	public void setFrame(Mat frame) {
		this.currentFrame = frame;
	}
	
	@Override
	public Mat nextFrame() {
		Mat m = this.currentFrame;

		ComputerVision2017 cv2017 = new ComputerVision2017();
		try {
			ComputerVisionResult cvResult = cv2017.DoComputerVision(new ImageReader() {
				@Override
				public Mat read() {
					Mat frame = m;
					Mat f32f = new Mat();
					frame.assignTo(f32f, CvType.CV_32FC3);
					return f32f;
				}
			}, Math.max(this.getWidth(), this.getHeight()), c1Range, c2Range, c3Range, dilateErodeKernelSize,
					removeGroupsSmallerThan, numberOfScaleFactors, minimumTemplateMatchPercentage,
					templatesToUse, distanceLookUpTable);

			// ComputerVisionResult cvResult = new ComputerVisionResult();
			// cvResult.didSucceed = false;
			// cvResult.distance = -1;
			// cvResult.targetPoint = new PointD(-1, -1);
			// cvResult.visionResult = m;

			this.lastCvResult = cvResult;

			Mat result = new Mat();
			cvResult.visionResult.assignTo(result, CvType.CV_8UC3);

			return result;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return m;
		}

	}

	public ComputerVisionResult getLastCvResult() {
		return this.lastCvResult;
	}

}
