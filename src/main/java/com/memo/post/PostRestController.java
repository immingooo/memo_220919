package com.memo.post;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.memo.post.bo.PostBO;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/post")
public class PostRestController {
	
	@Autowired
	private PostBO postBO;

	/**
	 * 글쓰기 API
	 * @param subject
	 * @param content
	 * @param file
	 * @param session
	 * @return
	 */
	@PostMapping("/create")
	public Map<String, Object> create( // request parameter는 DB desc보면서 null허용인지 보면서 하기
			@RequestParam("subject") String subject,
			@RequestParam(value="content", required=false) String content,
			@RequestParam(value="file", required=false) MultipartFile file,
			HttpSession session) {
		
		// 로그인 정보받음
		int userId = (int)session.getAttribute("userId"); // Object여서 원래 타입으로 캐스팅. Integer은 따로 null검사해야함 => 디버깅으로 검사해야함
		String userLoginId = (String)session.getAttribute("userLoginId");
		// 저장버튼 눌렀을 때 세션에 있는 정보 가져와서 (회원가입 됐는지)글을 작성할 권한이 있는지 판단하고 사용자 정보 없으면 리다이렉트 => 나중에
		
		// DB Insert
		int rowCount = postBO.addPost(userId, userLoginId, subject, content, file);
		
		Map<String, Object> result = new HashMap<>();
		
		if (rowCount > 0) {
			result.put("code", 1);
			result.put("result", "성공");			
		} else {
			result.put("code", 500);
			result.put("errorMessage", "메모 저장에 실패했습니다. 관리자에게 문의해주세요.");
		}
		
		return result;
	}
	
	/**
	 * 글수정 API
	 * @param postId
	 * @param subject
	 * @param content
	 * @param file
	 * @param session
	 * @return
	 */
	@PutMapping("/update")
	public Map<String, Object> update(
			@RequestParam("postId") int postId,
			@RequestParam("subject") String subject,
			@RequestParam(value="content", required=false) String content,
			@RequestParam(value="file", required=false) MultipartFile file,
			HttpSession session) {
		
		// 로그인여부는 나중에 일괄적으로 처리
		int userId = (int)session.getAttribute("userId"); // 이미 로그인여부 처리했다고 가정하고 비로그인인 사용자가 들어오면 에러나도록
		String userLoginId = (String)session.getAttribute("userLoginId");
		
		// DB update - 이미지가 없으면 원래 이미지로 올려야하고, 수정되는 이미지가 있으면 원래 있던 이미지는 삭제 => 로직이여서 BO에서 처리
		postBO.updatePost(userId, userLoginId, postId, subject, content, file);
		
		Map<String, Object> result = new HashMap<>();
		result.put("code", 1);
		result.put("result", "성공");
		
		return result;
	}
}
