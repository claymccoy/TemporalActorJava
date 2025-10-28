package com.claymccoy.moneytransfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.temporal.activity.Activity;

public class AccountActivityImpl
        implements AccountActivity
{
    private static final Logger LOG = LoggerFactory.getLogger(AccountActivityImpl.class);

    // Mock up the withdrawal of an amount of money from the source account
    @Override
    public void withdraw(String accountId, String referenceId, int amount)
    {
        LOG.info("Withdrawing $%d from account %s.\n[ReferenceId: %s]\n", amount, accountId, referenceId);
    }

    // Mock up the deposit of an amount of money from the destination account
    @Override
    public void deposit(String accountId, String referenceId, int amount)
    {
        boolean activityShouldSucceed = false;
        if (!activityShouldSucceed) {
            LOG.info("Deposit failed");
            throw Activity.wrap(new RuntimeException("Simulated Activity error during deposit of funds"));
        }
        LOG.info("Depositing $%d into account %s.\n[ReferenceId: %s]\n", amount, accountId, referenceId);
    }

    // Mock up a compensation refund to the source account
    @Override
    public void refund(String accountId, String referenceId, int amount)
    {
        boolean activityShouldSucceed = true;
        if (!activityShouldSucceed) {
            LOG.info("Refund failed");
            throw Activity.wrap(new RuntimeException("Simulated Activity error during refund to source account"));
        }
        LOG.info("Refunding $%d to account %s.\n[ReferenceId: %s]\n", amount, accountId, referenceId);
    }
}
