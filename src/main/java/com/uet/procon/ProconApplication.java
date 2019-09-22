package com.uet.procon;

import com.uet.procon.common.enums.Workers;
import com.uet.procon.common.util.LoggingUtil;
import com.uet.procon.worker.base.BaseWorker;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProconApplication implements CommandLineRunner {

    private Logger logger = LoggingUtil.createLogger(ProconApplication.class);

    @Autowired
    private BaseWorker worker;

    public static void main(String[] args) {
        SpringApplication.run(ProconApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            worker.start(Workers.PROCON);
        } catch (Exception e) {
            logger.error("START:WORKER:FAILED:" + e.getMessage());
        }
    }
}
