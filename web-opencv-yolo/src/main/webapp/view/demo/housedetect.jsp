<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@include file="/module/include/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <sys:header title="首页" extLibs=""></sys:header>
    <script type="text/javascript">
		$(function(){
			var baseImageFile = "/statics/sourceimage/house.jpg"
			var newImageFile = "/statics/destimage/house.jpg"
			$("#oldimg").attr("src",baseUrl+baseImageFile);
			$("#oldimg").attr("style","width:100%;");
			
			//识别
			$("#detectHouse").click(function(){
				var url = baseImageFile;
				var fileName=$("#upload").val();
				if (fileName.length > 0){
					url = fileName;
				}
				$.SaveForm({
					url : ctxPath+"/demo/detectHouse?_" + $.now(),
					param: {"url":url},
					json:true,
					success : function(result) {
						$("#newimg").attr("src",result.path); 
						$("#newimg").attr("style","width:100%;");
						layer.msg(result.matchNum + '个匹配', {icon: 1});
					}
				}); 
			});
			
			//上传
			$("#upload").change(function(){
				var baseImageFile = "/statics/sourceimage/lena.png";
				var fileName=$("#upload").val();
				$("#oldimg").attr("src",fileName);
				$("#newimg").attr("src",'');
			});
			
		});
		function uploadImg() {
	        var reads= new FileReader();
	        f=document.getElementById('file').files[0];
	        reads.readAsDataURL(f);
	        reads.onload=function (e) {
	            document.getElementById('show').src=this.result;
	        };
	    }
	</script>
</head>
<body>
<h3>
     房屋识别demo
</h3>	
     		<div class="row">
     			<div class="col-sm-6">
     				<input name="upload" id="upload" style="width:100%;height:32px;" οnchange="uploadImg(this)" type="file" accept="image/jpg, image/png"/>
     			</div>
     			<div class="col-sm-6">
			    	<a class="btn btn-info"  id="detectHouse" style="margin:0px 0px 10px 0px;"><i class="fa fa-object-ungroup" ></i>识别</a>
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
