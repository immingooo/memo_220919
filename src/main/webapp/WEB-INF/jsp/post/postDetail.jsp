<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<div class="d-flex justify-content-center">
	<div class="w-50">
		<h1>글 상세/수정</h1>
		
		<%-- Enter로 안할꺼여서 form태그 필요없음 --%>
		<input type="text" id="subject" class="form-control" placeholder="제목을 입력하세요" value="${post.subject}">
		<textarea class="form-control" id="content" placeholder="내용을 입력하세요" rows="15">${post.content}</textarea>
		
		<%-- 이미지가 있을 때만 이미지 영역 추가 --%>
		<c:if test="${not empty post.imagePath}">
		<div class="mt-3">
			<img src="${post.imagePath}" alt="업로드 이미지" width="300px">
		</div>
		</c:if>
		
		<div class="d-flex justify-content-end my-4">
			<input type="file" id="file" accept=".jpg,.jpeg,.png,.gif">
		</div>
		
		<div class="d-flex justify-content-between">
			<button type="button" id="postDeleteBtn" class="btn btn-secondary">삭제</button>
			
			<div>
				<a href="/post/post_list_view" id="postListBtn" class="btn btn-dark">목록으로</a>
				<button type="button" id="postUpdateBtn" class="btn btn-info" data-post-id="${post.id}">수정</button>
			</div>
		</div>
	</div>
</div>

<script>
	$(document).ready(function() {
		// 수정 버튼 클릭
		$('#postUpdateBtn').on('click', function() {
			//alert(1111);
			let subject = $('#subject').val().trim();
			if (subject == '') { // 제목은 필수항목이여서 validation 검사하기
				alert("제목을 입력해주세요");
				return;
			}
			
			let content = $('#content').val();
			console.log(content);
			
			let file = $('#file').val(); 
			console.log(file); // C:\fakepath\cookie-756601_960_720.jpg
			
			// 파일이 새로 업로드 된 경우에 확장자 체크
			if (file != '') {
				let ext = file.split(".").pop().toLowerCase(); // 확장자
				if ($.inArray(ext, ['jpg', 'jpeg', 'png', 'gif']) == -1) { // $.inArray(): 이 배열에 있는지. 없으면 -1리턴
					alert("이미지 파일만 업로드 할 수 있습니다.");
					$('#file').val(""); // 잘못된 파일을 비운다.
					return;
				}
			}
			
			let postId = $(this).data('post-id');
			//alert(postId);
			
			// 폼태그를 자바스크립트에서 만든다.(이미지 파일이 있어서 폼태그로 보낸다.)
			let formData = new FormData();
			formData.append("postId", postId);
			formData.append("subject", subject);
			formData.append("content", content);
			formData.append("file", $('#file')[0].files[0]);
			
			// AJAX => 서버 통신
			$.ajax({
				// request
				type:"PUT"
				, url:"/post/update"
				, data:formData
				, enctype:"multipart/form-data" // 파일 업로드를 위한 필수 설정
				, processData:false // 파일 업로드를 위한 필수 설정
				, contentType:false // 파일 업로드를 위한 필수 설정
				
				// response
				, success:function(data) {
					if (data.code == 1) {
						alert("메모가 수정되었습니다.");
						location.reload(true);
					} else {
						alert(data.errorMessage);
					}
				}
				, error:function(e) {
					alert("메모수정에 실패했습니다.");
				}
			});
		});
	});
</script>