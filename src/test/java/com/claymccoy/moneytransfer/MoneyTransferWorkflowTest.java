package com.claymccoy.moneytransfer;

import com.google.common.collect.ImmutableList;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.testing.TestWorkflowExtension;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoneyTransferWorkflowTest
{
    enum StepType {
        WITHDRAW, DEPOSIT, REFUND
    }

    enum StepStatus {
        SUCCESS, FAILURE
    }

    record AccountActivityStep(StepType type, StepStatus status, String accountId, String referenceId, int amount) {}

    static class FakeAccountActivity
            implements AccountActivity
    {
        private final boolean failWithdrawal;
        private final boolean failDeposit;
        private final boolean failRefund;
        private final List<AccountActivityStep> activitySteps = new ArrayList<>();

        public FakeAccountActivity()
        {
            this(false, false, false);
        }

        public FakeAccountActivity(boolean failWithdrawal, boolean failDeposit, boolean failRefund)
        {
            this.failWithdrawal = failWithdrawal;
            this.failDeposit = failDeposit;
            this.failRefund = failRefund;
        }

        public List<AccountActivityStep> getActivitySteps()
        {
            return ImmutableList.copyOf(activitySteps);
        }

        @Override
        public void withdraw(String accountId, String referenceId, int amount)
        {
            if (failWithdrawal) {
                activitySteps.add(new AccountActivityStep(StepType.WITHDRAW, StepStatus.FAILURE, accountId, referenceId, amount));
                throw new RuntimeException("Withdraw failed");
            }
            else {
                activitySteps.add(new AccountActivityStep(StepType.WITHDRAW, StepStatus.SUCCESS, accountId, referenceId, amount));
            }
        }

        // Mock up the deposit of an amount of money from the destination account
        @Override
        public void deposit(String accountId, String referenceId, int amount)
        {
            if (failDeposit) {
                activitySteps.add(new AccountActivityStep(StepType.DEPOSIT, StepStatus.FAILURE, accountId, referenceId, amount));
                throw new RuntimeException("Deposit failed");
            }
            else {
                activitySteps.add(new AccountActivityStep(StepType.DEPOSIT, StepStatus.SUCCESS, accountId, referenceId, amount));
            }
        }

        // Mock up a compensation refund to the source account
        @Override
        public void refund(String accountId, String referenceId, int amount)
        {
            if (failRefund) {
                activitySteps.add(new AccountActivityStep(StepType.REFUND, StepStatus.FAILURE, accountId, referenceId, amount));
                throw new RuntimeException("Refund failed");
            }
            else {
                activitySteps.add(new AccountActivityStep(StepType.REFUND, StepStatus.SUCCESS, accountId, referenceId, amount));
            }
        }
    }

    @RegisterExtension
    public static final TestWorkflowExtension testWorkflowExtension =
            TestWorkflowExtension.newBuilder()
                    .registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class)
                    .setDoNotStart(true)
                    .build();

    @Test
    public void testWithdrawalAndDeposit(TestWorkflowEnvironment testEnv, Worker worker, MoneyTransferWorkflow workflow)
    {
        final var accountActivity = new FakeAccountActivity();
        worker.registerActivitiesImplementations(accountActivity);
        testEnv.start();
        final var transaction = new LedgerWorkflow.TransactionDetails("123", "456", 42);
        workflow.transfer(new MoneyTransferWorkflow.TransferInput("1", transaction));
        assertEquals(ImmutableList.of(
                new AccountActivityStep(StepType.WITHDRAW, StepStatus.SUCCESS, "123", "1", 42),
                new AccountActivityStep(StepType.DEPOSIT, StepStatus.SUCCESS, "456", "1", 42)

        ), accountActivity.getActivitySteps());
    }

    @Test
    public void testWithdrawalFail(TestWorkflowEnvironment testEnv, Worker worker, MoneyTransferWorkflow workflow)
    {
        final var accountActivity = new FakeAccountActivity(true, false, false);
        worker.registerActivitiesImplementations(accountActivity);
        testEnv.start();
        final var transaction = new LedgerWorkflow.TransactionDetails("123", "456", 42);
        final var e = assertThrows(RuntimeException.class, () -> {
            workflow.transfer(new MoneyTransferWorkflow.TransferInput("1", transaction));
        });
        assertTrue(e.getCause().getMessage().startsWith("Activity with activityType='Withdraw' failed"));
        assertEquals(ImmutableList.of(
                new AccountActivityStep(StepType.WITHDRAW, StepStatus.FAILURE, "123", "1", 42),
                new AccountActivityStep(StepType.WITHDRAW, StepStatus.FAILURE, "123", "1", 42),
                new AccountActivityStep(StepType.WITHDRAW, StepStatus.FAILURE, "123", "1", 42)
        ), accountActivity.getActivitySteps());
    }

    @Test
    public void testWithdrawalThenDepositFailThenRefund(TestWorkflowEnvironment testEnv, Worker worker, MoneyTransferWorkflow workflow)
    {
        final var accountActivity = new FakeAccountActivity(false, true, false);
        worker.registerActivitiesImplementations(accountActivity);
        testEnv.start();
        final var transaction = new LedgerWorkflow.TransactionDetails("123", "456", 42);
        workflow.transfer(new MoneyTransferWorkflow.TransferInput("1", transaction));
        assertEquals(ImmutableList.of(
                new AccountActivityStep(StepType.WITHDRAW, StepStatus.SUCCESS, "123", "1", 42),
                new AccountActivityStep(StepType.DEPOSIT, StepStatus.FAILURE, "456", "1", 42),
                new AccountActivityStep(StepType.DEPOSIT, StepStatus.FAILURE, "456", "1", 42),
                new AccountActivityStep(StepType.DEPOSIT, StepStatus.FAILURE, "456", "1", 42),
                new AccountActivityStep(StepType.REFUND, StepStatus.SUCCESS, "123", "1", 42)
        ), accountActivity.getActivitySteps());
    }

    @Test
    public void testWithdrawalThenDepositFailThenRefundFail(TestWorkflowEnvironment testEnv, Worker worker, MoneyTransferWorkflow workflow)
    {
        final var accountActivity = new FakeAccountActivity(false, true, true);
        worker.registerActivitiesImplementations(accountActivity);
        testEnv.start();
        final var transaction = new LedgerWorkflow.TransactionDetails("123", "456", 42);
        final var e = assertThrows(RuntimeException.class, () -> workflow.transfer(new MoneyTransferWorkflow.TransferInput("1", transaction)));
        assertTrue(e.getCause().getMessage().startsWith("Activity with activityType='Refund' failed"));
        assertEquals(ImmutableList.of(
                new AccountActivityStep(StepType.WITHDRAW, StepStatus.SUCCESS, "123", "1", 42),
                new AccountActivityStep(StepType.DEPOSIT, StepStatus.FAILURE, "456", "1", 42),
                new AccountActivityStep(StepType.DEPOSIT, StepStatus.FAILURE, "456", "1", 42),
                new AccountActivityStep(StepType.DEPOSIT, StepStatus.FAILURE, "456", "1", 42),
                new AccountActivityStep(StepType.REFUND, StepStatus.FAILURE, "123", "1", 42),
                new AccountActivityStep(StepType.REFUND, StepStatus.FAILURE, "123", "1", 42),
                new AccountActivityStep(StepType.REFUND, StepStatus.FAILURE, "123", "1", 42)
        ), accountActivity.getActivitySteps());
    }
}
