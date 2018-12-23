package com.bonc.broker;

import com.bonc.broker.service.mysql.base.BaseWorkerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

@Component
public class VersionInterceptor implements HandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(BaseWorkerThread.class);
    private final String versionKey = "X-Broker-API-Version";
    private final String version = "2.13";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String v = request.getHeader(versionKey);

        Map<String,String[]> requestMsg = request.getParameterMap();
        Enumeration<String> requestHeader = request.getHeaderNames();

        String pathInfo = request.getPathInfo();
        logger.info("====pathInfo===:\t" + pathInfo);
        String method = request.getServletPath();
        logger.info("----请求方法====:\t" + method);

        logger.info("------- header -------");
        while (requestHeader.hasMoreElements()) {
            String headerKey = requestHeader.nextElement();
            //打印所有Header值
            System.out.println("headerKey=" + headerKey + ";value=" + request.getHeader(headerKey));
        }

        logger.info("------- parameter -------");
        for (String key : requestMsg.keySet()) {
            for (int i = 0; i < requestMsg.get(key).length; i++) {
                //打印所有请求参数值
                logger.info("key=" + key + ";value=" + requestMsg.get(key)[i].toString());
            }
        }

        System.out.println("version:\t" + v);
        if (version.equals(v)) {
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
