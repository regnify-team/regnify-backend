package com.regnify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.regnify.model.ValidationRules;

@Repository
public interface ValidationRulesRepository extends JpaRepository<ValidationRules, Long> {

}

