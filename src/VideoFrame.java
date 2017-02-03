import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.opencv.core.Mat;

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
			if (img == null || img.getWidth() != w || img.getHeight() != h || img.getType() != BufferedImage.TYPE_3BYTE_BGR)
				img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		}

		public BufferedImage getImage(Mat mat) {
			getSpace(mat);
			mat.get(0, 0, dat);
			img.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), dat);
			return img;
		}
	}
	
	public class VideoComponent extends JComponent {
		private static final long serialVersionUID = 1L;
		private VideoSource source;
		private Mat2Image mat2Img = new Mat2Image();
		
		public VideoComponent(VideoSource source) {
			this.source = source;
		}
		
		@Override
		public void paintComponent(Graphics g) {
			if (source == null) return;
			
			Mat m = source.nextFrame();
			BufferedImage image = mat2Img.getImage(m);
			
			g.drawImage(image, 0, 0, source.getWidth(), source.getHeight(), this);
		}
	}
	
	public VideoFrame(String title, VideoSource source) {
		this.setTitle(title);
		this.setSize(source.getWidth(), source.getHeight());
		
		VideoComponent component = new VideoComponent(source);
		this.add(component);
	}
	
}
