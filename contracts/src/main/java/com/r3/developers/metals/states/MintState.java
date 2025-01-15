package com.r3.developers.metals.states;

import com.r3.developers.metals.contracts.MetalsMintContract;
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

@BelongsToContract(MetalsMintContract.class)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MintState implements ContractState {
    @Builder.Default
    UUID id = UUID.randomUUID();
    String metalTicker;
    int weight;
    PublicKey minter;
    PublicKey owner;
    List<PublicKey> participants;

    @ConstructorForDeserialization
    public MintState(UUID id, String metalTicker, int weight, PublicKey minter, PublicKey owner, List<PublicKey> participants) {
        this.id = id;
        this.metalTicker = metalTicker;
        this.weight = weight;
        this.minter = minter;
        this.owner = owner;
        this.participants = participants;
    }

    @Override
    public List<PublicKey> getParticipants() {
        return List.of(minter, owner);
    }
}
