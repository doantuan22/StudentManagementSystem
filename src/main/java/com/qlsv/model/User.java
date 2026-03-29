/**
 * Mô tả thực thể người dùng của hệ thống.
 */
package com.qlsv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", length = 150)
    private String email;

    // Keep a real FK mapping for JPA, while getRole()/setRole() still expose the enum used across the app.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity roleEntity;

    @Column(name = "active", nullable = false)
    private boolean active;

    /**
     * Khởi tạo người dùng.
     */
    public User() {
    }

    /**
     * Khởi tạo người dùng.
     */
    public User(Long id, String username, String passwordHash, String fullName, String email, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        setRole(role);
        this.active = active;
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
     * Trả về username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Cập nhật username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Trả về mật khẩu băm.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Cập nhật mật khẩu băm.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Trả về họ tên.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Cập nhật họ tên.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Trả về email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Cập nhật email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Trả về vai trò.
     */
    public Role getRole() {
        return roleEntity == null ? null : roleEntity.toRoleEnum();
    }

    /**
     * Cập nhật vai trò.
     */
    public void setRole(Role role) {
        this.roleEntity = RoleEntity.fromEnum(role);
    }

    /**
     * Trả về vai trò entity.
     */
    public RoleEntity getRoleEntity() {
        return roleEntity;
    }

    /**
     * Cập nhật vai trò entity.
     */
    public void setRoleEntity(RoleEntity roleEntity) {
        this.roleEntity = roleEntity;
    }

    /**
     * Kiểm tra đang hoạt động.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Cập nhật đang hoạt động.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Trả về chuỗi hiển thị của đối tượng.
     */
    @Override
    public String toString() {
        return fullName == null || fullName.isBlank() ? username : fullName + " (" + username + ")";
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof User user)) {
            return false;
        }
        if (id != null && user.id != null) {
            return Objects.equals(id, user.id);
        }
        return Objects.equals(username, user.username);
    }

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : username);
    }
}
