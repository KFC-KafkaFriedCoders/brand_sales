package com.example.brand_sales;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class EncodingTest {
    public static void main(String[] args) {
        // 현재 시스템의 인코딩 정보 출력
        System.out.println("시스템 파일 인코딩: " + System.getProperty("file.encoding"));
        System.out.println("시스템 콘솔 인코딩: " + System.getProperty("console.encoding"));
        System.out.println("기본 문자셋: " + java.nio.charset.Charset.defaultCharset());
        System.out.println("시스템 언어: " + System.getProperty("user.language"));
        System.out.println("시스템 국가: " + System.getProperty("user.country"));
        System.out.println();

        // 시스템 기본 출력으로 한글 테스트
        System.out.println("시스템 출력으로 한글 테스트: 안녕하세요");

        // OutputStreamWriter로 명시적 UTF-8 인코딩 사용
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
            out.println("UTF-8 명시적 출력으로 한글 테스트: 안녕하세요");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}