package com.claymccoy.moneytransfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class MoneyTransferWorkflowImpl
        implements MoneyTransferWorkflow
{
    private static final Logger LOG = LoggerFactory.getLogger(MoneyTransferWorkflowImpl.class);
    private static final String WITHDRAW = "Withdraw";

    // RetryOptions specify how to automatically handle retries when Activities fail
    private final RetryOptions retryoptions = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(1)) // Wait 1 second before first retry
            .setMaximumInterval(Duration.ofSeconds(20)) // Do not exceed 20 seconds between retries
            .setBackoffCoefficient(2) // Wait 1 second, then 2, then 4, etc
            .setMaximumAttempts(3) // Fail after 3 attempts
            .build();

    // ActivityOptions specify the limits on how long an Activity can execute before
    // being interrupted by the Orchestration service
    private final ActivityOptions defaultActivityOptions = ActivityOptions.newBuilder()
            .setRetryOptions(retryoptions) // Apply the RetryOptions defined above
            .setStartToCloseTimeout(Duration.ofSeconds(2)) // Max execution time for single Activity
            .setScheduleToCloseTimeout(Duration.ofSeconds(5000)) // Entire duration from scheduling to completion including queue time
            .build();

    private final Map<String, ActivityOptions> perActivityMethodOptions = new HashMap<String, ActivityOptions>() {{
        // A heartbeat time-out is a proof-of life indicator that an activity is still working.
        // The 5 second duration used here waits for up to 5 seconds to hear a heartbeat.
        // If one is not heard, the Activity fails.
        // The `withdraw` method is hard-coded to succeed, so this never happens.
        // Use heartbeats for long-lived event-driven applications.
            put(WITHDRAW, ActivityOptions.newBuilder().setHeartbeatTimeout(Duration.ofSeconds(5)).build());
        }
    };

    // ActivityStubs enable calls to methods as if the Activity object is local but actually perform an RPC invocation
    private final AccountActivity accountActivityStub = Workflow.newActivityStub(AccountActivity.class, defaultActivityOptions, perActivityMethodOptions);

    // The transfer method is the entry point to the Workflow
    // Activity method executions can be orchestrated here or from within other Activity methods
    @Override
    public void transfer(TransferInput transferInput)
    {
        final var transaction = transferInput.transactionDetails();
        final var sourceAccountId = transaction.sourceAccountId();
        final var destinationAccountId = transaction.destinationAccountId();
        final var referenceId = transferInput.id();

        // Withdraw funds from source
        try {
            accountActivityStub.withdraw(sourceAccountId, referenceId, transaction.amount());
        }
        catch (Exception e) {
            LOG.info("[%s] Withdrawal of $%d from account %s failed", referenceId, transaction.amount(), sourceAccountId);
            throw(e);
        }

        // Deposit funds to destination
        try {
            accountActivityStub.deposit(destinationAccountId, referenceId, transaction.amount());
            LOG.info("[%s] Transaction succeeded.\n", referenceId);
        }
        catch (Exception eDeposit) {
            // If the deposit fails, for any exception, it's caught here
            LOG.info("[%s] Deposit of $%d to account %s failed.\n", referenceId, transaction.amount(), destinationAccountId);
            try {
                // Perform `refund` Activity
                LOG.info("[%s] Refunding $%d to account %s.\n", referenceId, transaction.amount(), sourceAccountId);
                accountActivityStub.refund(sourceAccountId, referenceId, transaction.amount());
                LOG.info("[%s] Refund to originating account was successful.\n", referenceId);
                LOG.info("[%s] Transaction is complete. No transfer made.\n", referenceId);
            }
            catch (Exception eRefund) {
                // A recovery mechanism can fail too. Handle any exception here
                LOG.info("[%s] Deposit of $%d to account %s failed. Did not compensate withdrawal.\n",
                        referenceId, transaction.amount(), destinationAccountId);
                LOG.info("[%s] Workflow failed.", referenceId);

                // Rethrowing the exception causes a Workflow Task failure
                throw(eRefund);
            }
        }
    }
}
