package com.nttdata.eva.whatsapp.model;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EVARequestTuple {
    private final String content;
    private final ObjectNode context;
}