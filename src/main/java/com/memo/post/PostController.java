package com.memo.post;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.memo.post.bo.PostBO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/post")
public class PostController {
	
	@Autowired
	private PostBO postBO;

	// 글 목록화면
	@GetMapping("/post_list_view")
	public String postListView(
			Model model,
			HttpSession session) {
		Integer userId = (Integer)session.getAttribute("userId"); // Integer로 해야 로그인안된 사람도 들어올 수 있다. int는 로그인안된 사람이 들어오면 바로 에러남
		if (userId == null) { // 비로그인 처리(나중에 일괄적으로 할 거임)
			return "redirect:/user/sign_in_view";
		}
		
		// DB select
		List<Post> postList = postBO.getPostListByUserId(userId); // null은 빠져나감. 여기서 디버깅으로 값이 들어오는지 확인해보기
		
		model.addAttribute("postList", postList);
		model.addAttribute("viewName", "post/postList");
		return "template/layout";
	}
	
	/**
	 * 글쓰기 화면
	 * @param model
	 * @return
	 */
	@GetMapping("/post_create_view")
	public String postCreateView(Model model) {
		model.addAttribute("viewName", "post/postCreate");
		return "template/layout";
	}
	
	@GetMapping("/post_detail_view")
	public String postDetailView(
			@RequestParam("postId") int postId,
			HttpSession session,
			Model model) { // 로그인이 됐는지도 확인
		// 로그인 된 사람만 오는 로직은 나중에 공통으로 처리하긴 할 예정
		Integer userId = (Integer)session.getAttribute("userId");
		if (userId == null) {
			return "redirect:/user/sign_in_view";
		}
		
		// DB select by - userId, postId 내가 쓴 글 하나 가져오기
		
		model.addAttribute("viewName", "post/postDetail");
		return "template/layout";
	}
}
