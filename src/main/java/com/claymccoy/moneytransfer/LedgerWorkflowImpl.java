package com.claymccoy.moneytransfer;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.ParentClosePolicy;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class LedgerWorkflowImpl
        implements LedgerWorkflow
{
    private int eventLimit = 3;
    private int eventCounter;
    private long nextId = 1;
    private final List<LedgerRecord> history = new ArrayList<>();
    private final Queue<LedgerRecord> unhandledLedgerRecordQueue = new ArrayDeque<>();

    @WorkflowInit
    public LedgerWorkflowImpl(StartInput startInput)
    {
        if (startInput != null) {
            eventLimit = startInput.eventLimit();
            nextId = startInput.nextId();
            history.addAll(startInput.history());
            unhandledLedgerRecordQueue.addAll(startInput.ledgerRecordQueue());
        }
    }

    @Override
    public void start(StartInput startInput)
    {
        while (true) {
            Workflow.await(() -> !unhandledLedgerRecordQueue.isEmpty());
            handleTransferMoneyInput(unhandledLedgerRecordQueue.poll());
            if (Workflow.getInfo().isContinueAsNewSuggested() || eventCounter > eventLimit) {
                Workflow.continueAsNew(new StartInput(history, nextId, unhandledLedgerRecordQueue, eventLimit));
            }
        }
    }

    @Override
    public List<LedgerRecord> getHistory()
    {
        return new ArrayList<>(history);
    }

    @Override
    public LedgerRecord transferMoney(TransferMoneyInput transferInput)
    {
        eventCounter++;
        final var transactionId = Long.toString(nextId++);
        final var ledgerRecord = new LedgerRecord(transactionId, transferInput.transactionDetails());
        unhandledLedgerRecordQueue.add(ledgerRecord);
        history.add(ledgerRecord);
        return ledgerRecord;
    }

    public void handleTransferMoneyInput(LedgerRecord ledgerRecord)
    {
        final var moneyTransferWorkflow = Workflow.newChildWorkflowStub(MoneyTransferWorkflow.class,
                ChildWorkflowOptions.newBuilder()
                        .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_ABANDON)
                        .setWorkflowId("moneyTransfer " + ledgerRecord.id())
                        .build());
        Async.procedure(moneyTransferWorkflow::transfer, new MoneyTransferWorkflow.TransferInput(ledgerRecord.id(), ledgerRecord.transactionDetails()));
        Promise<WorkflowExecution> childExecution = Workflow.getWorkflowExecution(moneyTransferWorkflow);
        // wait for child to start
        childExecution.get();
    }
}
