package com.r3.developers.metals.workflows;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.security.PublicKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendMetalRequest {
  
  String metalTicker;
  int weight;
  PublicKey recipient;
}
