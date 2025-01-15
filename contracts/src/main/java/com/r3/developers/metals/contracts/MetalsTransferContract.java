package com.r3.developers.metals.contracts;

import com.r3.developers.metals.misc.MetalTickerEnum;
import com.r3.developers.metals.states.TransferState;
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

public class MetalsTransferContract implements Contract {

  public static List<String> ALLOWED_TICKERS = EnumSet.allOf(MetalTickerEnum.class).stream().map(Enum::name).toList();

  public static final String TRANSFER_COMMAND_MUST_HAVE_EXACTLY_ONE_INPUT_STATE = "Transfer command must have exactly one input state.";
  public static final String TRANSFER_COMMAND_MUST_HAVE_EXACTLY_ONE_OUTPUT_STATE = "Transfer command must have exactly one output state.";
  public static final String TRANSACTION_MUST_CONTAIN_TRANSFER_COMMAND = "Transaction must contain Transfer command.";
  public static final String INPUT_STATE_MUST_HAVE_OWNER_SIGNATURE = "Input Transfer state must have Owner's signature.";
  public static final String INPUT_STATE_MUST_BE_TRANSFER_STATE = "Input state must be Transfer state.";
  public static final String STATE_MUST_CONTAIN_ALLOWED_TICKER = "Output Transfer state must contain allowed ticker.";

  @Override
  public void verify(@NotNull UtxoLedgerTransaction transaction) {
    Optional<Command> optionalCommand = Optional.of(transaction)
      .map(UtxoLedgerTransaction::getCommands)
      .filter(CollectionUtils::isNotEmpty)
      .map(List::iterator)
      .map(Iterator::next);

    MetalsCommands.TransferCommand transferCommand = optionalCommand
      .filter(MetalsCommands.TransferCommand.class::isInstance)
      .map(MetalsCommands.TransferCommand.class::cast)
      .orElseThrow(() -> new CordaRuntimeException(TRANSACTION_MUST_CONTAIN_TRANSFER_COMMAND));

    ValidationUtils.requireThat(transaction.getInputContractStates().size() == 1, TRANSFER_COMMAND_MUST_HAVE_EXACTLY_ONE_INPUT_STATE);
    ValidationUtils.requireThat(transaction.getOutputContractStates().size() == 1, TRANSFER_COMMAND_MUST_HAVE_EXACTLY_ONE_OUTPUT_STATE);

    TransferState inputTransferState = ValidationUtils.getState(UtxoLedgerTransaction::getInputContractStates, TransferState.class, transaction, INPUT_STATE_MUST_BE_TRANSFER_STATE);

    ValidationUtils.requireThat(ALLOWED_TICKERS.stream().anyMatch(allowedTicker -> StringUtils.equalsIgnoreCase(allowedTicker, inputTransferState.getMetalTicker())), STATE_MUST_CONTAIN_ALLOWED_TICKER);

    PublicKey owner = inputTransferState.getOwner();
    ValidationUtils.requireThat(transaction.getSignatories().contains(owner), INPUT_STATE_MUST_HAVE_OWNER_SIGNATURE);
  }
}
