// src/main/java/com/ciberaccion/voting/repo/projection/NomineeProjection.java
package com.ciberaccion.voting.repo.projection;

public interface NomineeProjection {
    Long getContestantId();
    String getContestantName();
}