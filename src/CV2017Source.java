import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.usfirst.frc.team5417.cv2017.ChannelRange;
import org.usfirst.frc.team5417.cv2017.ComputerVision2017;
import org.usfirst.frc.team5417.cv2017.ComputerVisionResult;
import org.usfirst.frc.team5417.cv2017.ImageReader;
import org.usfirst.frc.team5417.cv2017.opencvops.BooleanMatrix;

public class CV2017Source implements VideoSource {

	private VideoSource source;
	private ChannelRange c1Range, c2Range, c3Range;
	private int dilateErodeKernelSize;
	private int removeGroupsSmallerThan;
	private int numberOfScaleFactors;
	private double minimumTemplateMatchPercentage;
	private List<BooleanMatrix> templatesToUse;
	private double[] distanceLookUpTable;

	private ComputerVisionResult lastCvResult;

	public CV2017Source(VideoSource source, ChannelRange c1Range, ChannelRange c2Range, ChannelRange c3Range,
			int dilateErodeKernelSize, int removeGroupsSmallerThan, int numberOfScaleFactors,
			double minimumTemplateMatchPercentage, List<BooleanMatrix> templatesToUse, double[] distanceLookUpTable) {

		this.source = source;

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
		return this.source.getWidth();
	}

	@Override
	public int getHeight() {
		return this.source.getHeight();
	}

	@Override
	public Mat nextFrame() {
		Mat m = this.source.nextFrame();

		ComputerVision2017 cv2017 = new ComputerVision2017();
		try {
			ComputerVisionResult cvResult = cv2017.DoComputerVision(new ImageReader() {
				@Override
				public Mat read() {
					Mat frame = source.nextFrame();
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

			// LEAVE ME COMMENTED EXCEPT FOR DEBUGGING
			// if (cvResult.didSucceed) {
			// System.out.println("Distance: " + cvResult.distance + ", Target
			// Point: (" + cvResult.targetPoint.getX() + "," +
			// cvResult.targetPoint.getY() +")");
			// }
			// else {
			// System.out.println("Did not find exactly 2 groups!");
			// }

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
