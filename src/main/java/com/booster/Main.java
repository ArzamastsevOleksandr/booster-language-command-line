package com.booster;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

// todo: standardize maven builds with maven wrapper
// todo: learn javac
// todo: java flame graphs
@Configuration
@ComponentScan
public class Main {
    public static void main(String[] args) {
        var applicationContext = new AnnotationConfigApplicationContext(Main.class);
        var learningSessionManager = applicationContext.getBean("learningSessionManager", LearningSessionManager.class);
        learningSessionManager.launch();
    }

}
