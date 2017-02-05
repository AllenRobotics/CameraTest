import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.VolatileImage;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

import org.opencv.core.Mat;
import org.usfirst.frc.team5417.cv2017.OpenCV;
import org.usfirst.frc.team5417.cv2017.TimedMovingAverage;
import org.usfirst.frc.team5417.cv2017.opencvops.PointD;

public class VideoFrame extends JFrame {

	private class Mat2Image {
		protected Mat mat = new Mat();
		protected BufferedImage img;
		protected byte[] dat;

		public Mat2Image() {
		}

		private void getSpace(Mat mat) {
			this.mat = mat;
			int w = mat.cols(), h = mat.rows();
			if (dat == null || dat.length != w * h * 3)
				dat = new byte[w * h * 3];
			if (img == null || img.getWidth() != w || img.getHeight() != h) {
				
//				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//				GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
//
//				img = gc.createCompatibleImage(w, h, Transparency.OPAQUE);

				if (mat.channels() == 1)
					img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY); 
				else if (mat.channels() == 3)
					img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
				else if (mat.channels() == 4)
					img = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
			}
		}

		public Image getImage(Mat mat) {
			getSpace(mat);
			mat.get(0, 0, dat);
			
			//img.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), dat);
			byte[] imgData = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
			System.arraycopy(dat, 0, imgData, 0, dat.length);

			return img;
		}
	}

	private class VideoComponent extends JComponent {
		private static final long serialVersionUID = 1L;
		private Mat2Image mat2Img = new Mat2Image();

		private String fpsText;
		private String distanceText;
		private String targetPointText;
		
		private DecimalFormat decimalFormat = new DecimalFormat("0.00");

		private Mat currentFrame;

		public VideoComponent() {
		}

		public void setFrame(Mat frame) {
			currentFrame = frame;
		}
		
		public void displayFps(double actualFps) {
			fpsText = "fps: " + decimalFormat.format(actualFps);
		}

		public void displayDistance(double actualDistance) {
			distanceText = "pixel distance: " + decimalFormat.format(actualDistance);
		}

		public void displayTargetPoint(PointD targetPoint) {
			targetPointText = 
					"target point: (" + decimalFormat.format(targetPoint.getX()) + "," + decimalFormat.format(targetPoint.getY()) + ")";
		}

		@Override
		public void paintComponent(Graphics g) {
			if (currentFrame == null)
				return;

			this.setBackground(Color.white);
			
			Mat m = currentFrame;
			if (m.channels() == 3) {
				m = OpenCV.reverseColorChannels(m);
			}
			Image image = mat2Img.getImage(m);

			Graphics2D g2 = (Graphics2D) g;
			g2.drawImage(image, 0, 0, m.cols(), m.rows(), this);

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			FontRenderContext frc = g2.getFontRenderContext();
			Font f = new Font("Helvetica", 1, 20);

			if (fpsText != null) {
				this.drawOutlinedText(g2, frc, f, fpsText, 10, m.rows() - 60);
			}
			if (distanceText != null) {
				this.drawOutlinedText(g2, frc, f, distanceText, 10, 30);
			}
			if (targetPointText != null) {
				this.drawOutlinedText(g2, frc, f, targetPointText, 10, 50);
			}

			// if (fpsText != null) {
			// g.drawString(fpsText, 10, source.getHeight() - 60);
			// }
			// if (distanceText != null) {
			// g.drawString(distanceText, 10, 30);
			// }
			// if (targetPointText != null) {
			// g.drawString(targetPointText, 10, 50);
			// }
		}

		private void drawOutlinedText(Graphics2D g2, FontRenderContext frc, Font f, String str, int x, int y) {
			TextLayout l = new TextLayout(str, f, frc);
			Shape outline = l.getOutline(null);
			AffineTransform transform = new AffineTransform();

			int zeroX = 0;
			int zeroY = 0;

			transform.translate(zeroX + x, zeroY + y);
			g2.transform(transform);
			try
			{
				g2.setColor(Color.darkGray);
				g2.draw(outline);
				g2.setClip(outline);
	
				g2.setColor(Color.yellow);
				g2.fill(outline);
			}
			finally
			{
				try {
					transform.invert();
					g2.transform(transform);
				} catch (NoninvertibleTransformException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private VideoComponent video;

	private TimedMovingAverage fpsMovingAverage = new TimedMovingAverage(3.0);
	private TimedMovingAverage distanceMovingAverage = new TimedMovingAverage(3.0);
	private TimedMovingAverage targetPointXMovingAverage = new TimedMovingAverage(3.0);
	private TimedMovingAverage targetPointYMovingAverage = new TimedMovingAverage(3.0);
	
	public VideoFrame(String title, int width, int height) {
		this.setTitle(title);
		this.setSize(width, height);

		this.video = new VideoComponent();
		this.add(this.video);
	}
	
	public void setFrame(Mat frame) {
		this.video.setFrame(frame);
	}

	public void displayFps(double instantaneousFps) {
		fpsMovingAverage.recordDataPoint(instantaneousFps);
		this.video.displayFps(fpsMovingAverage.average());	
	}

	public void displayDistance(double actualDistance) {
//		distanceMovingAverage.recordDataPoint(actualDistance);
//		this.video.displayDistance(distanceMovingAverage.average());

		this.video.displayDistance(actualDistance);
	}

	public void displayTargetPoint(PointD targetPoint) {
//		targetPointXMovingAverage.recordDataPoint(targetPoint.getX());
//		targetPointYMovingAverage.recordDataPoint(targetPoint.getY());
//		this.video.displayTargetPoint(new PointD(targetPointXMovingAverage.average(), targetPointYMovingAverage.average()));

		this.video.displayTargetPoint(targetPoint);
	}

}
