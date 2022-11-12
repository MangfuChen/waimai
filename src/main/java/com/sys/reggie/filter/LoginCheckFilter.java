package com.sys.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.sys.reggie.common.BaseContext;
import com.sys.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录   ---过滤器
 *
 * urlPatterns----需要拦截的地址    /*所有的请求都拦截
 */

@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter  implements Filter {

    //路径匹配器  Strig提供的   支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req =(HttpServletRequest) servletRequest;
        HttpServletResponse resp =(HttpServletResponse) servletResponse;

        //获取本次请求url
        String requestURI = req.getRequestURI();
        log.info("拦截到请求:{}",req.getRequestURI());
        //不需要处理请求路径
        String[] urls = new String[]{
            "/employee/login",
            "/employee/logout",
            "/backend/**",
            "/front/**",
            "/common/**",
            "/user/login",
                "/user/sendMsg"
        };
        //判断路径是否需要处理
        boolean check = check(urls,requestURI);

        //如果不需要处理放行
        if(check){
            log.info("本次请求不需要处理:{}",req.getRequestURI());
            //放行
            filterChain.doFilter(req,resp);
            return;
        }
        //4-1需要处理判断是否登录
        if(req.getSession().getAttribute("employee")!=null){
            log.info("用户已登录，id为:{}",req.getSession().getAttribute("employee"));
            //已经登录了
            Long empId = (Long) req.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(req,resp);
            return;
        }
        //4-2需要处理移动端判断是否登录
        if(req.getSession().getAttribute("user")!=null){
            log.info("用户已登录，id为:{}",req.getSession().getAttribute("user"));
            //已经登录了
            Long userId = (Long) req.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(req,resp);
            return;
        }

        log.info("用户未登录");
        //没有登录  通过输出流向客户端响应界面
        resp.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
        return;
    }

    /**
     * 路径匹配 ，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match)return true;
        }
        return false;
    }
}
