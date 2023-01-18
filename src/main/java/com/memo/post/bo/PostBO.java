package com.memo.post.bo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.memo.common.FileManagerService;
import com.memo.post.Post;
import com.memo.post.dao.PostDAO;

@Service
public class PostBO {
	
	@Autowired
	private PostDAO postDAO;
	
	@Autowired
	private FileManagerService fileManagerService;

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
	
	public List<Post> getPostListByUserId(int userId) { // 여기선 userId가 null이 되면 안돼서 int로
		return postDAO.selectPostListByUserId(userId);
	}
}
