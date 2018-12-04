package com.wuwenze.wechatsmallapptmplmsg.initializer;

import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;

import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    public void onStartup(ServletContext servletContext) {
        WebApplicationContext rootApplicationContext = createRootApplicationContext(servletContext);
        if (null != rootApplicationContext) {
            /**
             * @see WebUtil#getRequest()
             * @see WebUtil#getResponse()
             */
            servletContext.addListener(new RequestContextListener());
            log.info("init org.springframework.web.context.request.RequestContextListener...");
        }
    }
}
