package com.example.esports_arena.model;

public class LeaderVote {
    private int teamId;
    private int voterId;
    private int candidateId;
    private long voteTime;

    public LeaderVote() {
    }

    public LeaderVote(int teamId, int voterId, int candidateId, long voteTime) {
        this.teamId = teamId;
        this.voterId = voterId;
        this.candidateId = candidateId;
        this.voteTime = voteTime;
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

    public long getVoteTime() {
        return voteTime;
    }

    public void setVoteTime(long voteTime) {
        this.voteTime = voteTime;
    }
}
