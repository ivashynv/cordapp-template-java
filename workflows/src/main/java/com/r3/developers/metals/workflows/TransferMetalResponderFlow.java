package com.r3.developers.metals.workflows;

import lombok.extern.java.Log;
import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.flows.InitiatedBy;
import net.corda.v5.application.flows.ResponderFlow;
import net.corda.v5.application.messaging.FlowSession;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

@InitiatedBy(protocol = "transfer-metal-protocol")
@Log
public class TransferMetalResponderFlow implements ResponderFlow {

  @CordaInject
  UtxoLedgerService utxoLedgerService;

  @Override
  public void call(@NotNull FlowSession session) {
    try {
//      MemberX500Name owner = session.getCounterparty();
      UtxoSignedTransaction finalizedSignedTransaction = utxoLedgerService.receiveFinality(session, _transaction -> {}).getTransaction();
      log.log(Level.INFO, String.format("Finished transfer flow. Tx: %s", finalizedSignedTransaction.getId()));
    } catch (Exception e) {
      log.log(Level.WARNING, "Exception occurred in transfer metal flow", e);
    }
  }
}
