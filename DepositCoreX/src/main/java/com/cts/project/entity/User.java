package com.cts.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long userId;

    @Column(name = "Name", length = 150, nullable = false)
    private String name;

    @Column(name = "Role", length = 100)
    private String role;

    @Column(name = "Email", length = 200, unique = true)
    private String email;

    @Column(name = "Phone", length = 30)
    private String phone;
}