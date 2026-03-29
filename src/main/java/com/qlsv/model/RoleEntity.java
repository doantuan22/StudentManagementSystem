/**
 * Mô tả thực thể vai trò entity của hệ thống.
 */
package com.qlsv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, unique = true, length = 50)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    /**
     * Khởi tạo vai trò entity.
     */
    public RoleEntity() {
    }

    /**
     * Khởi tạo vai trò entity.
     */
    public RoleEntity(Long id, String roleCode, String roleName) {
        this.id = id;
        this.roleCode = roleCode;
        this.roleName = roleName;
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
     * Trả về vai trò mã.
     */
    public String getRoleCode() {
        return roleCode;
    }

    /**
     * Cập nhật vai trò mã.
     */
    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    /**
     * Trả về vai trò tên.
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Cập nhật vai trò tên.
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Xử lý to vai trò enum.
     */
    public Role toRoleEnum() {
        return Role.fromCode(roleCode);
    }

    /**
     * Xử lý from enum.
     */
    public static RoleEntity fromEnum(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleEntity(null, role.getCode(), role.getDisplayName());
    }

    /**
     * Trả về chuỗi hiển thị của đối tượng.
     */
    @Override
    public String toString() {
        return roleName == null || roleName.isBlank() ? roleCode : roleName;
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RoleEntity that)) {
            return false;
        }
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(roleCode, that.roleCode);
    }

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : roleCode);
    }
}
