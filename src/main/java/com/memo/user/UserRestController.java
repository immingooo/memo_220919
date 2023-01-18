package com.memo.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.memo.common.EncryptUtils;
import com.memo.user.bo.UserBO;
import com.memo.user.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class UserRestController {
	
	@Autowired
	private UserBO userBO;

	/**
	 * 아이디 중복확인 API
	 * @param loginId
	 * @return
	 */
	@RequestMapping("/is_duplicated_id") // GET, POST 둘 다 상관없을 땐 RequestMapping으로
	public Map<String, Object> isDuplicatedId(
			@RequestParam("loginId") String loginId) {
		
		Map<String, Object> result = new HashMap<>();
		boolean isDuplicated = false;
//		try {
//			boolean isDuplicated = userBO.existLoginId(loginId);
//		} catch (Exception e) {
//			result.put("code", 1);
//			result.put("result", true);
//			return;
//		}
		if (isDuplicated) { // 중복일 때
			result.put("code", 1);
			result.put("result", true); // userBO.existLoginId(loginId)로도 가능
		} else { // 사용 가능
			result.put("code", 1);
			result.put("result", false);
		}
		
		return result;
	}
	
	/**
	 * 회원가입 API
	 * @param loginId
	 * @param password
	 * @param name
	 * @param email
	 * @return
	 */
	@PostMapping("/sign_up")
	public Map<String, Object> signUp(
			@RequestParam("loginId") String loginId,
			@RequestParam("password") String password,
			@RequestParam("name") String name,
			@RequestParam("email") String email) {
		
		// 비밀번호 해싱(hashing) - md5방식(보안상 취약함). 다른 알고리즘들도 있음(검색해서 복붙). 포트폴리오에 작성할 수 있음
		// aaaa => esfsf6484f6sd5f4swe
		// aaaa => esfsf6484f6sd5f4swe
		// static에 올라가 있어서 이미 메모리에(힙영역) 올라와있어서 new를 하지 않아도 사용가능하다.
		String hashedPassword = EncryptUtils.md5(password);
		
		// DB insert
		userBO.addUser(loginId, hashedPassword, name, email);
		
		Map<String, Object> result = new HashMap<>();
		result.put("code", 1);
		result.put("result", "성공");
		//result.put("code", 500); // ?
		//result.put("errorMessage", "회원가입에 실패했습니다.");
		
		return result;
	}
	
	/**
	 * 로그인 API
	 * @param loginId
	 * @param password
	 * @param request
	 * @return
	 */
	@PostMapping("/sign_in")
	public Map<String, Object> signIn(
			@RequestParam("loginId") String loginId,
			@RequestParam("password") String password,
			HttpServletRequest request) {
		
		// 비밀번호 해싱
		String hashedPassword = EncryptUtils.md5(password); // DB select없는 상태에서 브레이크포인트로 디버깅 검사하기
		
		// DB select
		User user = userBO.getUserByLoginIdPassword(loginId, hashedPassword); // 이 로직에 대한 실패는 try catch로 잡는다.
		
		Map<String, Object> result = new HashMap<>();
		if (user != null) {
			// 행이 있으면 로그인
			result.put("code", 1);
			result.put("result", "성공");
			
			// 세션에 유저 정보를 담는다.(로그인 상태 유지. 모든 곳에서 로그인이 사용가능하다.)
			// 너무 아무거나 막 담으면 안된다.
			HttpSession session = request.getSession();
			session.setAttribute("userId", user.getId());
			session.setAttribute("userLoginId", user.getLoginId());
			session.setAttribute("userName", user.getName());
		} else {
			// 행이 없으면 로그인 실패
			result.put("code", 500);
			result.put("errorMessage", "존재하지 않는 사용자입니다.");
		}
		
		return result;
	}
}
