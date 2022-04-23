package com.ce.sr.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ZipAspects {
    
    @Pointcut("execution(public String com.ce.sr.services.FileService.uploadFile(..))")
    public void uploadFileMethod() {};

    @After("uploadFileMethod()")
    public void zipFile(JoinPoint jp){
        
    }


}
