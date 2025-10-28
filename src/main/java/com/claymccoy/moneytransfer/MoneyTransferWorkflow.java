package com.claymccoy.moneytransfer;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MoneyTransferWorkflow
{
    @WorkflowMethod
    void transfer(TransferInput transferInput);

    record TransferInput(String id, LedgerWorkflow.TransactionDetails transactionDetails) {}
}
