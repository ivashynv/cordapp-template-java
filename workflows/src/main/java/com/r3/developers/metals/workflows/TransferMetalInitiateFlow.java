package com.r3.developers.metals.workflows;

import com.r3.developers.metals.contracts.MetalsCommands;
import com.r3.developers.metals.states.TransferState;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import net.corda.v5.application.flows.ClientRequestBody;
import net.corda.v5.application.flows.ClientStartableFlow;
import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.flows.FlowEngine;
import net.corda.v5.application.flows.InitiatingFlow;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.application.membership.MemberLookup;
import net.corda.v5.application.messaging.FlowMessaging;
import net.corda.v5.application.messaging.FlowSession;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.ledger.common.NotaryLookup;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import net.corda.v5.membership.MemberInfo;
import net.corda.v5.membership.NotaryInfo;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;

@InitiatingFlow(protocol = "transfer-metal-protocol")
@NoArgsConstructor
@Log
public class TransferMetalInitiateFlow implements ClientStartableFlow {

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

  @Suspendable
  @NotNull
  @Override
  public String call(@NotNull ClientRequestBody requestBody) {
    MemberInfo aliceTrader = memberLookup.lookup(MemberX500Name.parse("CN=AliceTrader, OU=Test Dept, O=R3, L=London, C=GB"));
    MemberInfo bobTrader = memberLookup.lookup(MemberX500Name.parse("CN=BobTrader, OU=Test Dept, O=R3, L=London, C=GB"));
    
    NotaryInfo notaryInfo = notaryLookup.getNotaryServices().iterator().next();

    StateAndRef<TransferState> inputTransferState = null;
//    StateRef inputTransferState = checkForMetalStates();
    PublicKey owner = inputTransferState.getState().getContractState().getOwner();

    TransferState outputTransferState = TransferState.builder()
      .metalTicker("GOLD")
      .weight(100)
//      .owner(aliceTrader.getLedgerKeys().iterator().next())
      .owner(owner)
      .recipient(bobTrader.getLedgerKeys().iterator().next())
      .build();

    UtxoSignedTransaction transaction = utxoLedgerService.createTransactionBuilder()
      .setNotary(notaryInfo.getName())
      // No input states in Mint transaction
      .addInputState(inputTransferState.getRef())
      .addOutputState(outputTransferState)
      .addCommand(new MetalsCommands.TransferCommand())
      .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.DAYS))
      .addSignatories(List.of(aliceTrader.getLedgerKeys().iterator().next(), bobTrader.getLedgerKeys().iterator().next()))
      .toSignedTransaction();

    FlowSession aliceSession = flowMessaging.initiateFlow(aliceTrader.getName());
    FlowSession bobSession = flowMessaging.initiateFlow(bobTrader.getName());
    List<FlowSession> sessionList = List.of(aliceSession, bobSession);
    try {
      UtxoSignedTransaction signedTransaction = utxoLedgerService.finalize(transaction, sessionList).getTransaction();
      return signedTransaction.getId().toString();
    } catch (Exception e) {
      log.log(Level.WARNING, "Failed to finalize", e);
      return String.format("Failed to finalize. %s", e.getMessage()); 
    }
  }

/*
  // Example from old Corda 4 version
  StateAndRef<MetalState> checkForMetalStates() throws FlowException {

    // Find an UNCONSUMED state to enable metal transfer
    QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

    List<StateAndRef<MetalState>> MetalStates = getServiceHub().getVaultService().queryBy(MetalState.class, generalCriteria).getStates();

    boolean inputFound = false;
    int t = MetalStates.size();

    // For states, find the (first) one that has the same metal name and weight
    for (int x = 0; x < t; x++) {
      if (MetalStates.get(x).getState().getData().getMetalName().equals(metalName)
        && MetalStates.get(x).getState().getData().getWeight() == weight) {
        input = x;
        inputFound = true;
      }
    }


    if (inputFound) {
      System.out.println("\n Input Found");
    } else {
      System.out.println("\n Input not found");
      throw new FlowException();
    }

    return MetalStates.get(input);
  }
*/
}
