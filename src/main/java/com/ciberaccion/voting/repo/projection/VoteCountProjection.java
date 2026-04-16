// src/main/java/com/ciberaccion/voting/repo/projection/VoteCountProjection.java
package com.ciberaccion.voting.repo.projection;

public interface VoteCountProjection {
    Long getContestantId();
    String getContestantName();
    Long getVoteCount();
}