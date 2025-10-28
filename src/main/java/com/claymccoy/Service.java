package com.claymccoy;

import com.claymccoy.moneytransfer.MoneyTransferWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ServiceConfig.class)
public class Service implements CommandLineRunner
{
    @Autowired
    private MoneyTransferWorker moneyTransferWorker;

    public static void main(String[] args)
    {
        SpringApplication.run(Service.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        moneyTransferWorker.init();
    }
}
