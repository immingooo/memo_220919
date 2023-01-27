package com.memo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.memo.common.FileManagerService;
import com.memo.interceptor.PermissionInterceptor;

@Configuration // 설정파일 일 때
public class WebMvcConfig implements WebMvcConfigurer {
	
	@Autowired
	private PermissionInterceptor interceptor;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		
		registry
		.addResourceHandler("/images/**") // 웹 이미지 주소 // http://localhost/images/aaaa_165482365/sun.png
		.addResourceLocations("file:///" + FileManagerService.FILE_UPLOAD_PATH); // 실제 파일 위치 (Mac: // Window: ///)
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor)
		.addPathPatterns("/**")        //  /**: 아래 디렉토리까지(자식요소들까지) 모든 주소 다 확인
		.excludePathPatterns("/favicon.ico", "/error", "/static/**", "/user/sign_out");
	}
}
