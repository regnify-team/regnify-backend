// src/main/java/com/regnify/controller/InvoiceController.java
package com.regnify.controller;

import com.regnify.dto.request.InvoiceFilterRequest;
import com.regnify.dto.request.InvoiceRequest;
import com.regnify.dto.response.ApiResponse;
import com.regnify.dto.response.InvoiceResponse;
import com.regnify.dto.response.ValidationResponse;
import com.regnify.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice management endpoints")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    @PostMapping("/upload")
    @Operation(summary = "Upload invoice", description = "Upload and validate a new invoice")
    @PreAuthorize("hasRole('VIEWER') or hasRole('SUPER_USER') or hasRole('ADMIN_MODERATOR')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> uploadInvoice(
            @Valid @ModelAttribute InvoiceRequest request) throws IOException {
        
        InvoiceResponse response = invoiceService.uploadInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice uploaded successfully", response));
    }
    
    @GetMapping
    @Operation(summary = "Get all invoices", description = "Get paginated list of invoices with filtering")
    @PreAuthorize("hasRole('VIEWER') or hasRole('SUPER_USER') or hasRole('ADMIN_MODERATOR')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getInvoices(
            @Parameter(description = "Filter criteria") @ModelAttribute InvoiceFilterRequest filter) {
        
        Page<InvoiceResponse> invoices = invoiceService.getInvoices(filter);
        return ResponseEntity.ok(ApiResponse.success("Invoices retrieved successfully", invoices));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID", description = "Get detailed information about a specific invoice")
    @PreAuthorize("hasRole('VIEWER') or hasRole('SUPER_USER') or hasRole('ADMIN_MODERATOR')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        InvoiceResponse invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice retrieved successfully", invoice));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update invoice", description = "Update an existing invoice")
    @PreAuthorize("hasRole('SUPER_USER') or hasRole('ADMIN_MODERATOR')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoice(
            @PathVariable Long id,
            @Valid @ModelAttribute InvoiceRequest request) {
        
        InvoiceResponse updatedInvoice = invoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Invoice updated successfully", updatedInvoice));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete invoice", description = "Soft delete an invoice")
    @PreAuthorize("hasRole('SUPER_USER') or hasRole('ADMIN_MODERATOR')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice deleted successfully", null));
    }
    
    @PostMapping("/{id}/process")
    @Operation(summary = "Process invoice", description = "Manually process an invoice")
    @PreAuthorize("hasRole('SUPER_USER') or hasRole('ADMIN_MODERATOR')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> processInvoice(@PathVariable Long id) {
        InvoiceResponse processedInvoice = invoiceService.processInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice processed successfully", processedInvoice));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search invoices", description = "Search invoices by text")
    @PreAuthorize("hasRole('VIEWER') or hasRole('SUPER_USER') or hasRole('ADMIN_MODERATOR')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> searchInvoices(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<InvoiceResponse> invoices = invoiceService.searchInvoices(query, limit);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", invoices));
    }
    
    @GetMapping("/{id}/download")
    @Operation(summary = "Download invoice file", description = "Download the attached invoice file")
    @PreAuthorize("hasRole('VIEWER') or hasRole('SUPER_USER') or hasRole('ADMIN_MODERATOR')")
    public ResponseEntity<byte[]> downloadInvoiceFile(@PathVariable Long id) throws IOException {
        byte[] fileContent = invoiceService.downloadInvoiceFile(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "invoice_" + id + ".file");
        headers.setContentLength(fileContent.length);
        
        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }
    
    
	@PostMapping(value = "/validate", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Validate Invoice XML")
	@PreAuthorize("hasRole('VIEWER') or hasRole('SUPER_USER') or hasRole('ADMIN_MODERATOR')")
	public ValidationResponse validate(@RequestBody String xml) {
		return invoiceService.validate(xml);
	}
    
    
}