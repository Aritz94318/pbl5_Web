package edu.mondragon.we2.pinkAlert.model;

public record SimEvent(
        String actor,   // "PATIENT", "DOCTOR", "MACHINE"
        int actorId,
        String text,
        long ts
) {}
