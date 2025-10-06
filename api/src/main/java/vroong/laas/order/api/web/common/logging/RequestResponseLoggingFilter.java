package vroong.laas.order.api.web.common.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.logstash.logback.argument.StructuredArguments.keyValue;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Request/Response 로깅 Filter
 * 
 * - 모든 HTTP 요청/응답을 로그로 기록
 * - ContentCachingWrapper 사용으로 성능 최적화
 * - 제외 URL 패턴 지원
 * - traceId는 Micrometer가 자동으로 MDC에 설정
 */
@Component
public class RequestResponseLoggingFilter implements Filter {
    
    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final int MAX_PAYLOAD_LENGTH = 10 * 1024; // 10KB (성능 최적화)
    
    // 민감 정보 헤더 제외
    private static final Set<String> EXCLUDED_HEADERS = Set.of(
        "authorization",
        "cookie",
        "x-api-key",
        "x-auth-token"
    );
    
    private final LoggingProperties loggingProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    public RequestResponseLoggingFilter(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 제외 URL 체크
        if (isExcluded(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }
        
        // ContentCachingWrapper로 감싸기 (Body를 여러 번 읽을 수 있도록)
        ContentCachingRequestWrapper wrappedRequest = 
            httpRequest instanceof ContentCachingRequestWrapper 
                ? (ContentCachingRequestWrapper) httpRequest 
                : new ContentCachingRequestWrapper(httpRequest, MAX_PAYLOAD_LENGTH);
        
        ContentCachingResponseWrapper wrappedResponse = 
            httpResponse instanceof ContentCachingResponseWrapper 
                ? (ContentCachingResponseWrapper) httpResponse 
                : new ContentCachingResponseWrapper(httpResponse);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Request 로깅 (실패해도 요청 처리는 계속)
            try {
                logRequest(wrappedRequest);
            } catch (Exception e) {
                log.error("Failed to log request", e);
            }
            
            // 다음 Filter로 진행
            chain.doFilter(wrappedRequest, wrappedResponse);
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Response 로깅 (실패해도 응답은 전달)
            try {
                logResponse(wrappedRequest, wrappedResponse, duration);
            } catch (Exception e) {
                log.error("Failed to log response", e);
            }
            
            // Response Body를 실제 응답으로 복사 (필수!)
            try {
                wrappedResponse.copyBodyToResponse();
            } catch (Exception e) {
                log.error("Failed to copy response body", e);
            }
        }
    }
    
    /**
     * Request 로깅 (StructuredArguments 사용)
     */
    private void logRequest(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        // Headers 수집 (민감 정보 제외)
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!EXCLUDED_HEADERS.contains(headerName.toLowerCase())) {
                headers.put(headerName, request.getHeader(headerName));
            }
        }

        // Query Parameters 수집
        Map<String, String> queryParams = new HashMap<>();
        if (queryString != null) {
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    queryParams.put(key, values[0]);
                }
            });
        }

        // Request Body (크기 제한)
        byte[] content = request.getContentAsByteArray();
        String payload = getPayloadForLog(content);
        
        // Content Length
        int contentLength = content.length;

        // Timestamp
        Instant requestedAt = Instant.now();

        // StructuredArguments를 사용한 로깅
        log.info("Request: {} {}", method, uri,
            keyValue("headers", headers),
            keyValue("query_params", queryParams),
            keyValue("payload", payload),
            keyValue("content_length", contentLength),
            keyValue("requested_at", requestedAt)
        );
    }
    
    /**
     * Response 로깅 (StructuredArguments 사용)
     */
    private void logResponse(ContentCachingRequestWrapper request, 
                            ContentCachingResponseWrapper response, 
                            long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        
        // URI 패턴 추출 (Spring MVC가 매핑한 패턴)
        String uriPattern = extractUriPattern(request);

        // Response Body (크기 제한)
        byte[] content = response.getContentAsByteArray();
        String payload = getPayloadForLog(content);

        // Content Length
        int contentLength = content.length;
        
        // Timestamp
        Instant respondedAt = Instant.now();
        
        // StructuredArguments를 사용한 로깅
        log.info("Response: {} {}", method, uri,
            keyValue("uri_pattern", uriPattern),
            keyValue("payload", payload),
            keyValue("status", status),
            keyValue("duration_ms", duration),
            keyValue("content_length", contentLength),
            keyValue("responded_at", respondedAt)
        );
    }
    
    /**
     * Payload를 로그용으로 변환 (크기 제한 적용)
     */
    private String getPayloadForLog(byte[] content) {
        if (content.length == 0) {
            return null;
        }
        
        if (content.length > MAX_PAYLOAD_LENGTH) {
            // 크기 초과 시 별도 경고 로그
            log.warn("Payload exceeds max length: {} bytes (max: {} bytes)", 
                content.length, MAX_PAYLOAD_LENGTH);
            
            return String.format("[PAYLOAD_TOO_LARGE: %d bytes, max: %d bytes]", 
                content.length, MAX_PAYLOAD_LENGTH);
        }
        
        return new String(content, StandardCharsets.UTF_8);
    }
    
    /**
     * URI 패턴 추출 (예: /api/v1/orders/{orderId})
     */
    private String extractUriPattern(ContentCachingRequestWrapper request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return pattern != null ? pattern.toString() : request.getRequestURI();
    }
    
    /**
     * 제외 URL 체크
     */
    private boolean isExcluded(String uri) {
        return loggingProperties.getExcludePatterns().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }
}
