package com.bugshot;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BugshotApplication {

	public static void main(String[] args) {
		// JVM 기본 timezone을 UTC로 설정
		// 모든 시간은 UTC로 저장되고, 프론트엔드에서 사용자 timezone으로 변환
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(BugshotApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// Spring Context 초기화 후에도 UTC timezone 보장
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

}
