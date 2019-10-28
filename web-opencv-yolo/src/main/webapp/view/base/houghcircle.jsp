<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@include file="/module/include/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <sys:header title="首页" extLibs=""></sys:header>
	<link rel="stylesheet" href="${ctxStatic}/plugins/bootstrap-slider/slider.css">
	<link rel="stylesheet" href="${ctxStatic}/plugins/iCheck/all.css?t=${version}">
	<link rel="stylesheet" href="${ctxStatic}/plugins/iCheck/minimal/blue.css?t=${version}">
    <script type="text/javascript">
		$(function(){
			var baseImageFile = "/statics/sourceimage/line1.png"
			//var baseImageFile = "/statics/sourceimage/xx1.jpg"
			var newImagePath = "/statics/distimage/lena.png"
			$("#oldimg").attr("src",baseUrl+baseImageFile);
			//$("#newimg").attr("src",baseUrl+baseImageFile); 
			
			//缩放
			$("#houghcircle").click(function(){
				var minDist = $("#minDist").val();
				var param1 = $("#param1").val();
				var param2 = $("#param2").val();
				var minRadius = $("#minRadius").val();
				var maxRadius = $("#maxRadius").val();
				var imagefile = baseImageFile;
				$("#oldimg").attr("src",baseUrl+baseImageFile);
				var srcurl = ctxPath+"/base/houghcircle?_" + $.now()+"&minDist="+minDist+"&param1="+param1+"&param2="+param2+
						"&minRadius="+minRadius+"&maxRadius="+maxRadius+"&imagefile="+imagefile;
				$("#newimg").attr("src",srcurl);
			});
			
			//重置
			$("#reset").click(function(){
				var baseImageFile = "/statics/sourceimage/line1.png";
				$("#oldimg").attr("src",baseUrl+baseImageFile);
				$("#newimg").attr("src",'');
				layer.msg('重置成功!', {icon: 1});
			});
			
			//滑动插件加载
			$("#minDist,#param1,#param2,#minRadius,#maxRadius").slider({
				tooltip: 'always',
			});
			
 			$("#minDist").on("slide", function(slideEvt) {
				console.log(slideEvt.value);
				//$("#binary").click();
			}).on("change", function (e) {  
			    //当值发生改变的时候触发  
			    //console.info(e);  
			    //获取旧值和新值  
			    console.info(e.value.oldValue + '--' + e.value.newValue); 
			    //需要赋值，否则获取的有可能是变化之前的值
			    $("#minDist").val(e.value.newValue);
			    $("#houghcircle").click();
			});
 			
 			$("#param1").on("slide", function(slideEvt) {
				console.log(slideEvt.value);
				//$("#binary").click();
			}).on("change", function (e) {  
			    //当值发生改变的时候触发  
			    //console.info(e);  
			    //获取旧值和新值  
			    console.info(e.value.oldValue + '--' + e.value.newValue); 
			    //需要赋值，否则获取的有可能是变化之前的值
			    $("#param1").val(e.value.newValue);
			    $("#houghcircle").click();
			});
 			
 			$("#param2").on("slide", function(slideEvt) {
				console.log(slideEvt.value);
				//$("#binary").click();
			}).on("change", function (e) {  
			    //当值发生改变的时候触发  
			    //console.info(e);  
			    //获取旧值和新值  
			    console.info(e.value.oldValue + '--' + e.value.newValue); 
			    //需要赋值，否则获取的有可能是变化之前的值
			    $("#param2").val(e.value.newValue);
			    $("#houghcircle").click();
			});
 			
 			$("#minRadius").on("slide", function(slideEvt) {
				console.log(slideEvt.value);
				//$("#binary").click();
			}).on("change", function (e) {  
			    //当值发生改变的时候触发  
			    //console.info(e);  
			    //获取旧值和新值  
			    console.info(e.value.oldValue + '--' + e.value.newValue); 
			    //需要赋值，否则获取的有可能是变化之前的值
			    $("#minRadius").val(e.value.newValue);
			    $("#houghcircle").click();
			});
 			
 			$("#maxRadius").on("slide", function(slideEvt) {
				console.log(slideEvt.value);
				//$("#binary").click();
			}).on("change", function (e) {  
			    //当值发生改变的时候触发  
			    //console.info(e);  
			    //获取旧值和新值  
			    console.info(e.value.oldValue + '--' + e.value.newValue); 
			    //需要赋值，否则获取的有可能是变化之前的值
			    $("#maxRadius").val(e.value.newValue);
			    $("#houghcircle").click();
			});
 			
 			
		});
		
		
		
	</script>
</head>
<body>

              <div class="box-group" id="accordion">
                <!-- we are adding the .panel class so bootstrap.js collapse plugin detects it -->
                <div class="panel box box-primary">
                  <div class="box-header with-border">
                    <h4 class="box-title">
                      <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne">
                        	 霍夫圆变换
                      </a>
                    </h4>
                  </div>
                  <div id="collapseOne" class="panel-collapse collapse in"><!--class="panel-collapse collapse in"中的 in 控制展开 -->
                    <div class="box-body">
                    <h4>参考资料：<br>
                    <a href="https://docs.opencv.org/2.4.13.6/modules/imgproc/doc/feature_detection.html#houghcircles">https://docs.opencv.org/2.4.13.6/modules/imgproc/doc/feature_detection.html#houghcircles</a><br>
                    <a href="http://www.opencv.org.cn/opencvdoc/2.3.2/html/doc/tutorials/imgproc/imgtrans/hough_circle/hough_circle.html#hough-circle">霍夫圆变换demo</a><br>
                     霍夫圆变换：Hough Circle 是一种在影像中找出圆的方法，霍夫圆变换的基本原理和霍夫线变换类似， 只是点对应的二维极径极角空间被三维的圆心点x， y还有半径r空间取代。
                     <br>
                     如HoughCircles方法：public static void HoughCircles(Mat image, Mat circles, int method, double dp, double minDist, double param1, double param2, int minRadius, int maxRadius); 各参数说明：</h4>
                      		<table class="table table-bordered">
				                <tbody><tr>
				                  <th style="width: 10px">#</th>
				                  <th style="width: 20%">参数</th>
				                  <th>说明</th>
				                </tr>
				                <tr>
				                  <td>1.</td>
				                  <td>Mat image</td>
				                  <td>源文件Mat对象,单通道 8-bit 灰度图</td>
				                </tr>
				                <tr>
				                  <td>2.</td>
				                  <td>Mat circles</td>
				                  <td>储存着检测到的圆的参数对 (圆心x,圆心y,半径r) 的容器</td>
				                </tr>
				                <tr>
				                  <td>3.</td>
				                  <td>int method</td>
				                  <td>指定检测方法. 现在OpenCV中只有霍夫梯度法 Imgproc.CV_HOUGH_GRADIENT</td>
				                </tr>
				                <tr>
				                  <td>4.</td>
				                  <td>double dp</td>
				                  <td>累加器图像的分辨率，该参数允许创建一个比输入图像分辨率低的累加器。设置为1时，代表同输入图像分辨率相同。该参数不能比1小，如果设置为更大的值（如2），累加器的分辨率会受此影响变小（此情况下为一半）</td>
				                </tr>
				                <tr>
				                  <td>5.</td>
				                  <td>double minDist</td>
				                  <td>检测到圆心之间的最小距离 ，让算法明显区分两个不同圆之间的最小距离</td>
				                </tr>
				                <tr>
				                  <td>6.</td>
				                  <td>double param1</td>
				                  <td>Canny边缘函数的高阈值，但是Canny边缘检测函数还需要有一个低阈值，内部实现里是该值的一半</td>
				                </tr>
				                <tr>
				                  <td>7.</td>
				                  <td>double param2</td>
				                  <td>圆心累加器检测阈值，检测圆需要的最小曲线交点，越小错误率越高。</td>
				                </tr>
				                <tr>
				                  <td>8.</td>
				                  <td>double minRadius</td>
				                  <td>能检测到的最小圆半径, 默认为0</td>
				                </tr>
				                <tr>
				                  <td>9.</td>
				                  <td>double maxRadius</td>
				                  <td>能检测到的最大圆半径, 默认为0</td>
				                </tr>
				                <tr>
				              </tbody></table>
              
              
                      </div>
                  </div>
                </div>
                
                
            </div>
            <!-- /.box-body -->
            
            <h4>&nbsp;&nbsp; 霍夫圆变换</h4>	
    			 <div class="box-body">
    			<table class="table table-bordered">
                <tbody><tr>
                  <th style="width:15%">double minDist</th>
                  <th style="width:15%">double param1</th>
                  <th style="width:15%">double param2</th>
                  <th style="width:15%">double minRadius</th>
                  <th style="width:15%">double maxRadius</th>
<!--                   <th style="width:20%">使用黑白图</th> -->
                  <th style="width: 200px">操作</th>
                </tr>
                <tr>
                  <td>
                  		<input id="minDist" data-slider-id='ex1Slider' type="text" data-slider-min="1" data-slider-max="300" data-slider-step="1" data-slider-value="30"/>
				  </td>
                  <td>
                  		<input id="param1" data-slider-id='ex1Slider' type="text" data-slider-min="1" data-slider-max="300" data-slider-step="1" data-slider-value="200"/>
				  </td>
                  <td>
                  		<input id="param2" data-slider-id='ex1Slider' type="text" data-slider-min="1" data-slider-max="300" data-slider-step="1" data-slider-value="100"/>
                  </td>
                  <td>
                  		<input id="minRadius" data-slider-id='ex1Slider' type="text" data-slider-min="1" data-slider-max="300" data-slider-step="1" data-slider-value="10"/>
				  </td>
                  <td>
                  		<input id="maxRadius" data-slider-id='ex1Slider' type="text" data-slider-min="1" data-slider-max="300" data-slider-step="1" data-slider-value="100"/>
                  </td>
<!--                   <td> -->
<!--                   		<input type="checkbox" name="isBinary" value="1"/>&nbsp;使用黑白图 &nbsp;<br> -->
                  
<!--                   </td> -->
                  <td><a class="btn btn-info"  id="houghcircle"><i class="fa fa-object-ungroup"></i>霍夫圆检测</a>
			          <a class="btn btn-info" id="reset"><i class="fa fa-refresh"></i>重置</a></td>
                </tr>
              </tbody></table></div>
    			
    			
	
			   
			<div class="row">
			    <div class="col-sm-6">
			      <div class="box box-primary">
			        <div class="box-header with-border">
			          <h3 class="box-title">原图</h3>
			          <span class="label label-primary pull-right"><i class="fa fa-html5"></i></span>
			        </div><!-- /.box-header -->
			        <div class="box-body">
			          <p>未识别前的原文件。</p>
		          		<img style="width:auto; height:auto; max-width:100%; max-height:100%"  id="oldimg" src=""  alt="原图" />
			        </div><!-- /.box-body -->
			      </div><!-- /.box -->
			    </div><!-- /.col -->
			    <div class="col-sm-6">
			      <div class="box box-danger">
			        <div class="box-header with-border">
			          <h3 class="box-title">识别后的图片</h3>
			          <span class="label label-danger pull-right"><i class="fa fa-database"></i></span>
			        </div><!-- /.box-header -->
			        <div class="box-body">
			          <p>点击识别按钮后，将显示识别后的文件。</p>
			          <img style="width:auto; height:auto; max-width:100%; max-height:100%"  id="newimg" src=""  alt="此处将展现识别后的图" />

			        </div><!-- /.box-body -->
			      </div><!-- /.box -->
			    </div><!-- /.col -->
			  </div>
						
			
</body>
<script src="${ctxStatic}/plugins/bootstrap-slider/bootstrap-slider.js?t=${version}"></script>
<script src="${ctxStatic}/plugins/iCheck/icheck.js?t=${version}"></script>
</html>
