<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@include file="/module/include/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
<sys:header title="首页" extLibs=""></sys:header>
<script type="text/javascript">
		$(function(){
			var baseImageFile = "/statics/sourceimage/human.jpg"
			var newImageFile = "/statics/destimage/human.jpg"
			$("#oldimg").attr("src",baseUrl+baseImageFile);
			$("#oldimg").attr("resrc",baseImageFile);
			$("#oldimg").attr("style","width:100%;");
			
			//上传
			$("#file").change(function(){
				document.getElementById('filename').value=document.getElementById('file').value;
				var check = document.getElementsByTagName('input')[0];
				var mails = document.getElementById("mailsel").checked==false ? "" : document.getElementById("emaiaddr").value;
	             for(var i=0; i<$('#file')[0].files.length;i++){
	            	var formData = new FormData();
	                formData.append('file[' + i + ']', $('#file')[0].files[i]);
	                $.ajax({
				        url:ctxPath+"/demo/doAutoDetectObject?suffixName=" + "_object" + "**" + mails,
				        dataType:'json',
				        type:'POST',
				        async: true,
				        data: formData,
				        processData : false, // 使数据不做处理
				        contentType : false, // 不要设置Content-Type请求头
				        success: function(data){
				        	if (data.path != 0){
				        		$("#oldimg").attr("src",'');
				        		$("#oldimg").attr("src",baseUrl + data.path);
				        		$("#oldimg").attr("resrc",data.path);
				        		if (data.cpath != 0){
				        			$("#newimg").attr("src",baseUrl + data.cpath);
				        			$("#newimg").attr("style","width:100%;");
				        		}
				        	}else{
				        		layer.msg('文件上传失败', {icon: 1});
				        	}
				        },
				        error:function(response){
				        	layer.msg('文件上传失败', {icon: 1});
				        }
				    });
	             }
			});
		});
		function openfolder(){
			document.getElementById('file').click();
		}
	</script>
</head>
<body>
	<input type="text" name="filename" id="filename" placeholder="图像文件路径"
		style="width: 50%; margin: 12px 0px 20px 20px; padding: 4px 12px 6px 12px;" />
	<form enctype="multipart/form-data" id="upForm" ACTION=""
		style="display: inline">
		<input type="button" id="seleimgs" value="摄像头图像"
			onclick="openfolder()" style="margin: 12px 0px 20px 10px;" /> 
		<input type="file" name="file[]" id="file" style="display: none"
			multiple="multiple" accept="image/jpg, image/png" />
	</form>
	<input type="button" value="停止" style="margin: 12px 0px 20px 10px;" />

	<div class="row">
		<div class="col-sm-6">
			<div class="box box-primary">
				<div class="box-header with-border">
					<span class="label"><i class="fa fa-html5"></i></span>
				</div>
				<!-- /.box-header -->
				<div class="box-body">
					<img id="oldimg" src="" alt="" />
				</div>
				<!-- /.box-body -->
			</div>
			<!-- /.box -->
		</div>
		<!-- /.col -->
		<div class="col-sm-6">
			<div class="box box-danger">
				<div class="box-header with-border">
					<span class="label"><i class="fa fa-database"></i></span>
				</div>
				<!-- /.box-header -->
				<div class="box-body">
					<img id="newimg" src="" alt="" />

				</div>
				<!-- /.box-body -->
			</div>
			<!-- /.box -->
		</div>
		<!-- /.col -->
	</div>
	<input type="checkbox" id="mailsel" name="mailsel"
		style="margin: 0px 10px 0px 20px;">自动发送违建信息
	<input type="text" name="emaiaddr" id="emaiaddr"
		placeholder="请输入接收信息的邮箱,多个邮箱用;号隔开" value="chaotictimes@163.com"
		style="width: 75%; margin: 0px 0px 0px 20px; padding: 4px 12px 6px 12px;" />
	<h4 style="line-height: 24px; letter-spacing: 3px;">
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;自动识别工程车（重型运输车辆、大型吊车、挖掘机、推土机），自动识别聚集人群（3人以上）。信息是否需要上报，在后台可以针对各个摄像头分别进行设置，
		例如摄像头1只上报工程车识别结果，而不上报聚集人群信息等。</h4>
</body>
</html>
