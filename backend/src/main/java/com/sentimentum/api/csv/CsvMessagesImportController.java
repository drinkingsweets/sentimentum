package com.sentimentum.api.csv;

import com.sentimentum.api.security.AuthenticatedUser;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/csv")
public class CsvMessagesImportController {

    private final CsvMessagesImportService service;

    public CsvMessagesImportController(CsvMessagesImportService service) {
        this.service = service;
    }

    @PostMapping("/messages/import")
    @ResponseStatus(HttpStatus.CREATED)
    public ImportCsvMessagesResponse importMessages(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam @NotNull UUID projectId,
            @RequestParam @NotNull MultipartFile file
    ) {
        return service.importMessages(projectId, file, user.getId());
    }
}
