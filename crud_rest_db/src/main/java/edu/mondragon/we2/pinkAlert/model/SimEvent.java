package edu.mondragon.we2.pinkalert.model;

public record SimEvent(
        String actor,   
        int actorId,
        String text,
        long ts
) {}
