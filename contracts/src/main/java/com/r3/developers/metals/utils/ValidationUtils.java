package com.r3.developers.metals.utils;

import lombok.experimental.UtilityClass;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@UtilityClass
public class ValidationUtils {

  public static void requireThat(boolean asserted, String errorMessage) {
    if (!asserted) {
      throw new CordaRuntimeException(String.format("Failed requirement: %s", errorMessage));
    }
  }

  public static <T> T getState(Function<UtxoLedgerTransaction, List<ContractState>> getStateFunction,
                               Class<T> clazz,
                               UtxoLedgerTransaction transaction,
                               String errorMessage) {
    return Optional.of(getStateFunction.apply(transaction))
      .map(List::iterator)
      .map(Iterator::next)
      .filter(clazz::isInstance)
      .map(clazz::cast)
      .orElseThrow(() -> new CordaRuntimeException(errorMessage));
  }
}
