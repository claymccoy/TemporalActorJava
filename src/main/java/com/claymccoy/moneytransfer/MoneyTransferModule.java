package com.claymccoy.moneytransfer;

import com.claymccoy.temporal.TemporalConfig;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MoneyTransferModule
{
    public static final String MONEY_TRANSFER_TASK_QUEUE = "MONEY_TRANSFER_TASK_QUEUE";

    @Bean
    public WorkflowServiceStubs workflowServiceStubs()
    {
        return WorkflowServiceStubs.newLocalServiceStubs();
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs)
    {
        return WorkflowClient.newInstance(serviceStubs);
    }

    @Bean
    public TemporalConfig temporalConfig(WorkflowClient client)
    {
        return new TemporalConfig(client, MONEY_TRANSFER_TASK_QUEUE);
    }

    @Bean
    public AccountActivity accountActivity()
    {
        return new AccountActivityImpl();
    }
}
