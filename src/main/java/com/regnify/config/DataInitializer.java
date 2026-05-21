package com.regnify.config;

import com.regnify.model.User;
import com.regnify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.regnify.model.ValidationRules;
import com.regnify.repository.ValidationRulesRepository;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ValidationRulesRepository validationRulesRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default.admin.username}")
    private String adminUsername;

    @Value("${app.default.admin.password}")
    private String adminPassword;

    @Value("${app.default.admin.email}")
    private String adminEmail;

    @Value("${app.default.admin.first-name}")
    private String adminFirstName;

    @Value("${app.default.admin.last-name}")
    private String adminLastName;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setFirstName(adminFirstName);
            admin.setLastName(adminLastName);
            admin.setRole(User.Role.ADMIN_MODERATOR);
            admin.setStatus(User.Status.ACTIVE);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            admin.setCreatedBy("SYSTEM");
            admin.setUpdatedBy("SYSTEM");

            userRepository.save(admin);
            log.info("Default admin user created: {}", adminUsername);
        } else {
            log.info("Admin user already exists: {}", adminUsername);
        }
        
        if(validationRulesRepository.count()==0)
        {
        	readAndExportExcelToDB();
        }
    }
    
    public void readAndExportExcelToDB() {
        // Use a leading slash if the file is in the root of src/main/resources
        String fileName = "/ksa_validation_matrixUpdated.xlsx"; 

        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IllegalArgumentException("File not found: " + fileName);
            }

            // WorkbookFactory handles both .xls and .xlsx
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            List<ValidationRules> validationRules = new ArrayList<ValidationRules>(); 
            for (Row row : sheet) {
            	 if (row.getRowNum() == 0) continue; // Skip header row

            	 if(row.getCell(0).getStringCellValue().trim().length() == 0) break;
            	 
            	 ValidationRules validationRule =  new ValidationRules();
            	 
            	 validationRule.setSection(row.getCell(0).getStringCellValue());
            	 validationRule.setBusinessField(row.getCell(1).getStringCellValue());
            	 validationRule.setUBLXPath(row.getCell(2).getStringCellValue());
            	 validationRule.setKSAId(row.getCell(3).getStringCellValue());
            	 validationRule.setFlag(row.getCell(4).getStringCellValue().trim().charAt(0));
            	 validationRule.setWhenMissing(row.getCell(5).getStringCellValue());
            	 validationRule.setErrorCode(row.getCell(6).getStringCellValue());
            	 validationRule.setErrorMessageTemplate(row.getCell(7).getStringCellValue());
            	 validationRule.setIsActive(true);
            	 validationRule.setCountryCode(null);
            	 validationRule.setCreatedBy("SYSTEM");
            	 validationRule.setCreatedAt(LocalDateTime.now());
            	 validationRule.setUpdatedAt(LocalDateTime.now());
            	 validationRule.setUpdatedBy("SYSTEM");
            	 
            	 validationRules.add(validationRule);
            	 
            	 
            	 log.info(row.getCell(0).getStringCellValue());
//                for (Cell cell : row) {
//                    System.out.print(cell.toString() + "\t");
//                }
                System.out.println();
            }
            workbook.close();
        	if(!validationRules.isEmpty())
            {
        		log.info("Validation Rules Created Total : {}", validationRules.size());
        		validationRulesRepository.saveAll(validationRules);
        		 log.info("Validation Rules Created Total : {}", validationRules.size());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
