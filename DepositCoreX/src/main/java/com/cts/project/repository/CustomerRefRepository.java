// src/main/java/com/example/deposit/repository/CustomerRefRepository.java
package com.cts.project.repository;

import com.cts.project.entity.CustomerRef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRefRepository extends JpaRepository<CustomerRef, Long> {}