package com.r3.developers.metals.contracts;

import net.corda.v5.ledger.utxo.Command;

public interface MetalsCommands extends Command {
  class MintCommand implements MetalsCommands {}
  class TransferCommand implements MetalsCommands{}
}
