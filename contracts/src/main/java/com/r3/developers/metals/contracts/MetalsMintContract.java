package com.r3.developers.metals.contracts;

import com.r3.developers.metals.misc.MetalTickerEnum;
import com.r3.developers.metals.states.MintState;
import com.r3.developers.metals.utils.ValidationUtils;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.Contract;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class MetalsMintContract implements Contract {

  public static List<String> ALLOWED_TICKERS = EnumSet.allOf(MetalTickerEnum.class).stream().map(Enum::name).toList();

  public static final String MINT_COMMAND_MUST_HAVE_NO_INPUT_STATES = "Mint command must have no input states.";
  public static final String MINT_COMMAND_MUST_HAVE_EXACTLY_ONE_OUTPUT_STATE = "Mint command must have exactly one  output state.";
  public static final String TRANSACTION_MUST_CONTAIN_MINT_COMMAND = "Transaction must contain Mint command.";
  public static final String OUTPUT_MINT_STATE_MUST_HAVE_MINTER_SIGNATURE = "Transaction must have Minter's signature.";
  public static final String OUTPUT_STATE_MUST_BE_MINT_STATE = "Output state must be Mint state.";
  public static final String OUTPUT_STATE_MUST_CONTAIN_ALLOWED_TICKER = "Output Mint state must contain allowed ticker.";

  @Override
  public void verify(@NotNull UtxoLedgerTransaction transaction) {
    Optional<Command> optionalCommand = Optional.of(transaction)
      .map(UtxoLedgerTransaction::getCommands)
      .filter(CollectionUtils::isNotEmpty)
      .map(List::iterator)
      .map(Iterator::next);

    // Transaction verification will happen in MetalsTransferContract
    if (optionalCommand.map(MetalsCommands.TransferCommand.class::isInstance).orElse(false)) {
      return;
    }

    MetalsCommands.MintCommand mintCommand = optionalCommand
      .filter(MetalsCommands.MintCommand.class::isInstance)
      .map(MetalsCommands.MintCommand.class::cast)
      .orElseThrow(() -> new CordaRuntimeException(TRANSACTION_MUST_CONTAIN_MINT_COMMAND));

    ValidationUtils.requireThat(transaction.getInputContractStates().isEmpty(), MINT_COMMAND_MUST_HAVE_NO_INPUT_STATES);
    ValidationUtils.requireThat(transaction.getOutputContractStates().size() == 1, MINT_COMMAND_MUST_HAVE_EXACTLY_ONE_OUTPUT_STATE);

    MintState outputMintState = ValidationUtils.getState(UtxoLedgerTransaction::getOutputContractStates, MintState.class, transaction, OUTPUT_STATE_MUST_BE_MINT_STATE);

    ValidationUtils.requireThat(ALLOWED_TICKERS.stream().anyMatch(allowedTicker -> StringUtils.equalsIgnoreCase(allowedTicker, outputMintState.getMetalTicker())), OUTPUT_STATE_MUST_CONTAIN_ALLOWED_TICKER);

    PublicKey minter = outputMintState.getMinter();
    ValidationUtils.requireThat(transaction.getSignatories().contains(minter), OUTPUT_MINT_STATE_MUST_HAVE_MINTER_SIGNATURE);
    
  }
}
