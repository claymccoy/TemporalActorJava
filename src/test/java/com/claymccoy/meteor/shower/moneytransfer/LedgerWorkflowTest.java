package com.claymccoy.meteor.shower.moneytransfer;

import com.google.common.collect.ImmutableList;
import io.temporal.client.WorkflowClient;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayDeque;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LedgerWorkflowTest
{
    @RegisterExtension
    public static final TestWorkflowExtension testWorkflowExtension =
            TestWorkflowExtension.newBuilder()
                    .registerWorkflowImplementationTypes(LedgerWorkflowImpl.class, FakeMoneyTransferWorkflow.class)
                    .build();

    @BeforeEach
    public void init()
    {
        transferCounter = 0;
    }

    private static int transferCounter;

    public static class FakeMoneyTransferWorkflow
            implements MoneyTransferWorkflow
    {
        @Override
        public void transfer(TransferInput transferInput)
        {
            transferCounter++;
        }
    }

    @Test
    public void testTransfersAddedToHistoryWithIncrementedId(TestWorkflowEnvironment testEnv, WorkflowClient workflowClient, LedgerWorkflow workflow)
    {
        final var wfExecution = WorkflowClient.start(workflow::start, null);
        workflow.transferMoney(new LedgerWorkflow.TransferMoneyInput(new LedgerWorkflow.TransactionDetails("123", "456", 42)));
        workflow.transferMoney(new LedgerWorkflow.TransferMoneyInput(new LedgerWorkflow.TransactionDetails("asd", "zxc", 57)));
        final var history = workflow.getHistory();
        final var expectedHistory = ImmutableList.of(
                new LedgerWorkflow.LedgerRecord("1", new LedgerWorkflow.TransactionDetails("123", "456", 42)),
                new LedgerWorkflow.LedgerRecord("2", new LedgerWorkflow.TransactionDetails("asd", "zxc", 57)));
        assertEquals(expectedHistory, history);
        assertEquals(2, transferCounter);
        final var workflowHistory = workflowClient.fetchHistory(wfExecution.getWorkflowId(), wfExecution.getRunId());
        final var continueAsNewEvents = workflowHistory.getEvents().stream().filter(historyEvent -> historyEvent.hasWorkflowExecutionContinuedAsNewEventAttributes()).toList();
        assertEquals(continueAsNewEvents.size(), 0);
    }

    @Test
    public void testContinueAsNewAfterEnoughTransfers(WorkflowClient workflowClient, LedgerWorkflow workflow)
    {
        final var startInput = new LedgerWorkflow.StartInput(new ArrayList<>(), 1, new ArrayDeque<>(), 3);
        final var wfExecution = WorkflowClient.start(workflow::start, startInput);
        workflow.transferMoney(new LedgerWorkflow.TransferMoneyInput(new LedgerWorkflow.TransactionDetails("123", "456", 42)));
        workflow.transferMoney(new LedgerWorkflow.TransferMoneyInput(new LedgerWorkflow.TransactionDetails("asd", "zxc", 57)));
        workflow.transferMoney(new LedgerWorkflow.TransferMoneyInput(new LedgerWorkflow.TransactionDetails("fgh", "vbn", 72)));
        workflow.transferMoney(new LedgerWorkflow.TransferMoneyInput(new LedgerWorkflow.TransactionDetails("rty", "uio", 101)));
        workflow.transferMoney(new LedgerWorkflow.TransferMoneyInput(new LedgerWorkflow.TransactionDetails("hjk", "bnm", 151)));
        final var history = workflow.getHistory();
        final var expectedHistory = ImmutableList.of(
                new LedgerWorkflow.LedgerRecord("1", new LedgerWorkflow.TransactionDetails("123", "456", 42)),
                new LedgerWorkflow.LedgerRecord("2", new LedgerWorkflow.TransactionDetails("asd", "zxc", 57)),
                new LedgerWorkflow.LedgerRecord("3", new LedgerWorkflow.TransactionDetails("fgh", "vbn", 72)),
                new LedgerWorkflow.LedgerRecord("4", new LedgerWorkflow.TransactionDetails("rty", "uio", 101)),
                new LedgerWorkflow.LedgerRecord("5", new LedgerWorkflow.TransactionDetails("hjk", "bnm", 151)));
        assertEquals(expectedHistory, history);
        assertEquals(5, transferCounter);
        final var workflowHistory = workflowClient.fetchHistory(wfExecution.getWorkflowId(), wfExecution.getRunId());
        final var continueAsNewEvents = workflowHistory.getEvents().stream().filter(historyEvent -> historyEvent.hasWorkflowExecutionContinuedAsNewEventAttributes()).toList();
        assertEquals(1, continueAsNewEvents.size());
    }
}
