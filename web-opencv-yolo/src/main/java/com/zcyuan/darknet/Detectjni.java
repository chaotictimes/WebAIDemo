package com.zcyuan.darknet;

public class Detectjni {
	public native void darknetTestDetector(String datacfg, String basefile, String cfgfile, String weightfile, String filename,
			float thresh,float hier_thresh, String outfile);
}