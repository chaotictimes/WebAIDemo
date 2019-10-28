<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@include file="/module/include/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <sys:header title="首页" extLibs=""></sys:header>
    <script type="text/javascript">
		$(function(){
			var baseImageFile = "/statics/sourceimage/diji4.jpg"
			var newImageFile = "/statics/sourceimage/diji4.jpg"
			$("#oldimg").attr("src",baseUrl+baseImageFile);
			$("#oldimg").attr("style","width:100%;");
			$("#newimg").attr("src",baseUrl+newImageFile);
			$("#newimg").attr("style","width:100%;");
			//识别
			$("#matchdiff").click(function(){
				var url = baseImageFile;
				var url2 = newImageFile;
				var fileName=$("#upload").val();
				if (fileName.length > 0){
					url = fileName;
				}
				var fileName2=$("#upload2").val();
				if (fileName2.length > 0){
					url2 = fileName2;
				}
				$.SaveForm({
					url : ctxPath+"/demo/matchdiff?_" + $.now(),
					param: {"url":url,"url2":url2},
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
				var fileName=$("#upload").val();
				$("#oldimg").attr("src",fileName);
			});
			$("#upload2").change(function(){
				var fileName=$("#upload2").val();
				$("#newimg").attr("src",fileName);
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
     动土检测demo
</h3>	
     		<div class="row">
     			<div class="col-sm-6">
     				<input name="upload" id="upload" style="width:100%;height:32px;" οnchange="uploadImg(this)" type="file" accept="image/jpg, image/png"/>
     			</div>
     			<div class="col-sm-6">
     				<input name="upload" id="upload2" style="width:100%;height:32px;" οnchange="uploadImg2(this)" type="file" accept="image/jpg, image/png"/>
			    	<a class="btn btn-info"  id="matchdiff" style="margin:10px 0px 10px 0px;"><i class="fa fa-object-ungroup" ></i>识别</a>
			    </div>
			</div>
	
			<div class="row">
			    <div class="col-sm-6">
			      <div class="box box-primary">
			        <div class="box-header with-border">
			          <span class="label"><i class="fa fa-html5"></i></span>
			        </div><!-- /.box-header -->
			        <div class="box-body">
		          		<img id="oldimg" src=""  alt="" />
			        </div><!-- /.box-body -->
			      </div><!-- /.box -->
			    </div><!-- /.col -->
			    <div class="col-sm-6">
			      <div class="box box-danger">
			        <div class="box-header with-border">
			          <span class="label"><i class="fa fa-database"></i></span>
			        </div><!-- /.box-header -->
			        <div class="box-body">
			          <img  id="newimg" src=""  alt="" />

			        </div><!-- /.box-body -->
			      </div><!-- /.box -->
			    </div><!-- /.col -->
			  </div>
</body>
</html>
