package com.zcyuan.opencv.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.HOGDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.zcyuan.darknet.Detectjni;
import com.zcyuan.opencv.common.utils.Constants;
import com.zcyuan.opencv.common.web.BaseController;
import com.zcyuan.opencv.mail.MailSendService;
import com.zcyuan.opencv.utils.ImageCompare;

/**
 */
@Controller
@RequestMapping(value = "demo")
public class DemoController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(DemoController.class);
	private CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();

	private String getResourcePath(String cascadeName) {
		String resourcePath = getClass().getResource("/" + cascadeName).getPath().substring(1);
		resourcePath = resourcePath.charAt(1) == ':' ? resourcePath : "/" + resourcePath;
		return resourcePath;
	}

	@RequestMapping(value = "detectFace")
	public void detectFace(HttpServletResponse response, HttpServletRequest request, String url) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		} catch (UnsatisfiedLinkError ignore) {
		}
		System.out.println("===========java.library.path:" + System.getProperty("java.library.path"));
		logger.info("\nRunning DetectFaceDemo");
		String resourcePath = getResourcePath("lbpcascade_frontalface.xml");
		logger.info("resourcePath============" + resourcePath);

		CascadeClassifier faceDetector = new CascadeClassifier(resourcePath);
		logger.info("url==============" + Constants.PATH + url);
		Mat image = Imgcodecs.imread(Constants.PATH + url);
		// Detect faces in the image.
		// MatOfRect is a special container class for Rect.
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);

		logger.info(String.format("Detected %s faces", faceDetections.toArray().length));
		// Draw a bounding box around each face.
		for (Rect rect : faceDetections.toArray()) {
			Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 255, 0));
		}

		// Save the visualized detection.
		String filename = url.substring(url.lastIndexOf("/"), url.length());
		System.out.println(String.format("Writing %s", Constants.PATH + Constants.DEST_IMAGE_PATH + filename));
		Imgcodecs.imwrite(Constants.PATH + Constants.DEST_IMAGE_PATH + filename, image);
		renderString(response, Constants.SUCCESS);
	}

//	E:/developKits/eclipse/eclipse-workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/web_opencv/WEB-INF/classes/exe/darknet.exe detector test 
//	E:/developKits/eclipse/eclipse-workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/web_opencv/WEB-INF/classes/exe/data/coco.data
//	E:/developKits/eclipse/eclipse-workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/web_opencv/WEB-INF/classes/exe/cfg/yolov3.cfg 
//	E:/developKits/eclipse/eclipse-workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/web_opencv/WEB-INF/classes/exe/yolov3.weights 
//	E:\developKits\eclipse\eclipse-workspace\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\web_opencv\/statics/sourceimage/human.jpg 
//	-dont_show 1 -out E:\developKits\eclipse\eclipse-workspace\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\web_opencv\\statics\destimage\/human.jpg
	@RequestMapping(value = "detectThings")
	public void detectThings(HttpServletResponse response, HttpServletRequest request, String url) {
		String exePath = getResourcePath("exe/darknet.exe");
		String cfgPath = getResourcePath("exe/cfg/yolov3.cfg");
		String weightsPath = getResourcePath("exe/yolov3.weights");
		String dataPath = getResourcePath("exe/data/coco.data");
		url = url.replaceAll("\\\\", "/");
		if (url.startsWith("/"))
			url = Constants.PATH + url;

		logger.info("detectThings url===" + url);
		String filename = url.substring(url.lastIndexOf("/"), url.length());
		String filePath = Constants.PATH + Constants.DEST_IMAGE_PATH;

		try {
			ProcessBuilder processBuilder = new ProcessBuilder();
			// darknet.exe detector test data\coco.data .\cfg\yolov3.cfg yolov3.weights
			// .\data\dog.jpg -dont_show 1 -out filePath
			String extCmd = exePath + " detector test " + dataPath + " " + cfgPath + " " + weightsPath + " " + url
					+ " -dont_show 1" + " -out " + filePath;
			System.out.println("extCmd " + extCmd);
			processBuilder.command(exePath, "detector", "test", dataPath, cfgPath, weightsPath, url, "-dont_show", "1",
					"-out", filePath);
			System.out.println("" + processBuilder.command());

			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			InputStream inputStream = process.getInputStream();
			InputStreamReader reader = new InputStreamReader(inputStream, "gbk");

			char[] chars = new char[1024];
			int len = -1;
			while ((len = reader.read(chars)) != -1) {
				String string = new String(chars, 0, len);
				System.out.println(string);
			}

			inputStream.close();
			reader.close();

			Map<String, Object> result = new HashMap<String, Object>();
			result.put("success", "success");
			result.put("path", Constants.DEST_IMAGE_PATH + filename);
			result.put("matchNum", 1);
			renderString(response, result);

		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "detectHouse")
	public void detectHouse(HttpServletResponse response, HttpServletRequest request, String url) {

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", "success");
		result.put("path", "");
		result.put("matchNum", 0);
		renderString(response, result);
	}

//https://github.com/reu2018DL/YOLO-LITE/
//https://github.com/AlexeyAB/darknet/tree/master/cfg
//https://blog.csdn.net/qq_22709065/article/details/71601730
	@RequestMapping(value = "detectHuman")
	public void detectHuman(HttpServletResponse response, HttpServletRequest request, String url) {
		int size = 416;
		boolean isHog = false;
		int detectNum = 0;
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		} catch (UnsatisfiedLinkError ignore) {
		}
		url = url.replaceAll("\\\\", "/");
		if (url.startsWith("/"))
			url = Constants.PATH + url;
		logger.info("detectHuman url===" + url);

		Mat image = Imgcodecs.imread(url);
		int w = image.width() > size ? size : image.width();
		int h = w * image.height() / image.width();
		Imgproc.resize(image, image, new Size(w, h));
		Mat gray = new Mat();
		Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

		if (!isHog) {
			Net net = Dnn.readNetFromDarknet(getResourcePath("yolo/yolov2.cfg"),
					getResourcePath("yolo/yolov2.weights"));
			if (net.empty()) {
				System.out.println("Reading Net error");
				isHog = true;
			} else {
				Size sz = new Size(size, size);
				float scale = 1.0F / 255.0F;
				Mat inputBlob = Dnn.blobFromImage(image, scale, sz, new Scalar(0), true, false);

				net.setInput(inputBlob, "data");
				List<Mat> outputBlobs = new ArrayList<Mat>();

				MatOfInt outLayers = net.getUnconnectedOutLayers();
				int[] layers = outLayers.toArray();
				List<String> layersNames = net.getLayerNames();
				ArrayList<String> outBlobNames = new ArrayList<String>();
				for (int i = 0; i < layers.length; ++i) {
					outBlobNames.add(layersNames.get(layers[i] - 1));
				}
				if (outBlobNames.size() == 1) {
					Mat detectionMat = net.forward(outBlobNames.get(0));
					outputBlobs.add(detectionMat);
				} else {
					net.forward(outputBlobs, outBlobNames);
				}
				for (Mat detectionMat : outputBlobs) {
					System.out.println("detectionMat " + detectionMat);
					for (int i = 0; i < detectionMat.rows(); i++) {
						int probability_index = 5;
						int s = (int) (detectionMat.cols() * detectionMat.channels());
						float[] data = new float[s];
						detectionMat.get(i, 0, data);
						float confidence = -1;
						int objectClass = -1;
						for (int j = 0; j < detectionMat.cols(); j++) {
							if (j >= probability_index && confidence < data[j]) {
								confidence = data[j];
								objectClass = j - probability_index;
								if (confidence > 0)
									System.out.println("j: " + j + "  confidence " + confidence);
							}
						}

						if (confidence > 0.3 && objectClass == 0) {
							float x = data[0];
							float y = data[1];
							float width = data[2];
							float height = data[3];
							float xLeftBottom = (x - width / 2) * image.cols();
							float yLeftBottom = (y - height / 2) * image.rows();
							float xRightTop = (x + width / 2) * image.cols();
							float yRightTop = (y + height / 2) * image.rows();
							System.out.println("ROI: " + xLeftBottom + " " + yLeftBottom + " " + xRightTop + " "
									+ yRightTop + "\n");

							detectNum++;
							Imgproc.rectangle(image, new Point(xLeftBottom, yLeftBottom),
									new Point(xRightTop, yRightTop), new Scalar(0, 255, 0), 2);
						}
					}
				}
			}
		}
		if (isHog) {
			HOGDescriptor hog = new HOGDescriptor();
			hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
			MatOfRect regions = new MatOfRect();
			MatOfDouble foundWeights = new MatOfDouble();
			hog.detectMultiScale(gray, regions, foundWeights, 1.05, new Size(4, 4), new Size(8, 8));
			logger.info(String.format("Detected %s humans", regions.toArray().length));
			for (Rect rect : regions.toArray()) {
				if (rect.width < 10 && rect.height < 10)
					continue;
				detectNum++;
				Imgproc.rectangle(image, new Point(rect.x, rect.y),
						new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 2);
			}
		} else {
		}
		// Save the visualized detection.
		String filename = url.substring(url.lastIndexOf("/"), url.length());
		String filePath = Constants.PATH + Constants.DEST_IMAGE_PATH + filename;
		logger.info(String.format("Writing %s", filePath));
		Imgcodecs.imwrite(filePath, image);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", "success");
		result.put("path", Constants.DEST_IMAGE_PATH + filename);
		result.put("matchNum", detectNum);
		renderString(response, result);
	}

	@RequestMapping(value = "detectExcavator")
	public void detectExcavator(HttpServletResponse response, HttpServletRequest request, String url) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		} catch (UnsatisfiedLinkError ignore) {
		}
		logger.info("\nRunning detectExcavatorDemo  " + Core.NATIVE_LIBRARY_NAME);
		String resourcePath = getResourcePath("excavator/cascade.xml");

		CascadeClassifier detector = new CascadeClassifier(resourcePath);
		url = url.replaceAll("\\\\", "/");
		if (url.startsWith("/"))
			url = Constants.PATH + url;
		logger.info("detectExcavator url======" + url);
		Mat image = Imgcodecs.imread(url);
		int w = image.width() > 400 ? 400 : image.width();
		int h = w * image.height() / image.width();
		float scale = (float) (image.width() * 1.0 / w);
		Mat resizeImage = new Mat();
		Imgproc.resize(image, resizeImage, new Size(w, h));
		Mat gray = new Mat();
		Imgproc.cvtColor(resizeImage, gray, Imgproc.COLOR_BGR2GRAY);

		MatOfRect detections = new MatOfRect();
		detector.detectMultiScale(image, detections);

		logger.info(String.format("Detected %s Excavators", detections.toArray().length));
		// Draw a bounding box around each face.
		for (Rect rect : detections.toArray()) {
			logger.info("orgin rect = " + rect.toString());
			if (rect.width < 10 && rect.height < 10)
				continue;
			Rect rc = new Rect((int) (rect.x * scale), (int) (rect.y * scale), (int) (rect.width * scale),
					(int) (rect.height * scale));
			logger.info("after rect = " + rc.toString());
			Imgproc.rectangle(image, new Point(rc.x, rc.y), new Point(rc.x + rc.width, rc.y + rc.height),
					new Scalar(0, 0, 255), 3);
		}

		// Save the visualized detection.
		String filename = url.substring(url.lastIndexOf("/"), url.length());
		String filePath = Constants.PATH + Constants.DEST_IMAGE_PATH + filename;
		logger.info(String.format("Writing %s", filePath));
		Imgcodecs.imwrite(filePath, image);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", "success");
		result.put("path", Constants.DEST_IMAGE_PATH + filename);
		result.put("matchNum", detections.toArray().length);
		renderString(response, result);
	}

	@RequestMapping(value = "matchdiff")
	public void matchdiff(HttpServletResponse response, HttpServletRequest request, String url, String url2) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		} catch (UnsatisfiedLinkError ignore) {
		}

		logger.info("\nRunning matchdiffDemo");
		ImageCompare ic = new ImageCompare();
		url = url.replaceAll("\\\\", "/");
		if (url.startsWith("/"))
			url = Constants.PATH + url;

		url2 = url2.replaceAll("\\\\", "/");
		if (url2.startsWith("/"))
			url2 = Constants.PATH + url2;

		logger.info("url==============" + url);
		logger.info("url2==============" + url2);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", "success");
		Mat mat = ic.CompareAndMarkDiff2(url, url2);
		if (mat == null) {
			result.put("success", "failure");
		} else {
			String filename = url2.substring(url2.lastIndexOf("/"), url2.length());
			String filePath = Constants.PATH + Constants.DEST_IMAGE_PATH + filename;
			System.out.println(String.format("Writing %s", filePath));
			Imgcodecs.imwrite(filePath, mat);
			result.put("path", filePath);
		}
		renderString(response, result);
	}

	static int uploadCount = 0;

	@RequestMapping(value = "doSaveFile", method = RequestMethod.POST)
	public void doSaveFile(HttpServletResponse response, MultipartHttpServletRequest request, String suffixName) {
		String dirStr = Constants.PATH + Constants.UPLOAD_IMAGE_PATH;
		File dir = new File(dirStr);
		if (!dir.exists()) {
			dir.mkdir();
		}

		java.util.Iterator<String> itr = request.getFileNames();
		String filtPath = "";
		while (itr.hasNext()) {
			String str = itr.next();
			MultipartFile mf = (CommonsMultipartFile) request.getFile(str);
			String fileName = null;
			suffixName += uploadCount++ % 10;
			try {
				fileName = new String(mf.getOriginalFilename().getBytes("iso-8859-1"), "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("======doSaveFile=====filename:" + fileName);
			if (fileName == null)
				break;
			String fileType = fileName.substring(fileName.lastIndexOf('.'));

			String newFileName = dirStr + "upload" + suffixName + fileType;
			File saveFile = new File(newFileName);
			if (saveFile.exists()) {
				saveFile.delete();
			}
			try {
				FileCopyUtils.copy(mf.getBytes(), saveFile);
				filtPath = Constants.UPLOAD_IMAGE_PATH + "upload" + suffixName + fileType;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("=======doSaveFile===new=filename:" + newFileName);
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", "success");
		result.put("path", filtPath);
		renderString(response, result);
	}

	private String yoloDetect(String url) {
		int size = 416;
		int detectNum = 0;
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		} catch (UnsatisfiedLinkError ignore) {
		}
		url = url.replaceAll("\\\\", "/");
		if (url.startsWith("/"))
			url = Constants.PATH + url;
		logger.info("yoloDetect url===" + url);

		Mat image = Imgcodecs.imread(url);
		int w = size;//image.width() > size ? size : image.width();
		int h = size;//w * image.height() / image.width();
		float imgscale = image.width() * 1.0f / w;
		Mat resize = new Mat();
		Imgproc.resize(image, resize, new Size(w, h));
		//Mat gray = new Mat();
		//Imgproc.cvtColor(resize, gray, Imgproc.COLOR_BGR2GRAY);

		Net net = Dnn.readNetFromDarknet(getResourcePath("yolo/yolov2.cfg"), getResourcePath("yolo/yolov2.weights"));
		if (net.empty()) {
			System.out.println("Reading Net error");
			return null;
		} else {
			Size sz = new Size(size, size);
			float scale = 1.0F / 255.0F;
			
			Mat inputBlob = Dnn.blobFromImage(resize, scale, sz, new Scalar(0), true, true);

			net.setInput(inputBlob, "data");
			List<Mat> outputBlobs = new ArrayList<Mat>();

			MatOfInt outLayers = net.getUnconnectedOutLayers();
			int[] layers = outLayers.toArray();
			List<String> layersNames = net.getLayerNames();
			ArrayList<String> outBlobNames = new ArrayList<String>();
			for (int i = 0; i < layers.length; ++i) {
				outBlobNames.add(layersNames.get(layers[i] - 1));
			}
			if (outBlobNames.size() == 1) {
				Mat detectionMat = net.forward(outBlobNames.get(0));
				outputBlobs.add(detectionMat);
			} else {
				net.forward(outputBlobs, outBlobNames);
			}
			for (Mat detectionMat : outputBlobs) {
				System.out.println("detectionMat " + detectionMat);
				for (int i = 0; i < detectionMat.rows(); i++) {
					int probability_index = 5;
					int s = (int) (detectionMat.cols() * detectionMat.channels());
					float[] data = new float[s];
					detectionMat.get(i, 0, data);
					float confidence = -1;
					int objectClass = -1;
					for (int j = 0; j < detectionMat.cols(); j++) {
						if (j >= probability_index && confidence < data[j]) {
							confidence = data[j];
							objectClass = j - probability_index;
							if (confidence > 0)
								System.out.println("j: " + j + "  confidence " + confidence);
						}
					}

					if (confidence > 0.2 && objectClass == 0) {
						float x = data[0];
						float y = data[1];
						float width = data[2];
						float height = data[3];
						float xLeftBottom = (x - width / 2) * resize.cols();
						float yLeftBottom = (y - height / 2) * resize.rows();
						float xRightTop = (x + width / 2) * resize.cols();
						float yRightTop = (y + height / 2) * resize.rows();
						logger.info("ROI: " + xLeftBottom + " " + yLeftBottom + " " + xRightTop + " " + yRightTop + "\n");

						detectNum++;
						Imgproc.rectangle(image, new Point(xLeftBottom * imgscale, yLeftBottom * imgscale), new Point(xRightTop * imgscale, yRightTop * imgscale),
								new Scalar(0, 255, 0), 2);
					}
				}
			}
		}
		// Save the visualized detection.
		String filename = url.substring(url.lastIndexOf("/") + 1, url.length());
		String filePath = Constants.PATH + Constants.DEST_IMAGE_PATH + filename;
		logger.info(String.format("Writing %s", filePath));
		Imgcodecs.imwrite(filePath, image);
		return Constants.DEST_IMAGE_PATH + filename;
	}
	
	private String darknetDetect(String url) {
		try {
			System.loadLibrary("opencv_world343");
			System.loadLibrary("pthreadGC2");
			System.loadLibrary("pthreadVC2");
			System.loadLibrary("darknet");
		} catch (UnsatisfiedLinkError ignore) {
			ignore.printStackTrace();
		}
		url = url.replaceAll("\\\\", "/");
		if (url.startsWith("/"))
			url = Constants.PATH + url;
		logger.info("darknetDetect url===" + url);

		Detectjni detectjni = new Detectjni();
		String basefile = getResourcePath("yolo/exe/");
		String outfile = Constants.PATH + Constants.DEST_IMAGE_PATH;
		detectjni.darknetTestDetector("data/coco.data", basefile, "cfg/yolov3.cfg", "yolov3.weights", url, 0.24f, 0.5f, outfile);
		// Save the visualized detection.
		String filename = url.substring(url.lastIndexOf("/") + 1, url.length());
		return Constants.DEST_IMAGE_PATH + filename;
	}
	
	@RequestMapping(value = "doAutoDetectObject", method = RequestMethod.POST)
	public void doAutoDetectObject(HttpServletResponse response, MultipartHttpServletRequest request,
			String suffixName) {
		String dirStr = Constants.PATH + Constants.UPLOAD_IMAGE_PATH;
		File dir = new File(dirStr);
		if (!dir.exists()) {
			dir.mkdir();
		}
		String mails = "";
		String values[] = suffixName.split("\\*\\*");
		if (values.length > 1) {
			suffixName = values[0];
			mails = values[1];
			logger.info("======suffixName==:" + suffixName + ":mails:" + mails);
		}
		suffixName = suffixName.replace("*", "");
		java.util.Iterator<String> itr = request.getFileNames();
		String filtPath = "";
		String newFileName = null;
		String fileName = null;
		while (itr.hasNext()) {
			String str = itr.next();
			MultipartFile mf = (CommonsMultipartFile) request.getFile(str);
			
			suffixName += uploadCount++ % 10;
			try {
				fileName = new String(mf.getOriginalFilename().getBytes("iso-8859-1"), "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			logger.info("======doSaveFile=====filename:" + fileName);
			if (fileName == null)
				break;
			String fileType = fileName.substring(fileName.lastIndexOf('.'));

			newFileName = dirStr + "upload" + suffixName + fileType;
			File saveFile = new File(newFileName);
			if (saveFile.exists()) {
				saveFile.delete();
			}
			try {
				FileCopyUtils.copy(mf.getBytes(), saveFile);
				filtPath = Constants.UPLOAD_IMAGE_PATH + "upload" + suffixName + fileType;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("=======doSaveFile===new=filename:" + newFileName);
		}
		Map<String, Object> result = new HashMap<String, Object>();
		if (filtPath != "") {
			String ret = darknetDetect(newFileName);
			result.put("cpath", ret);
			if (mails.length() > 1 && mails.contains("@")) {
				MailSendService mailSender = new MailSendService();
				mailSender.sendMailWithImg(mails, "test", "test",Constants.PATH + ret,fileName);
			}
		}
		result.put("success", "success");
		result.put("path", filtPath);
		renderString(response, result);
	}
}
