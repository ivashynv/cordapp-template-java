package com.r3.developers.metals.workflows;

import com.r3.developers.metals.contracts.MetalsCommands;
import com.r3.developers.metals.states.MintState;
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
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import net.corda.v5.membership.MemberInfo;
import net.corda.v5.membership.NotaryInfo;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;

@InitiatingFlow(protocol = "mint-metal-protocol")
@NoArgsConstructor
@Log
public class MintMetalInitiateFlow implements ClientStartableFlow {

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
    MemberInfo minter = memberLookup.lookup(MemberX500Name.parse("CN=Minter, OU=Test Dept, O=R3, L=London, C=GB"));
    
    NotaryInfo notaryInfo = notaryLookup.getNotaryServices().iterator().next();

    MintState mintOutputState = MintState.builder()
      .metalTicker("GOLD")
      .weight(100)
      .minter(minter.getLedgerKeys().iterator().next())
      .owner(aliceTrader.getLedgerKeys().iterator().next())
      .build();

    UtxoSignedTransaction transaction = utxoLedgerService.createTransactionBuilder()
      .setNotary(notaryInfo.getName())
      // No input states in Mint transaction
      .addOutputState(mintOutputState)
      .addCommand(new MetalsCommands.MintCommand())
      .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.DAYS))
      // Recipient's signature is not required because only Minter needs to sign the Mint transaction
      .addSignatories(List.of(minter.getLedgerKeys().iterator().next()))
      .toSignedTransaction();

    FlowSession session = flowMessaging.initiateFlow(aliceTrader.getName());
    List<FlowSession> sessionList = List.of(session);
    try {
      UtxoSignedTransaction signedTransaction = utxoLedgerService.finalize(transaction, sessionList).getTransaction();
      return signedTransaction.getId().toString();
    } catch (Exception e) {
      log.log(Level.WARNING, "Failed to finalize", e);
      return String.format("Failed to finalize. %s", e.getMessage()); 
    }
  }
}
