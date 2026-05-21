package com.depositcorex.productconfig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.depositcorex.productconfig.entity.DepositProduct;

@Repository
public interface DepositProductRepository extends JpaRepository<DepositProduct, Long> {}