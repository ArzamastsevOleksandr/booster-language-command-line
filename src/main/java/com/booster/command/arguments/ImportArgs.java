package com.booster.command.arguments;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class ImportArgs implements Args {

    String filename;

}
