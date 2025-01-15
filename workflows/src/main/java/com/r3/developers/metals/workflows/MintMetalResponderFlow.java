package com.r3.developers.metals.workflows;

import lombok.extern.java.Log;
import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.flows.FlowEngine;
import net.corda.v5.application.flows.InitiatedBy;
import net.corda.v5.application.flows.InitiatingFlow;
import net.corda.v5.application.flows.ResponderFlow;
import net.corda.v5.application.flows.SubFlow;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.application.membership.MemberLookup;
import net.corda.v5.application.messaging.FlowMessaging;
import net.corda.v5.application.messaging.FlowSession;
import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.ledger.common.NotaryLookup;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

@InitiatedBy(protocol = "mint-metal-protocol")
@Log
public class MintMetalResponderFlow implements ResponderFlow {

  @CordaInject
  public FlowMessaging flowMessaging;

  @CordaInject
  public JsonMarshallingService jsonMarshallingService;

  @CordaInject
  public MemberLookup memberLookup;

  @CordaInject
  NotaryLookup notaryLookup;

  @CordaInject
  UtxoLedgerService utxoLedgerService;

  @CordaInject
  public FlowEngine flowEngine;

  @Override
  public void call(@NotNull FlowSession session) {
    try {
//      MemberX500Name owner = session.getCounterparty();
      UtxoSignedTransaction finalizedSignedTransaction = utxoLedgerService.receiveFinality(session, _transaction -> {}).getTransaction();
      log.log(Level.INFO, String.format("Finished mint responder flow. Tx: %s", finalizedSignedTransaction.getId()));
    } catch (Exception e) {
      log.log(Level.WARNING, "Exception occurred in mint responder flow", e);
    }
  }
}
