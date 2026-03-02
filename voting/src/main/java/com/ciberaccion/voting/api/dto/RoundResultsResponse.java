package com.ciberaccion.voting.api.dto;

import java.util.List;

public class RoundResultsResponse {

    private Long roundId;
    private List<ResultItem> results;

    public RoundResultsResponse(Long roundId, List<ResultItem> results) {
        this.roundId = roundId;
        this.results = results;
    }

    public Long getRoundId() { return roundId; }
    public List<ResultItem> getResults() { return results; }

    public static class ResultItem {
        private Long contestantId;
        private long votes;

        public ResultItem(Long contestantId, long votes) {
            this.contestantId = contestantId;
            this.votes = votes;
        }

        public Long getContestantId() { return contestantId; }
        public long getVotes() { return votes; }
    }
}