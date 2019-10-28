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
			
			//识别
			$("#detectHuman").click(function(){
				var url = baseImageFile;
				var fileName=$("#oldimg").attr("resrc");
				if (fileName.length > 0){
					url = fileName;
				}
				$.SaveForm({
					url : ctxPath+"/demo/detectHuman?_" + $.now(),
					param: {"url":url},
					json:true,
					success : function(result) {
						$("#newimg").attr("src",baseUrl + result.path); 
						$("#newimg").attr("style","width:100%;");
						layer.msg(result.matchNum + '个匹配', {icon: 1});
					}
				}); 
			});
			
			//上传
			$("#upload").change(function(){
				var fileName=$("#upload").val();
				
				var formData = new FormData($("#upForm")[0]);
			    $.ajax({
			        url:ctxPath+"/demo/doSaveFile?suffixName=" + "_human",
			        dataType:'json',
			        type:'POST',
			        async: false,
			        data: formData,
			        processData : false, // 使数据不做处理
			        contentType : false, // 不要设置Content-Type请求头
			        success: function(data){
			        	if (data.path != 0){
			        		$("#oldimg").attr("src",'');
			        		$("#oldimg").attr("src",baseUrl + data.path);
			        		$("#oldimg").attr("resrc",data.path);
							$("#newimg").attr("src",'');
							layer.msg('文件上传成功', {icon: 1});
			        	}else{
			        		layer.msg('文件上传失败', {icon: 1});
			        	}
			        },
			        error:function(response){
			        	layer.msg('文件上传失败', {icon: 1});
			        }
			    });
			});
			
		});
	</script>
</head>
<body>
<h3>
     人体识别demo
</h3>	
     		<div class="row">
     			<div class="col-sm-6">
     				<form enctype="multipart/form-data" id="upForm">
     					<input name="upload" id="upload" style="width:100%;height:32px;"  type="file" accept="image/jpg, image/png"/>
     				</form>
     			</div>
     			<div class="col-sm-6">
			    	<a class="btn btn-info"  id="detectHuman" style="margin:0px 0px 10px 0px;"><i class="fa fa-object-ungroup" ></i>识别</a>
			    </div>
			</div>
	
			<div class="row">
			    <div class="col-sm-6">
			      <div class="box box-primary">
			        <div class="box-header with-border">
			          <h3 class="box-title">原图</h3>
			          <span class="label"><i class="fa fa-html5"></i></span>
			        </div><!-- /.box-header -->
			        <div class="box-body">
		          		<img id="oldimg" src=""  alt="原图" />
			        </div><!-- /.box-body -->
			      </div><!-- /.box -->
			    </div><!-- /.col -->
			    <div class="col-sm-6">
			      <div class="box box-danger">
			        <div class="box-header with-border">
			          <h3 class="box-title">识别后的图片</h3>
			          <span class="label"><i class="fa fa-database"></i></span>
			        </div><!-- /.box-header -->
			        <div class="box-body">
			          <img  id="newimg" src=""  alt="识别后的图" />

			        </div><!-- /.box-body -->
			      </div><!-- /.box -->
			    </div><!-- /.col -->
			  </div>
</body>
</html>
