package com.uet.procon.worker.base;

import com.uet.procon.common.util.LoggingUtil;
import org.slf4j.Logger;

public abstract class AbstractWorker implements BaseWorker {

    private Logger logger = LoggingUtil.createLogger(AbstractWorker.class);

    private String workerName;

    @Override
    public void start(String workerName) throws Exception {
        this.workerName = workerName;
        logger.info("START:WORKER:" + this.workerName);
        onStarted();
    }

    @Override
    public void stop() throws Exception {
        logger.info("STOP:WORKER:" + this.workerName);
        onStopped();
    }

    public abstract void onStarted() throws Exception;

    public abstract void onStopped() throws Exception;
}
