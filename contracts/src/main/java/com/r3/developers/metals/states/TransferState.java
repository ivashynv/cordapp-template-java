package com.r3.developers.metals.states;

import com.r3.developers.metals.contracts.MetalsTransferContract;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import net.corda.v5.base.annotations.ConstructorForDeserialization;
import net.corda.v5.ledger.utxo.BelongsToContract;
import net.corda.v5.ledger.utxo.ContractState;

import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

@BelongsToContract(MetalsTransferContract.class)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferState implements ContractState {
    UUID id;
    String metalTicker;
    int weight;
    PublicKey owner;
    PublicKey recipient;
    List<PublicKey> participants;

    @ConstructorForDeserialization
    public TransferState(UUID id, String metalTicker, int weight, PublicKey owner, PublicKey recipient, List<PublicKey> participants) {
        this.id = id;
        this.metalTicker = metalTicker;
        this.weight = weight;
        this.owner = owner;
        this.recipient = recipient;
        this.participants = participants;
    }

    @Override
    public List<PublicKey> getParticipants() {
        return List.of(owner, recipient);
    }
}
