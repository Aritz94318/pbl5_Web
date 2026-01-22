package edu.mondragon.we2.pinkAlert.model;

public record SimEvent(
        String actor,   
        int actorId,
        String text,
        long ts
) {}
