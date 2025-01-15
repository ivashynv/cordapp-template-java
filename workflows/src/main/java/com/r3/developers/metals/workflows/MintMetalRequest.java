package com.r3.developers.metals.workflows;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.corda.v5.base.types.MemberX500Name;

import java.security.PublicKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MintMetalRequest {
  
  String metalTicker;
  int weight;
  PublicKey owner;
}
