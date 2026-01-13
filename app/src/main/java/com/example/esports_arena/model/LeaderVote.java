package com.example.esports_arena.model;

public class LeaderVote {
    private int teamId;
    private int voterId;
    private int candidateId;
    private String voteTime;

    public LeaderVote() {
    }

    public LeaderVote(int teamId, int voterId, int candidateId, long voteTime) {
        this.teamId = teamId;
        this.voterId = voterId;
        this.candidateId = candidateId;
        setVoteTimeFromLong(voteTime);
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getVoterId() {
        return voterId;
    }

    public void setVoterId(int voterId) {
        this.voterId = voterId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    // Firebase requires String getter/setter for deserialization
    public String getVoteTime() {
        return voteTime;
    }

    public void setVoteTime(String voteTime) {
        this.voteTime = voteTime;
    }

    // Convenience methods for long conversion
    public long getVoteTimeAsLong() {
        if (voteTime == null || voteTime.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(voteTime);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public void setVoteTimeFromLong(long voteTime) {
        this.voteTime = String.valueOf(voteTime);
    }
}
