package com.lineguard.repository;

import com.lineguard.model.ProductionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductionLineRepository extends JpaRepository<ProductionLine, Long> {
    Optional<ProductionLine> findByName(String name);
    boolean existsByName(String name);
}
