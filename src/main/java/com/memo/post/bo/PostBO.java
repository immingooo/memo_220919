package com.memo.post.bo;

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
			logger.warn("[update post] 수정할 메모가 존재하지 않습니다. postId:{}, userId{}", postId, userId); // 적절한 레벨로 정해서 사용하면 됨
			return;
		}
		
		// 만약에 멀티파일이 비어있지 않다면 업로드 후 imagePath를 받아와야 함 - 만약에 업로드가 성공하면 기존 이미지 제거, 그렇지 않으면 기존 이미지가 있도록
		String imagePath = null;
		if (file != null) {
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
	
	public List<Post> getPostListByUserId(int userId) { // 여기선 userId가 null이 되면 안돼서 int로
		return postDAO.selectPostListByUserId(userId);
	}
	
	public Post getPostByPostIdUserId(int postId, int userId) {
		return postDAO.selectPostByPostIdUserId(postId, userId);
	}
}
