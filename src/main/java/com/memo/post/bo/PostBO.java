package com.memo.post.bo;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.memo.common.FileManagerService;
import com.memo.post.Post;
import com.memo.post.dao.PostDAO;

@Service
public class PostBO {
	
	//private Logger logger = LoggerFactory.getLogger(PostBO.class);
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 페이지 갯수 (변하지 않도록 상수)
	private static final int POST_MAX_SIZE = 3;
	
	@Autowired
	private PostDAO postDAO;
	
	@Autowired
	private FileManagerService fileManagerService;

	// 글 추가
	public int addPost(int userId, String userLoginId, String subject, String content, MultipartFile file) {

		// 파일 업로드(내 컴퓨터 서버에 업로드해야함) => 경로를 리턴받아서 DB에 경로를 넣어야 함
		String imagePath = null;
		if (file != null) { 
			// 파일이 있을 때만 업로드 => 이미지 경로를 얻어냄
			imagePath = fileManagerService.saveFile(userLoginId, file);
		}
		
		//return 1;
		// DAO insert
		return postDAO.insertPost(userId, subject, content, imagePath);
	}
	
	// 글 수정
	public void updatePost(int userId, String userLoginId,
			int postId, String subject, String content, MultipartFile file) {
		
		// 기존 글을 가져온다. (이미지가 교체될 때 기존 이미지 제거를 위해서) - 기존 글이 없는 경우(이상한 상황)도 체크해야 함
		Post post = getPostByPostIdUserId(postId, userId); // userId까지 있으면 완전히 내가 쓴 글만 가져올 수 있어서 안전한 코드!(postId만 있으면 사용자가 쓴 글이 아닌게 올 수 있음)
		if (post == null) { // 이상한 상황
			logger.warn("[update post] 수정할 메모가 존재하지 않습니다. postId:{}, userId{}", postId, userId); // 적절한 레벨로 정해서 사용하면 됨. {와일드카드}로 핵심 정보도 넣어서 로그를 찍을 수 있다.
			return;
		}
		
		// 만약에 멀티파일이 비어있지 않다면 업로드 후 imagePath를 받아와야 함 - 만약에 업로드가 성공하면 기존 이미지 제거, 그렇지 않으면 기존 이미지가 있도록
		String imagePath = null;
		if (file != null) { // 수정할 이미지파일이 있을 때에만
			// 업로드 요청
			imagePath = fileManagerService.saveFile(userLoginId, file);
			
			// 업로드 성공하면 기존 이미지 제거 => 업로드가 실패할 수 있으므로 업로드가 성공한 후 제거
			// imagePath가 null이 아니고(업로드 성공), 기존 글에 imagePath가 null이 아닐 경우(이미지가 원래 있었다면)
			if (imagePath != null && post.getImagePath() != null) {
				// 이미지 제거
				fileManagerService.deleteFile(post.getImagePath()); // imagePath: 방금 업로드(수정)한 사진 경로. post.getImagePath(): 기존 글에 있던 사진 경로
			}
		}
		
		// DB update - 무조건 이 로직을 타도록 따로 BO를 만들지 않음
		postDAO.updatePostByPostIdUserId(postId, userId, subject, content, imagePath);
	}
	
	// 글 삭제
	public int deletePostByPostIdUserId(int postId, int userId) {
		// 기존 글 가져와서 이미지가 있으면 이미지를 제거해달라하고 DB행을 삭제해야함
		// 기존 글 가져오기
		Post post = getPostByPostIdUserId(postId, userId);
		if (post == null) { // 없는 이상한 경우가 있을 수도 있어서 꼭 null체크를 해야함
			logger.warn("[글 삭제] post is null. postId:{}, userId:{}", postId, userId);
			return 0;
		}
		
		// 업로드 되었던 이미지가 있으면 파일 삭제
		if (post.getImagePath() != null) { // 이미지가 있을 때 제거
			fileManagerService.deleteFile(post.getImagePath());
		}
		
		// DB delete
		return postDAO.deletePostByPostIdUserId(postId, userId);
	}
	
	public List<Post> getPostListByUserId(int userId, Integer prevId, Integer nextId) { // 여기선 userId가 null이 되면 안돼서 int로
		// 게시글 번호:  10 9 8 | 7 6 5 | 4 3 2 | 1
		// 만약 4 3 2 페이지에 있을 때
		// 1) 이전을 눌렀을 때: (DESC가 아닌)정방향(ASC) 4보다 큰 3개(5 6 7) => List reverse로 7 6 5로 만들어야 한다.
		// 2) 다음: 2보다 작은 3개 DESC
		// 3) 첫페이지(이전, 다음 없음) DESC로 3개 가져오면 됨
		String direction = null; // 방향
		Integer standardId = null; // 기준 postId
		if (prevId != null) { // 이전
			direction = "prev";
			standardId = prevId;
			
			List<Post> postList = postDAO.selectPostListByUserId(userId, direction, standardId, POST_MAX_SIZE);
			Collections.reverse(postList); // 뒤집기
			return postList;
		} else if (nextId != null) { // 다음
			direction = "next";
			standardId = nextId;
		}
		
		// 첫페이지일 때(페이징X) direction, standardId이 null
		// 다음일 때 direction, standardId이 채워져서 넘어감
		return postDAO.selectPostListByUserId(userId, direction, standardId, POST_MAX_SIZE);
	}
	
	public boolean isPrevLastPage(int prevId, int userId) {
		int maxPostId = postDAO.selectPostIdByUserIdSort(userId, "DESC");
		return maxPostId == prevId ? true: false;
	}
	
	public boolean isNextLastPage(int nextId, int userId) {
		int minPostId = postDAO.selectPostIdByUserIdSort(userId, "ASC");
		return minPostId == nextId ? true : false;
	}
	
	public Post getPostByPostIdUserId(int postId, int userId) {
		return postDAO.selectPostByPostIdUserId(postId, userId);
	}
}
