package com.bugshot;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BugshotApplication {

	public static void main(String[] args) {
		// JVM 기본 timezone을 Asia/Seoul로 설정
		// LocalDateTime.now()가 KST 기준으로 동작하도록 함
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(BugshotApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// Spring Context 초기화 후에도 timezone 보장
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

}
