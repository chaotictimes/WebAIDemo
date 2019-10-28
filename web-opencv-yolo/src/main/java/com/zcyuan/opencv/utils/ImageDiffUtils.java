package com.zcyuan.opencv.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.bytedeco.javacpp.opencv_core.DMatch;
import org.bytedeco.javacpp.opencv_core.DMatchVector;
import org.bytedeco.javacpp.opencv_core.KeyPoint;
import org.bytedeco.javacpp.opencv_core.KeyPointVector;
import org.bytedeco.javacpp.opencv_features2d.AKAZE;
import org.bytedeco.javacpp.opencv_features2d.BFMatcher;
import org.bytedeco.javacpp.opencv_features2d.KAZE;
//import org.bytedeco.javacv.Java2DFrameConverter;
//import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

public class ImageDiffUtils {
	private void downscaleImages(Mat img1Src, Mat img1Dst, Mat img2Src, Mat img2Dst) {
		int width1 = img1Src.width();
		int height1 = img1Src.height();
		int width2 = img2Src.width();
		int height2 = img2Src.height();
		int maxWidth = 400;
		double scale = 1.0;
		if (width1 > maxWidth || width2 > maxWidth) {
			if (width1 > maxWidth && width1 > width2) {
				scale = maxWidth * 1.0 / width1;
			} else {
				scale = maxWidth * 1.0 / width2;
			}
			Imgproc.resize(img1Src, img1Dst, new Size(width1 * scale, height1 * scale), Imgproc.INTER_AREA);
			Imgproc.resize(img2Src, img2Dst, new Size(width2 * scale, height2 * scale), Imgproc.INTER_AREA);
		} else {
			img1Src.copyTo(img1Dst);
			img2Src.copyTo(img2Dst);
		}
	}

	private void normaliseImages(Mat img1, Mat img1Dst, Mat img2, Mat img2Dst) {
		int width1 = img1.width();
		int height1 = img1.height();
		int width2 = img2.width();
		int height2 = img2.height();

		CLAHE clahe;
		if (height1 * width1 > height2 * width2) {
			clahe = Imgproc.createCLAHE(4, new Size(width1, height1));
		} else {
			clahe = Imgproc.createCLAHE(4, new Size(width2, height2));
		}
		clahe.apply(img1, img1Dst);
		clahe.apply(img2, img2Dst);
	}

	private BufferedImage matToBufferedImage(Mat frame) {
		int type = 0;
		if (frame.channels() == 1) {
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else if (frame.channels() == 3) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
		WritableRaster raster = image.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		frame.get(0, 0, data);
		return image;
	}

	private org.bytedeco.javacpp.opencv_core.Mat bufferedImageToMat(BufferedImage bi) {
//		OpenCVFrameConverter.ToMat cv = new OpenCVFrameConverter.ToMat();
//		return cv.convertToMat(new Java2DFrameConverter().convert(bi));
		return null;
	}

	private class MyComparator implements Comparator<DMatch> {

		@Override
		public int compare(DMatch o1, DMatch o2) {
			if (o1.distance() > o2.distance()) {
				return 1;
			} else if (o1.distance() < o2.distance()) {
				return -1;
			}
			return 0;
		}

	}

	private ArrayList<KeyPoint> getMatches(Mat img1Src, Mat img2Src) {
		ArrayList<KeyPoint> matchedCoordinates = new ArrayList<KeyPoint>();
		org.bytedeco.javacpp.opencv_core.Mat img1 = bufferedImageToMat(matToBufferedImage(img1Src));
		org.bytedeco.javacpp.opencv_core.Mat img2 = bufferedImageToMat(matToBufferedImage(img2Src));
		KeyPointVector keypoint1 = new KeyPointVector();
		KeyPointVector keypoint2 = new KeyPointVector();
		org.bytedeco.javacpp.opencv_core.Mat mask = new org.bytedeco.javacpp.opencv_core.Mat();
		org.bytedeco.javacpp.opencv_core.Mat des1 = new org.bytedeco.javacpp.opencv_core.Mat();
		org.bytedeco.javacpp.opencv_core.Mat des2 = new org.bytedeco.javacpp.opencv_core.Mat();

		AKAZE akaze = AKAZE.create();
		akaze.detectAndCompute(img1, mask, keypoint1, des1);
		akaze.detectAndCompute(img2, mask, keypoint2, des2);

		DMatchVector matches = new DMatchVector();
		BFMatcher bf = new BFMatcher(Core.NORM_HAMMING, true);
		bf.match(des1, des2, matches);
		bf.close();
		DMatch[] _dmatches = matches.get();
		System.out.println("getMatches num2 " + _dmatches.length);
		Comparator<DMatch> cmp = new MyComparator();
		Arrays.sort(_dmatches, cmp);

		KeyPoint[] kps1 = keypoint1.get();
		KeyPoint[] kps2 = keypoint2.get();
		for (DMatch match : _dmatches) {
			KeyPoint kp1 = kps1[match.queryIdx()];
			KeyPoint kp2 = kps2[match.trainIdx()];
			matchedCoordinates.add(new KeyPoint(kp1));
			matchedCoordinates.add(new KeyPoint(kp2));
		}
		return matchedCoordinates;
	}

	private int getDiameter(Mat img) {
		int w = img.width();
		int h = img.height();

		double hyp = Math.pow(w * w + h * h, 1 / 2.0);
		return (int) (hyp + 1);
	}

	private Mat addBorders(Mat img) {
		int hyp = getDiameter(img);
		
		Mat mask = new Mat(hyp, hyp, CvType.CV_8UC3, new Scalar(0, 0, 0));
		int x1, y1;
		x1 = y1 = hyp;
		int cx = x1 / 2;
		int cy = y1 / 2;

		int x2 = img.width();
		int y2 = img.height();
		int cx2 = x2 / 2;
		int cy2 = y2 / 2;

//		int offsetX = x2 % 2;
//		int offsetY = y2 % 2;
		Rect r = new Rect(cx - cx2, cy - cy2, x2, y2);
//		System.out.println("addBorders hyp " + hyp + " rect " + r.toString());
		Mat roi = new Mat(mask, r);
		img.copyTo(roi, img);
		return mask;
	}

	private Mat removeBorders(Mat img) {
		int w = img.width();
		int h = img.height();

		Mat B = new Mat();
		Imgproc.cvtColor(img, B, Imgproc.COLOR_BGR2GRAY);

		int left = w;
		int right = 0;
		int top = h;
		int bottom = 0;
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				double[] data = img.get(i, j);
				if (data[0] > 0 || data[1] > 0 || data[2] > 0) {
					if (i < top)
						top = i;
					if (i > bottom)
						bottom = i;
					if (j < left)
						left = j;
					if (j > right)
						right = j;
				}
			}
		}
		Mat dst = new Mat();
		Mat imgROI = new Mat(img, new Rect(left, top, right - left, bottom - top));
		imgROI.copyTo(dst);
		return dst;
	}

	private double getRotationAngle(Mat img1, Mat img2) {
		ArrayList<KeyPoint> matches = getMatches(img1, img2);
		if (matches.size() < 4)return 0;
		float point1AX = matches.get(0).pt().x();
		float point1AY = matches.get(0).pt().y();
		float point2AX = matches.get(1).pt().x();
		float point2AY = matches.get(1).pt().y();

		float point1BX = matches.get(2).pt().x();
		float point1BY = matches.get(2).pt().y();
		float point2BX = matches.get(3).pt().x();
		float point2BY = matches.get(3).pt().y();

		float m1 = ((point1BY - point1AY) / (point1BX - point1AX));
		double line1Angle = Math.atan(m1);
		float m2 = ((point2BY - point2AY) / (point2BX - point2AX));
		double line2Angle = Math.atan(m2);
		double rotationAngle = line2Angle - line1Angle;
		return Math.toDegrees(rotationAngle);
	}

	private void rotateImage(Mat img, Mat dst, double rotationAngle) {
		int w = img.width();
		int h = img.height();

		int cx = w / 2;
		int cy = h / 2;

		Mat rm = Imgproc.getRotationMatrix2D(new Point(cx, cy), rotationAngle, 1.0);
		Imgproc.warpAffine(img, dst, rm, new Size(w, h), Imgproc.INTER_CUBIC);
	}

	private boolean checkRotation(Mat img1, Mat img2) {
		ArrayList<KeyPoint> matches = getMatches(img1, img2);
		if (matches.size() < 4)return true;
		float point1AX = matches.get(0).pt().x();
		float point2AX = matches.get(2).pt().x();
		float point1BX = matches.get(1).pt().x();
		float point2BX = matches.get(3).pt().x();
		if (point1AX < point1BX && point2AX > point2BX) {
			return false;
		} else if (point1AX > point1BX && point2AX < point2BX) {
			return false;
		}
		return true;
	}

	private Mat matchRotation(Mat img1, Mat img2) {
		Mat borderedImg = addBorders(img2);
		double rotationAngle = getRotationAngle(img1, img2);
		System.out.println("matchRotation rotationAngle:" + rotationAngle);
		Mat rotatedImage = new Mat();
		rotateImage(borderedImg, rotatedImage, rotationAngle);
//		if (checkRotation(img1, rotatedImage) == false) {
//			rotateImage(rotatedImage, rotatedImage, 180);
//		}
		return removeBorders(rotatedImage);
	}

	private double getDistance(float x1, float y1, float x2, float y2) {
		double xp = Math.pow(x1 - x2, 2);
		double yp = Math.pow(y1 - y2, 2);
		return Math.pow(xp + yp, 1 / 2.0);
	}

	private void getScalingLevel(ArrayList<KeyPoint> matches, Point p) {
		if (matches.size() < 4)return ;
		float point1AX = matches.get(0).pt().x();
		float point1AY = matches.get(0).pt().y();
		float point2AX = matches.get(1).pt().x();
		float point2AY = matches.get(1).pt().y();
		float point1BX = matches.get(2).pt().x();
		float point1BY = matches.get(2).pt().y();
		float point2BX = matches.get(3).pt().x();
		float point2BY = matches.get(3).pt().y();

		System.out.println("getScalingLevel point1A:" + point1AX + ":" + point1AY);
		System.out.println("getScalingLevel point1B:" + point1BX + ":" + point1BY);
		System.out.println("getScalingLevel point2A:" + point2AX + ":" + point2AY);
		System.out.println("getScalingLevel point2B:" + point2BX + ":" + point2BY);
		double dist1 = getDistance(point1AX, point1AY, point1BX, point1BY);
		double dist2 = getDistance(point2AX, point2AY, point2BX, point2BY);

		if (dist1 < dist2) {
			p.x = 0;
			p.y = dist1 / dist2;
		} else {
			p.x = 1;
			p.y = dist2 / dist1;
		}
	}

	private void scaleImages(Mat img1Src, Mat img2Src, Mat dst1, Mat dst2) {
		ArrayList<KeyPoint> matches = getMatches(img1Src, img2Src);
		Point p = new Point();
		getScalingLevel(matches, p);
		Mat img = img1Src;
		if (p.x == 0) {
			img = img2Src;
		}
		float w = img.width();
		float h = img.height();
		System.out.println("scaleImages scale:" + p.y);
		Imgproc.resize(img, dst2, new Size(w * p.y, h * p.y), Imgproc.INTER_CUBIC);

		System.out.println("scaleImages origin img1 w:" + img1Src.width() + "img1 h:" + img1Src.height());
		System.out.println("scaleImages origin img2 w:" + img2Src.width() + "img2 h:" + img2Src.height());
		if (p.x == 0) {
			img1Src.copyTo(dst1);
		} else {
			dst2.copyTo(dst1);
			img2Src.copyTo(dst2);
		}
		System.out.println("scaleImages after img1 w:" + dst1.width() + "img1 h:" + dst1.height());
		System.out.println("scaleImages after img2 w:" + dst2.width() + "img2 h:" + dst2.height());
	}

	private Mat locationCorrection(Mat img1, Mat img2) {
		int w = img2.width();
		int h = img2.height();
		ArrayList<KeyPoint> matches = getMatches(img1, img2);
		if (matches.size() < 2)return null;
		float img1X = matches.get(0).pt().x();
		float img1Y = matches.get(0).pt().y();
		float img2X = matches.get(1).pt().x();
		float img2Y = matches.get(1).pt().y();

		float difX = img1X - img2X;
		float difY = img1Y - img2Y;
		System.out.println("locationCorrection difX " + difX + " difY:" + difY);
		float data[] = { 1, 0, difX, 0, 1, difY };
		Mat tr = new Mat(2, 3, CvType.CV_32F);
		tr.put(0, 0, data);
		Mat dst = new Mat();
		Imgproc.warpAffine(img2, dst, tr, new Size(w, h), Imgproc.INTER_LINEAR);
		System.out.println("locationCorrection img2 w:" + dst.width() + "img2 h:" + dst.height());
		return dst;
	}

	private void getDifferences(Mat img1Src, Mat img2Src, ArrayList<KeyPoint> keyPoints1, ArrayList<KeyPoint> keyPoints2) {
		org.bytedeco.javacpp.opencv_core.Mat img1 = bufferedImageToMat(matToBufferedImage(img1Src));
		org.bytedeco.javacpp.opencv_core.Mat img2 = bufferedImageToMat(matToBufferedImage(img2Src));
		KeyPointVector keypoint1 = new KeyPointVector();
		KeyPointVector keypoint2 = new KeyPointVector();
		org.bytedeco.javacpp.opencv_core.Mat mask = new org.bytedeco.javacpp.opencv_core.Mat();
		org.bytedeco.javacpp.opencv_core.Mat des1 = new org.bytedeco.javacpp.opencv_core.Mat();
		org.bytedeco.javacpp.opencv_core.Mat des2 = new org.bytedeco.javacpp.opencv_core.Mat();
		AKAZE akaze = AKAZE.create(AKAZE.DESCRIPTOR_MLDB,0,3,0.001f,4,4,KAZE.DIFF_PM_G2);
		akaze.detectAndCompute(img1, mask, keypoint1, des1);
		akaze.detectAndCompute(img2, mask, keypoint2, des2);

		DMatchVector matches = new DMatchVector();
		BFMatcher bf = new BFMatcher(Core.NORM_HAMMING, true);
		bf.match(des1, des2, matches);
		bf.close();
		DMatch[] _dmatches = matches.get();
		KeyPoint[] kps1 = keypoint1.get();
		KeyPoint[] kps2 = keypoint2.get();
		for (DMatch match : _dmatches) {
			kps1[match.queryIdx()] = null;
			kps2[match.trainIdx()] = null;
		}

		for (KeyPoint kp1 : kps1) {
			if (kp1 != null) {
				keyPoints1.add(new KeyPoint(kp1));
			}
		}
		for (KeyPoint kp2 : kps2) {
			if (kp2 != null) {
				keyPoints2.add(new KeyPoint(kp2));
			}
		}
	}

	private MinMaxLocResult getBestMatch(Mat img, Mat patch) {
		Mat result = new Mat();
	    Imgproc.matchTemplate(img, patch,result, Imgproc.TM_CCOEFF_NORMED);

	    MinMaxLocResult mmr = Core.minMaxLoc(result);
	    return mmr;
	}
	    		
	private Mat getMask(Mat img1, Mat img2) {
		int w1 = img1.width();
		int h1 = img1.height();

		Imgproc.equalizeHist(img1, img1);
		Imgproc.equalizeHist(img2, img2);

		ArrayList<KeyPoint> keyPoints1 = new ArrayList<KeyPoint>();
		ArrayList<KeyPoint> keyPoints2 = new ArrayList<KeyPoint>();
		getDifferences(img1, img2, keyPoints1, keyPoints2);

		byte []value = new byte[1];
		value[0] = (byte) 0xFF;
		Mat mask = Mat.zeros(h1, w1, CvType.CV_8UC1);
		for (KeyPoint kp1 : keyPoints1) {
			mask.put((int)kp1.pt().y(), (int)kp1.pt().x(), value);
		}
		
		int lastNoContours = keyPoints1.size();
		
		int dsize = h1 < w1 ? h1 / 80 : w1 / 80;
		Mat shape = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(dsize, dsize));
		Imgproc.erode(mask,mask,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(1, 1)));
		Imgproc.dilate(mask,mask,shape);
//		Imgproc.morphologyEx(mask, mask,Imgproc.MORPH_CLOSE, shape);
		
		for (int i=0;i<100;i++) {
			Mat hierarchy = new Mat();
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(mask.clone(), contours,hierarchy, Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE );
			int cn = 0;
			for (MatOfPoint contour : contours) {
				Rect r = Imgproc.boundingRect(contour);
				Mat patch = new Mat();
				Mat imgROI = new Mat(img1, r);
				imgROI.copyTo(patch);
				MinMaxLocResult mmr = getBestMatch(img2, patch);
				System.out.println("getMask rect:" +r.toString());
				System.out.println("getBestMatch mmr.minVal:" + mmr.minVal + ":mmr.maxVal " + mmr.maxVal);
				if (mmr.maxVal > 0.5 || (r.width <= dsize && r.height <= dsize)) {
					Imgproc.drawContours(mask, contours, cn, new Scalar(0));
				}else {
					Imgproc.drawContours(mask, contours, cn, new Scalar(255,255,255),3);
				}
				cn ++;
				int noContours = contours.size();
				if (noContours * 1.0 / lastNoContours < 0.1) {
		            lastNoContours = noContours;
				} else{
		            break;
		        }
			}
		}
		//mask.convertTo(mask, CvType.CV_32F);
		return mask;
	}
	
	private void getAllPatches(Mat mask,ArrayList<Rect> patches){
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	    Imgproc.findContours(mask.clone(), contours,hierarchy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_NONE );
	    for (MatOfPoint contour : contours) {
	    	double arcPercentage = 0.01;
			MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
			double epsilon = Imgproc.arcLength(curve,true) * arcPercentage;
			MatOfPoint2f corners = new MatOfPoint2f();
			Imgproc.approxPolyDP(curve,corners, epsilon, true);
			
			MatOfPoint approxf1 = new MatOfPoint();
			corners.convertTo(approxf1, CvType.CV_32S);
			Rect r = Imgproc.boundingRect(approxf1);
			System.out.println("getAllPatches rect " + r.toString());
			int currentArea = r.width * r.height;
			if (currentArea > 1){
				patches.add(new Rect(r.x,r.y,r.width,r.height));
			}
	    }
	}
	
	private void getBestPatches(Mat sourceImg, Mat checkImg, ArrayList<Rect> patches, 
			ArrayList<Rect> bestPatches,double threshold) {
		for (Rect r : patches) {
			Mat patch = new Mat();
			Mat imgROI = new Mat(sourceImg, r);
			imgROI.copyTo(patch);
			MinMaxLocResult mmr = getBestMatch(checkImg, patch);
			if (mmr.maxVal < threshold) {
				bestPatches.add(new Rect(r.x,r.y,r.width,r.height));
			}
		}
	}
	
	private void getBestPatchesAuto(Mat sourceImg, Mat checkImg, ArrayList<Rect> patches, 
			ArrayList<Rect> bestPatches) {
		for (int th = 0;th < 100;th ++) {
			double threshold = (th + 1) / 100.0;
			getBestPatches(sourceImg, checkImg, patches,bestPatches, threshold);
			if (bestPatches.size() > 0) {
				break;
			}
		}
	}
	
	public ArrayList<Rect> getImageDiffArea(Mat img1,Mat img2) {
		Mat dst1 = new Mat();
		Mat dst2 = new Mat();
		downscaleImages(img1,dst1,img2,dst2);
		img1 = dst1;
		img2 = matchRotation(dst1, dst2);
		scaleImages(img1, img2,dst1,dst2);
		System.out.println("scaleImages dst1 w " + dst1.width() + " dst1 h " + dst1.height());
		System.out.println("scaleImages dst2 w " + dst2.width() + " dst2 h " + dst2.height());
		Mat lcmat = locationCorrection(dst1, dst2) ;
		img2 = lcmat == null ? dst2 : lcmat;
		img1 = dst1; 
		Imgproc.cvtColor(img1,dst1,Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(img2,dst2,Imgproc.COLOR_BGR2GRAY);
		
		Mat mask = getMask(dst1, dst2);
		ArrayList<Rect> patches = new ArrayList<Rect>();
		getAllPatches(mask,patches);
		
		ArrayList<Rect> bestPatches = new ArrayList<Rect>();
		normaliseImages(dst1,img1, dst2,img2);
		getBestPatchesAuto(img1, img2, patches,bestPatches);
		
		return bestPatches;
	}
	
	public Mat getImageDiffMat(Mat img1,Mat img2) {
		Mat dst1 = new Mat();
		Mat dst2 = new Mat();
		downscaleImages(img1,dst1,img2,dst2);
		img1 = dst1;
		
		img2 = matchRotation(dst1, dst2);
		scaleImages(img1, img2,dst1,dst2);
		img2 = locationCorrection(dst1, dst2);
		img1 = dst1; 
		Imgproc.cvtColor(img1,dst1,Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(img2,dst2,Imgproc.COLOR_BGR2GRAY);
		return dst2;
//		Mat mask = getMask(dst1, dst2);
//		return mask;
//		ArrayList<Rect> patches = new ArrayList<Rect>();
//		getAllPatches(mask,patches);
//		
//		ArrayList<Rect> bestPatches = new ArrayList<Rect>();
//		normaliseImages(dst1,img1, dst2,img2);
//		getBestPatchesAuto(img1, img2, patches,bestPatches);
//		
//		return img2;
	}
}