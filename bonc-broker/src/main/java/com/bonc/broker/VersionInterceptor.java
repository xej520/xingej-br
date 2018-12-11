package com.bonc.broker;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class VersionInterceptor implements HandlerInterceptor {
    private final String versionKey = "X-Broker-API-Version";
    private final String version = "2.13";
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String v = request.getHeader(versionKey);

        System.out.println("version:\t"+v);
        if(version.equals(v)){
            return true;
        }

        try {
            response.setStatus(412);
            PrintWriter out = response.getWriter();
            out.print("2.14");
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

    }
}
