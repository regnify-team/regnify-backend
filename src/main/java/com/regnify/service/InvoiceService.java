// src/main/java/com/regnify/service/InvoiceService.java
package com.regnify.service;

import com.regnify.dto.request.InvoiceFilterRequest;
import com.regnify.dto.request.InvoiceRequest;
import com.regnify.dto.response.InvoiceResponse;
import com.regnify.dto.response.ValidationMessage;
import com.regnify.dto.response.ValidationResponse;
import com.regnify.model.Invoice;
import com.regnify.model.User;
import com.regnify.repository.InvoiceRepository;
import com.regnify.repository.UserRepository;
import com.regnify.validator.ValidationStatus;
import com.regnify.validator.Validator;
import com.regnify.validator.XmlUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final EmailService emailService;
    private List<Validator> validators;
    
    private static final String UPLOAD_DIR = "uploads/invoices";
    
    @Transactional
    public InvoiceResponse uploadInvoice(InvoiceRequest request) throws IOException {
        // Validate invoice number uniqueness
        if (invoiceRepository.existsByInvoiceNumber(request.getInvoiceNumber())) {
            throw new RuntimeException("Invoice number already exists");
        }
        
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Process file upload if present
        String fileName = null;
        Long fileSize = null;
        String fileContentType = null;
        String filePath = null;
        
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            MultipartFile file = request.getFile();
            
            // Validate file
            validateFile(file);
            
            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + "_" + 
                System.currentTimeMillis() + "." + fileExtension;
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Save file
            Path targetLocation = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation);
            
            fileName = originalFileName;
            fileSize = file.getSize();
            fileContentType = file.getContentType();
            filePath = targetLocation.toString();
        }
        
        // Validate invoice based on country rules
        String validationErrors = validateInvoice(request);
        Integer validationScore = calculateValidationScore(request, validationErrors);
        
        // Determine status based on validation
        Invoice.Status status = validationErrors.isEmpty() ? 
            Invoice.Status.COMPLETE : Invoice.Status.ERROR;
        
        Invoice.BusinessStatus businessStatus = validationErrors.isEmpty() ? 
            Invoice.BusinessStatus.APPROVED : Invoice.BusinessStatus.REJECTED;
        
        Invoice.ProviderResponse providerResponse = validationErrors.isEmpty() ? 
            Invoice.ProviderResponse.SUCCESS : Invoice.ProviderResponse.FAILED;
        
        // Create invoice entity
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setDocDate(request.getDocDate());
        invoice.setProDate(request.getProDate());
        invoice.setSender(request.getSender());
        invoice.setReceiver(request.getReceiver());
        invoice.setStatus(status);
        invoice.setBusinessStatus(businessStatus);
        invoice.setProviderResponse(providerResponse);
        invoice.setCountry(request.getCountry());
        invoice.setDocumentType(request.getDocumentType() != null ? 
            request.getDocumentType() : Invoice.DocumentType.INVOICE);
        invoice.setFileName(fileName);
        invoice.setFileSize(fileSize);
        invoice.setFileContentType(fileContentType);
        invoice.setFilePath(filePath);
        invoice.setValidationErrors(validationErrors);
        invoice.setValidationScore(validationScore);
        invoice.setUploadedBy(user.getUsername());
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Log the action
        auditService.logInvoiceUpload(user.getUsername(), savedInvoice.getId(), 
            savedInvoice.getInvoiceNumber(), status.name());
        
        // Send notification email
        if (validationErrors.isEmpty()) {
            emailService.sendInvoiceProcessedEmail(user.getEmail(), savedInvoice);
        } else {
            emailService.sendInvoiceValidationFailedEmail(user.getEmail(), savedInvoice, validationErrors);
        }
        
        return mapToInvoiceResponse(savedInvoice);
    }
    
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getInvoices(InvoiceFilterRequest filter) {
        Pageable pageable = PageRequest.of(
            filter.getPage(),
            filter.getSize(),
            Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy())
        );
        
        Page<Invoice> invoices = invoiceRepository.findWithFilters(
            filter.getStartDate(),
            filter.getEndDate(),
            filter.getStatus(),
            filter.getCountry(),
            filter.getDocumentType(),
            filter.getSender(),
            filter.getReceiver(),
            filter.getUploadedBy(),
            pageable
        );
        
        return invoices.map(this::mapToInvoiceResponse);
    }
    
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        
        if (invoice.getDeleted()) {
            throw new EntityNotFoundException("Invoice has been deleted");
        }
        
        return mapToInvoiceResponse(invoice);
    }
    
    @Transactional
    public InvoiceResponse updateInvoice(Long id, InvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        
        if (invoice.getDeleted()) {
            throw new RuntimeException("Cannot update deleted invoice");
        }
        
        // Update fields
        invoice.setDocDate(request.getDocDate());
        invoice.setProDate(request.getProDate());
        invoice.setSender(request.getSender());
        invoice.setReceiver(request.getReceiver());
        invoice.setCountry(request.getCountry());
        invoice.setDocumentType(request.getDocumentType());
        invoice.setUpdatedAt(LocalDateTime.now());
        
        // Re-validate
        String validationErrors = validateInvoice(request);
        Integer validationScore = calculateValidationScore(request, validationErrors);
        
        invoice.setValidationErrors(validationErrors);
        invoice.setValidationScore(validationScore);
        
        // Update status based on validation
        if (validationErrors.isEmpty()) {
            invoice.setStatus(Invoice.Status.COMPLETE);
            invoice.setBusinessStatus(Invoice.BusinessStatus.APPROVED);
            invoice.setProviderResponse(Invoice.ProviderResponse.SUCCESS);
        } else {
            invoice.setStatus(Invoice.Status.ERROR);
            invoice.setBusinessStatus(Invoice.BusinessStatus.REJECTED);
            invoice.setProviderResponse(Invoice.ProviderResponse.FAILED);
        }
        
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        // Log the update
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        auditService.logInvoiceUpdate(username, invoice.getId(), invoice.getInvoiceNumber());
        
        return mapToInvoiceResponse(updatedInvoice);
    }
    
    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        invoice.setDeleted(true);
        invoice.setDeletedAt(LocalDateTime.now());
        invoice.setDeletedBy(username);
        invoiceRepository.save(invoice);
        
        auditService.logInvoiceDelete(username, invoice.getId(), invoice.getInvoiceNumber());
    }
    
    @Transactional
    public InvoiceResponse processInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        
        if (invoice.getDeleted()) {
            throw new RuntimeException("Cannot process deleted invoice");
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        invoice.setStatus(Invoice.Status.COMPLETE);
        invoice.setBusinessStatus(Invoice.BusinessStatus.APPROVED);
        invoice.setProviderResponse(Invoice.ProviderResponse.SUCCESS);
        invoice.setProcessedBy(username);
        invoice.setProcessedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        
        Invoice processedInvoice = invoiceRepository.save(invoice);
        
        auditService.logInvoiceProcess(username, invoice.getId(), invoice.getInvoiceNumber());
        
        return mapToInvoiceResponse(processedInvoice);
    }
    
    @Transactional(readOnly = true)
    public List<InvoiceResponse> searchInvoices(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Invoice> invoices = invoiceRepository.searchInvoices(query, pageable);
        return invoices.getContent().stream()
            .map(this::mapToInvoiceResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public byte[] downloadInvoiceFile(Long id) throws IOException {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        
        if (invoice.getFilePath() == null) {
            throw new RuntimeException("No file attached to this invoice");
        }
        
        Path filePath = Paths.get(invoice.getFilePath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found");
        }
        
        return Files.readAllBytes(filePath);
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        if (file.getSize() > 150 * 1024 * 1024) { // 150MB
            throw new RuntimeException("File size exceeds 150MB limit");
        }
        
        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName).toLowerCase();
        
        List<String> allowedExtensions = List.of("xml", "json", "csv", "xls", "xlsx", "pdf");
        if (!allowedExtensions.contains(fileExtension)) {
            throw new RuntimeException("Invalid file format. Allowed formats: " + allowedExtensions);
        }
    }
    
    private String validateInvoice(InvoiceRequest request) {
        StringBuilder errors = new StringBuilder();
        
        // Basic validation
        if (request.getDocDate().isAfter(LocalDate.now())) {
            errors.append("Document date cannot be in the future; ");
        }
        
        if (request.getProDate().isBefore(request.getDocDate())) {
            errors.append("Processing date cannot be before document date; ");
        }
        
        if (request.getSender() == null || request.getSender().trim().isEmpty()) {
            errors.append("Sender is required; ");
        }
        
        if (request.getReceiver() == null || request.getReceiver().trim().isEmpty()) {
            errors.append("Receiver is required; ");
        }
        
        // Country-specific validations
        if (request.getCountry() != null) {
            switch (request.getCountry()) {
                case GERMANY:
                    if (!request.getSender().matches(".*GmbH$|.*AG$|.*KG$")) {
                        errors.append("German companies must end with GmbH, AG or KG; ");
                    }
                    if (!request.getInvoiceNumber().matches("\\d{10}")) {
                        errors.append("German invoices must have 10-digit invoice number; ");
                    }
                    break;
                    
                case FRANCE:
                    if (!request.getInvoiceNumber().matches("FR\\d+")) {
                        errors.append("French invoices must start with 'FR' followed by numbers; ");
                    }
                    break;
                    
                case UK:
                    if (!request.getInvoiceNumber().matches("UK\\d{8}")) {
                        errors.append("UK invoices must start with 'UK' followed by 8 digits; ");
                    }
                    break;
                    
                case SPAIN:
                    if (!request.getInvoiceNumber().matches("ES\\d{8}[A-Z]")) {
                        errors.append("Spanish invoices must follow format ES12345678X; ");
                    }
                    break;
                    
                case ITALY:
                    if (!request.getInvoiceNumber().matches("IT\\d{11}")) {
                        errors.append("Italian invoices must start with 'IT' followed by 11 digits; ");
                    }
                    break;
            }
        }
        
        return errors.toString().trim();
    }
    
    private Integer calculateValidationScore(InvoiceRequest request, String validationErrors) {
        int score = 100;
        
        if (validationErrors.contains("Document date cannot be in the future")) score -= 20;
        if (validationErrors.contains("Processing date cannot be before document date")) score -= 20;
        if (validationErrors.contains("Sender is required")) score -= 15;
        if (validationErrors.contains("Receiver is required")) score -= 15;
        
        // Country-specific score deductions
        if (request.getCountry() != null) {
            switch (request.getCountry()) {
                case GERMANY:
                    if (validationErrors.contains("German companies must end")) score -= 10;
                    if (validationErrors.contains("German invoices must have")) score -= 10;
                    break;
                case FRANCE:
                    if (validationErrors.contains("French invoices must start")) score -= 10;
                    break;
                case UK:
                    if (validationErrors.contains("UK invoices must start")) score -= 10;
                    break;
                case SPAIN:
                    if (validationErrors.contains("Spanish invoices must follow")) score -= 10;
                    break;
                case ITALY:
                    if (validationErrors.contains("Italian invoices must start")) score -= 10;
                    break;
            }
        }
        
        return Math.max(score, 0);
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }
    
    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        return new InvoiceResponse(
            invoice.getId(),
            invoice.getInvoiceNumber(),
            invoice.getDocDate(),
            invoice.getProDate(),
            invoice.getSender(),
            invoice.getReceiver(),
            invoice.getStatus(),
            invoice.getBusinessStatus(),
            invoice.getProviderResponse(),
            invoice.getCountry(),
            invoice.getDocumentType(),
            invoice.getFileName(),
            invoice.getFileSize(),
            invoice.getUploadedBy(),
            invoice.getProcessedBy(),
            invoice.getValidationErrors(),
            invoice.getValidationScore(),
            invoice.getProcessedAt(),
            invoice.getCreatedAt(),
            invoice.getUpdatedAt()
        );
    }

	public ValidationResponse validate(String xml) {
		validators = List.of(
                new com.regnify.validator.StructureValidator(),
                new com.regnify.validator.MandateValidator(),
                new com.regnify.validator.FormatValidator(),
                new com.regnify.validator.DomainValidator(),
                new com.regnify.validator.CardinalityValidator(),
                new com.regnify.validator.BusinessRuleValidator()
        );
		
		  List<ValidationMessage> messages = new ArrayList<>();

	        try {
	            Document doc = XmlUtil.parse(xml);

	            for (Validator v : validators) {
	                v.validate(doc, messages);
	            }

	        } catch (Exception e) {
	            messages.add(new ValidationMessage(
	                    "STRUCTURE_ERROR",
	                    "STRUCTURAL",
	                    "Invalid XML",
	                    "ERROR",
	                    "Invoice"
	            ));
	        }

	        return new ValidationResponse(determineStatus(messages), messages);
	}
	
	private String determineStatus(List<ValidationMessage> messages) {
        return messages.stream().anyMatch(m -> m.getStatus().equals("ERROR"))
                ? ValidationStatus.ERROR.name()
                : ValidationStatus.PASS.name();
    }
}