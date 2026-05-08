package com.logobing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogoBingApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogoBingApplication.class, args);
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    LOGO BING BINGO                               ║");
        System.out.println("║                   Commercial Version                             ║");
        System.out.println("║              Full Bot Controlled Game                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }
}
