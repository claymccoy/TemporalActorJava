package com.claymccoy;

import com.claymccoy.moneytransfer.LedgerService;
import com.claymccoy.moneytransfer.LedgerWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/service")
public class ServiceResource
{
    private static final Logger LOG = LoggerFactory.getLogger(ServiceResource.class);

    private final LedgerService ledgerService;

    @Autowired
    public ServiceResource(LedgerService transferService)
    {
        this.ledgerService = transferService;
    }

    @GetMapping(value = "/transfer", produces = MediaType.APPLICATION_JSON_VALUE)
    public LedgerWorkflow.LedgerRecord transfer()
    {
        final var transactionDetails = new LedgerWorkflow.TransactionDetails("123", "456", 42);
        LOG.info("Initiating transfer of {}.\n\n", transactionDetails);
        return ledgerService.transfer(transactionDetails);
    }

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LedgerWorkflow.LedgerRecord> getHistory()
    {
        return ledgerService.getHistory();
    }
}
