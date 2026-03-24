package com.qlsv.model;

public class Score {

    private Long id;
    private Enrollment enrollment;
    private Double processScore;
    private Double midtermScore;
    private Double finalScore;
    private Double totalScore;
    private String result;

    public Score() {
    }

    public Score(Long id, Enrollment enrollment, Double processScore, Double midtermScore,
                 Double finalScore, Double totalScore, String result) {
        this.id = id;
        this.enrollment = enrollment;
        this.processScore = processScore;
        this.midtermScore = midtermScore;
        this.finalScore = finalScore;
        this.totalScore = totalScore;
        this.result = result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public Double getProcessScore() {
        return processScore;
    }

    public void setProcessScore(Double processScore) {
        this.processScore = processScore;
    }

    public Double getMidtermScore() {
        return midtermScore;
    }

    public void setMidtermScore(Double midtermScore) {
        this.midtermScore = midtermScore;
    }

    public Double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
