package vroong.laas.order.api.web.common.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 로깅 설정 Properties
 * application.yml의 app.logging 설정을 읽어옴
 */
@Component
@ConfigurationProperties(prefix = "app.logging")
public class LoggingProperties {
    
    /**
     * 로깅에서 제외할 URL 패턴 목록
     * 예: /actuator/**, /health, /favicon.ico
     */
    private List<String> excludePatterns = new ArrayList<>();
    
    public List<String> getExcludePatterns() {
        return excludePatterns;
    }
    
    public void setExcludePatterns(List<String> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }
}
