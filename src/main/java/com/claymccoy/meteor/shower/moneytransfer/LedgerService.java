package com.claymccoy.meteor.shower.moneytransfer;

import com.claymccoy.meteor.shower.temporal.TemporalConfig;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LedgerService
{
    private static final Logger LOG = LoggerFactory.getLogger(LedgerService.class);

    private final LedgerWorkflow workflow;

    @Autowired
    public LedgerService(TemporalConfig temporalConfig)
    {
        final var options = WorkflowOptions.newBuilder()
                .setTaskQueue(temporalConfig.taskQueue())
                .setWorkflowId("ledger-workflow")
                .build();
        workflow = temporalConfig.client().newWorkflowStub(LedgerWorkflow.class, options);
        try {
            WorkflowClient.start(workflow::start, null);
        }
        catch (Exception e) {
            LOG.info("LedgerWorkflow already started");
        }
    }

    public LedgerWorkflow.LedgerRecord transfer(LedgerWorkflow.TransactionDetails transaction)
    {
        return workflow.transferMoney(new LedgerWorkflow.TransferMoneyInput(transaction));
    }

    public List<LedgerWorkflow.LedgerRecord> getHistory()
    {
        return workflow.getHistory();
    }
}
