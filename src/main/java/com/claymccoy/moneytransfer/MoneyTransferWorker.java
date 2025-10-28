package com.claymccoy.moneytransfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.claymccoy.temporal.TemporalConfig;
import io.temporal.worker.WorkerFactory;

@Component
public class MoneyTransferWorker
{
    private final WorkerFactory factory;
    private final String taskQueue;
    private final AccountActivity accountActivity;

    @Autowired
    public MoneyTransferWorker(TemporalConfig temporalConfig, AccountActivity accountActivity)
    {
        factory = WorkerFactory.newInstance(temporalConfig.client());
        this.taskQueue = temporalConfig.taskQueue();
        this.accountActivity = accountActivity;
    }

    public void init()
    {
        final var worker = factory.newWorker(taskQueue);
        worker.registerWorkflowImplementationTypes(LedgerWorkflowImpl.class);
        worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);
        worker.registerActivitiesImplementations(accountActivity);
        factory.start();
    }
}
