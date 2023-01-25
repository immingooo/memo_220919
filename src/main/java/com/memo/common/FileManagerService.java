package com.memo.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component // 일반적인 스프링 빈. 로직을 들어가는 데 bo, dao는 아닐 때(bo, dao의 부모격)
public class FileManagerService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// 실제 이미지가 저장될 경로(서버)
	public static final String FILE_UPLOAD_PATH = "D:\\minkyung\\6_spring_project\\memo\\workspace\\images/"; // 집에서는 6_Spring_project 학원에서는 6_spring_project
	
	// input: MultipartFile, userLoginId
	// output: image path
	public String saveFile(String userLoginId, MultipartFile file) {
		// 파일 디렉토리 예) aaaa_165482365/sun.png (사용자로그인아이디폴더를 시간마다 관리할 것. userLoginId_시간/sun.png)
		String directoryName = userLoginId + "_" + System.currentTimeMillis() + "/"; // aaaa_165482365/     현재시간은 밀리세컨드 단위로
		String filePath = FILE_UPLOAD_PATH + directoryName; // D:\\minkyung\\6_spring_project\\memo\\workspace\\images/aaaa_165482365/
		
		File directory = new File(filePath); // 폴더를 만들어 낼 준비(폴더 경로 생성)
		if (directory.mkdir() == false) { // 폴더 생성 실패시
			return null; // 폴더 만드는데 실패시 이미지경로는 null
		} 
		
		// 파일 업로드: byte 단위로 업로드 된다.
		try { // 실패시 책임은 BO가 아닌 여기서 진다.
			byte[] bytes = file.getBytes(); 
			Path path = Paths.get(filePath + file.getOriginalFilename()); // OriginalFilename은 사용자가 올린 파일명. 한글파일 이름은 안올라감. 개인프로젝트일 땐 한글이름파일을 하기위한 로직을 추가해야함!!!!
			Files.write(path, bytes); // 진짜 업로드하는 순간
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		// 파일 업로드 성공했으면 이미지 url path를 리턴한다.
		// http://localhost/images/aaaa_165482365/sun.png
		return "/images/" + directoryName + file.getOriginalFilename();
	}
	
	// 이미지 제거하는 로직
	public void deleteFile(String imagePath) { // imagePath: /images/aaaa_165482365/sun.png
		// 이미지 제거 -> 폴더 제거
		//      \\images/      imagePath에 있는 겹치는  /images/  구문 제거
		Path path = Paths.get(FILE_UPLOAD_PATH + imagePath.replace("/images/", ""));
		if (Files.exists(path)) {
			// 이미지 삭제
			try {
				Files.delete(path); // 여기서 예외처리(여기서 안하면 BO로 가는데 BO가 처리할 필요가 없음)
			} catch (IOException e) {
				logger.error("[이미지 삭제] 이미지 삭제 실패. imagePath:{}", imagePath);
			}
			
			// 디렉토리(폴더) 삭제
			path = path.getParent(); // 부모로 이동
			if (Files.exists(path)) {
				try {
					Files.delete(path);
				} catch (IOException e) {
					logger.error("[이미지 삭제] 디렉토리(이미지 폴더) 삭제 실패. imagePath:{}", imagePath);
				}
			}
		}
	}
}
