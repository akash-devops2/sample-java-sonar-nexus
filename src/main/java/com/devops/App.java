package com.devops;

import java.util.logging.Logger;

public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        logger.info("Hello from DevOps Java App!");
        logger.info("New change from feature-branch-1 - hello from branch");
    }
}
