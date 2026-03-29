/**
 * Mô tả thực thể điểm của hệ thống.
 */
package com.qlsv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "scores")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    @Column(name = "process_score", nullable = false)
    private Double processScore;

    @Column(name = "midterm_score", nullable = false)
    private Double midtermScore;

    @Column(name = "final_score", nullable = false)
    private Double finalScore;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(name = "result", nullable = false, length = 20)
    private String result;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Khởi tạo điểm.
     */
    public Score() {
    }

    /**
     * Khởi tạo điểm.
     */
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

    /**
     * Trả về id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Cập nhật id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Trả về đăng ký.
     */
    public Enrollment getEnrollment() {
        return enrollment;
    }

    /**
     * Cập nhật đăng ký.
     */
    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    /**
     * Trả về process điểm.
     */
    public Double getProcessScore() {
        return processScore;
    }

    /**
     * Cập nhật process điểm.
     */
    public void setProcessScore(Double processScore) {
        this.processScore = processScore;
    }

    /**
     * Trả về midterm điểm.
     */
    public Double getMidtermScore() {
        return midtermScore;
    }

    /**
     * Cập nhật midterm điểm.
     */
    public void setMidtermScore(Double midtermScore) {
        this.midtermScore = midtermScore;
    }

    /**
     * Trả về final điểm.
     */
    public Double getFinalScore() {
        return finalScore;
    }

    /**
     * Cập nhật final điểm.
     */
    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }

    /**
     * Trả về tổng điểm.
     */
    public Double getTotalScore() {
        return totalScore;
    }

    /**
     * Cập nhật tổng điểm.
     */
    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    /**
     * Trả về kết quả.
     */
    public String getResult() {
        return result;
    }

    /**
     * Cập nhật kết quả.
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * Trả về updated at.
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Cập nhật updated at.
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
