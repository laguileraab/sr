package com.ce.sr.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Aspect
@Component
public class LogAspects {
    @Pointcut("@target(org.springframework.stereotype.Repository)")
    public void repositoryMethods() {
//        LogAspects.log.info("File {} uploaded successfully", file.getOriginalFilename());

    };
}
