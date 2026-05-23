package com.regnify.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "validation_rules", indexes = {
    @Index(name = "idx_validation_rules_country_code", columnList = "country_code")
})

@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ValidationRules {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
	@Column(name = "section", nullable = false, unique = false, length = 500)
	private String section;
	 
	@Column(name = "business_field", nullable = false, unique = true, length = 100)
	private String businessField;
	 
    @Column(name = "ubl_xpath", nullable = false, unique = true, length = 500)
    private String UBLXPath;
    
    @Column(name = "ksa_id", nullable = true, unique = false, length = 100)
    private String KSAId;
    
    @Column(name = "flag", nullable = false, unique = false, length = 1)
    private Character flag;
    
    @Column(name = "when_missing", nullable = false, unique = false, length = 100)
    private String whenMissing;
    
    @Column(name = "error_code", nullable = false, unique = false, length = 100)
    private String errorCode;
    
    @Column(name = "error_message_template", nullable = false, unique = true, length = 500)
    private String errorMessageTemplate;
        
    @Column(name = "country_code", nullable = true, unique = false, length = 100)
    private String countryCode;
    
    @Column(name = "is_active", nullable = false, unique = false)
    private Boolean isActive;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
          
     
}
