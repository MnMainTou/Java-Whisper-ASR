package com.metal.web.config;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.net.URLDecoder;
import java.util.stream.Stream;

/**
 * mvc配置
 *
 * @author xxl
 * @since 2023/9/16
 */
@ConfigurationProperties(prefix = "authorization")
@Configuration
@Data
@Slf4j
public class MvcConfigure implements WebMvcConfigurer, HandlerInterceptor {

    /**
     *  拦截路径
     */
    private static final String PATH = "/**";

    /**
     *  白名单
     */
    private String[] whiteList = new String[]{
            "/**/doc.html",
            "/**/*.css",
            "/**/*.js",
            "/**/*.png",
            "/**/*.jpg",
            "/**/*.ico",
            "/**/v3/**",
            "/**/static/**"
    };

    /**
     * 是否开启验证,默认为开启
     */
    private boolean enable = true;

    @Override
    public boolean preHandle(@Nullable HttpServletRequest request,@Nullable HttpServletResponse response,@Nullable Object handler) {
        if (!enable) {
            return true;
        }
        StpUtil.checkLogin();
        return true;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration authInterceptorRegistration = registry.addInterceptor(this);
        authInterceptorRegistration.addPathPatterns(PATH);
        authInterceptorRegistration.excludePathPatterns(whiteList);
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        // 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了,这里设置2个小时
        config.setMaxAge(360000L);
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    public void setWhiteList(String[] whiteList) {
        this.whiteList = Stream.concat(Stream.of(whiteList), Stream.of(this.whiteList)).toArray(String[]::new);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // 获取 JAR 文件的绝对路径
            String jarPath = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX).getPath();
            jarPath = URLDecoder.decode(jarPath, "UTF-8");

            // 处理 JAR 内部路径（Windows/Linux 通用）
            if (jarPath.contains(".jar!")) {
                // 示例：jarPath = file:/C:/path/to/application-2.0.0.jar!/BOOT-INF/classes!/
                String realJarPath = jarPath.replaceFirst("file:", "")
                        .replaceFirst("!/BOOT-INF/classes!/", "")
                        .replaceFirst("!.*", "");
                File jarFile = new File(realJarPath);
                jarPath = jarFile.getParentFile().getAbsolutePath();
            }

            // 拼接外部 static 目录路径
            String externalStaticDir = jarPath + File.separator + "static" + File.separator;
            System.out.println("External Static Directory: " + externalStaticDir);

            // 映射静态资源
            registry.addResourceHandler("/**")
                    .addResourceLocations("file:" + externalStaticDir)
                    .addResourceLocations("classpath:/static/");
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure static resources", e);
        }
    }

}
