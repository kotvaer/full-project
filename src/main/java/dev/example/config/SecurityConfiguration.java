package dev.example.config;

import dev.example.entity.Rest;
import dev.example.entity.response.AuthorizedVO;
import dev.example.filter.JwtAuthorizeFilter;
import dev.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfiguration {
    @Resource
    JwtUtils jwtUtils;

    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(conf -> conf
                        .loginProcessingUrl("/api/auth/login")
                        .failureHandler(this::onAuthenticationFailure)
                        .successHandler(this::onAuthenticationSuccess))
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess))
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint(this::onUnauthorized)
                        .accessDeniedHandler(this::onAccessDenied))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    public void onAccessDenied(HttpServletRequest request,
                               HttpServletResponse response,
                               AccessDeniedException exception) throws IOException, ServletException {
        //设置返回格式和编码
        response.setContentType("application/json;charset=utf-8");

        //返回错误信息
        response.getWriter().write(Rest.forbidden(exception.getMessage()).asJsonString());

    }

    public void onUnauthorized(HttpServletRequest request,
                               HttpServletResponse response,
                               AuthenticationException exception) throws IOException, ServletException {
        //设置返回格式和编码
        response.setContentType("application/json;charset=utf-8");

        //返回错误信息
        response.getWriter().write(Rest.unauthorized(exception.getMessage()).asJsonString());
    }

    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        //获取用户信息
        User user = (User) authentication.getPrincipal();
        String token = jwtUtils.generateToken(user, 2, "john");
        //相关视图对象
        AuthorizedVO authorizedVO = new AuthorizedVO();
        authorizedVO.setUsername(user.getUsername());
        authorizedVO.setRole("ROLE_USER");
        authorizedVO.setToken(token);
        authorizedVO.setExpire(jwtUtils.expireLDT());
        //设置返回格式和编码
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        //返回登录成功信息
        response.getWriter().write(Rest.success(authorizedVO).asJsonString());
    }

    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        //设置返回格式和编码
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        //返回错误信息
        response.getWriter().write(Rest.unauthorized(exception.getMessage()).asJsonString());
    }

    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        //设置返回格式和编码
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        if (jwtUtils.invalidateJwt(request.getHeader("Authorization"))) {
            writer.write(Rest.success().asJsonString());
        } else {
            writer.write(Rest.failure(400, "退出登录失败").asJsonString());
        }

    }
}
