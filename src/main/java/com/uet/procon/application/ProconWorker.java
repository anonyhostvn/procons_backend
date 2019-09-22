package com.uet.procon.application;

import com.uet.procon.common.util.LoggingUtil;
import com.uet.procon.worker.base.AbstractWorker;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ProconWorker extends AbstractWorker {

    private Logger logger = LoggingUtil.createLogger(ProconWorker.class);

    @Override
    public void onStarted() throws Exception {
        logger.info("START:PROCON:SUCCESSFUL");
    }

    @Override
    public void onStopped() throws Exception {
        logger.info("STOP:PROCON:SUCCESSFUL");
    }
}
