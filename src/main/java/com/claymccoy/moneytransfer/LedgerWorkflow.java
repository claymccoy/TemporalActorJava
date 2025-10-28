package com.claymccoy.moneytransfer;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;
import java.util.Queue;

@WorkflowInterface
public interface LedgerWorkflow
{
    @WorkflowMethod
    void start(StartInput startInput);

    @QueryMethod
    List<LedgerRecord> getHistory();

    @UpdateMethod
    LedgerRecord transferMoney(TransferMoneyInput transferInput);

    record TransactionDetails(String sourceAccountId, String destinationAccountId, int amount) {}

    record LedgerRecord(String id, TransactionDetails transactionDetails) {}

    record StartInput(List<LedgerRecord> history, long nextId, Queue<LedgerRecord> ledgerRecordQueue, int eventLimit) {}

    record TransferMoneyInput(TransactionDetails transactionDetails) {}
}
