package org.example.expert.config.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect // 1. "나는 스프링클러(AOP 모듈)입니다!" 선언
@Component // 2. 스프링 컨테이너에 빈으로 등록
public class ApiLoggingAspect {

    @Autowired
    private ObjectMapper objectMapper;

    // 3. Pointcut: "정확히 어느 화분에 물을 줄 건데?"
    @Around("@annotation(AdminLogging)")
    public Object logApi(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = attributes.getRequest();
        // 요청 사용자 id
        long userId = (long)request.getAttribute("userId");
        // 요청 시각
        LocalDateTime requestTime = LocalDateTime.now();
        // 요청 url
        String requestURL = request.getRequestURI();

        // 요청 본문
        Object requestDto = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg.getClass().getPackageName().contains("dto"))
                .findFirst()
                .orElse(null);

        String requestJson = objectMapper.writeValueAsString(requestDto);


        // 타겟 메서드 실행
        Object result = joinPoint.proceed();

        // 응답 본문
        String responseJson = objectMapper.writeValueAsString(result);

        log.info("""
                
                ===== ADMIN LOG =====
                USER_ID: {}
                REQUEST_AT : {}
                URL: {}
                REQUEST: {}
                RESPONSE: {}
                ===========================
                """,
                userId,
                requestTime.toString(),
                requestURL,
                requestJson,
                responseJson);

        return result;
    }
}