<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<div class="d-flex justify-content-center">
	<div class="w-50">
		<h1>글쓰기</h1>
		
		<%-- Enter로 안할꺼여서 form태그 필요없음 --%>
		<input type="text" id="subject" class="form-control" placeholder="제목을 입력하세요">
		<textarea class="form-control" id="content" placeholder="내용을 입력하세요" rows="15"></textarea>
		<div class="d-flex justify-content-end my-4">
			<input type="file" id="file" accept=".jpg,.jpeg,.png,.gif">
		</div>
		
		<div class="d-flex justify-content-between">
			<button type="button" id="postListBtn" class="btn btn-dark">목록</button>
			
			<div>
				<button type="button" id="clearBtn" class="btn btn-secondary">모두지우기</button>
				<button type="button" id="postCreateBtn" class="btn btn-info">저장</button>
			</div>
		</div>
	</div>
</div>

<script>
	$(document).ready(function() {
		// 목록 버튼 클릭 => 글 목록으로 이동
		$('#postListBtn').on('click', function() {
			location.href="/post/post_list_view";
		})
		
		// 모두지우기 버튼 클릭 => 제목, 글 내용 모두 지운다.
		$('#clearBtn').on('click', function() {
			$('#subject').val(""); // 빈칸으로 세팅
			$('#content').val("");
		});
		
		// 글저장 버튼 클릭
		$('#postCreateBtn').on('click', function() {
			let subject = $('#subject').val().trim();
			let content = $('#content').val();
			
			// DB에 null허용을 했는지 확인해보고 validation검사하기
			if (subject == '') {
				alert("제목을 입력하세요");
				return;
			}
			
			console.log(content); // 반드시 내용이 들어왔나 확인하기. 제목은 validation 검사해서 확인안해도됨
			
			// 이미지
			let file = $('#file').val(); //C:\fakepath\the tenth day practice''.py(파일이름)
			//alert(file);
			
			// 파일이 업로드 된 경우에만 확장자 체크(파일이 없을 땐 공백임)
			if (file != '') {
				//alert(file.split(".").pop().toLowerCase()); // .pop(): 제일마지막에 쌓인 애를 뽑아낸다. 전부 소문자(나 대문자)로 바꾼다.
				let ext = file.split(".").pop().toLowerCase();
				if ($.inArray(ext, ['jpg', 'jpeg', 'png', 'gif']) == -1) { // $.inArray(): 이 배열에 있는지. 없으면 -1리턴
					alert("이미지 파일만 업로드 할 수 있습니다.");
					$('#file').val(""); // 잘못된 파일을 비운다.
					return;
				}
			}
			
			// 서버에 보낼예정 - AJAX
			
			// 이미지를 업로드 할 때는 form태그가 있어야 한다.(자바스크립트에서 만듦)
			// append로 넣는 값은 form태그의 name으로 넣는 것과 같다(request parameter)
			let formData = new FormData(); // 비어있는 form태그 만듦
			formData.append("subject", subject); // name지정 "request parameter", 값
			formData.append("content", content);
			formData.append("file", $('#file')[0].files[0]); // file태그중에 0번째 하나 올림. 여러개 이미지올리는건 구글링으로 찾아보기
			
			// ajax 통신으로 formData에 있는 데이터 전송
			$.ajax({
				// request
				type:"post"
				, url:"/post/create"
				, data:formData // 폼객체를 통째로 보낸다.
				, enctype:"multipart/form-data" // 파일업로드를 위한 필수 설정
				, processData:false // 파일업로드를 위한 필수 설정
				, contentType:false // 파일업로드를 위한 필수 설정
				
				// response
				, success:function(data) {
					if (data.code == 1) {
						// 성공
						alert("메모가 저장되었습니다.");
						location.href="/post/post_list_view";
					} else {
						// 실패
						alert(data.errorMessage); // 로직상 에러
					}
				}
				, error:function(e) {
					alert("메모 저장에 실패했습니다.");
				}
			});
		});
	});
</script>