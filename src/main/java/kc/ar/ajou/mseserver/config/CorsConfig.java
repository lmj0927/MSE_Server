package kc.ar.ajou.mseserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource(@Value("${mse.cors.allowed-origin-patterns}") String patterns) {
		CorsConfiguration config = new CorsConfiguration();
		for (String pattern : patterns.split(",")) {
			String trimmed = pattern.trim();
			if (!trimmed.isEmpty()) {
				config.addAllowedOriginPattern(trimmed);
			}
		}
		config.addAllowedHeader(CorsConfiguration.ALL);
		config.addAllowedMethod(CorsConfiguration.ALL);
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
