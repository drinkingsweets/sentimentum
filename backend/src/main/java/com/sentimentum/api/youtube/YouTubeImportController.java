package com.sentimentum.api.youtube;

import com.sentimentum.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/youtube")
public class YouTubeImportController {

    private final YouTubeImportService service;

    public YouTubeImportController(YouTubeImportService service) {
        this.service = service;
    }

    @PostMapping("/comments/import")
    @ResponseStatus(HttpStatus.CREATED)
    public ImportYouTubeCommentsResponse importComments(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ImportYouTubeCommentsRequest request
    ) {
        return service.importComments(request, user.getId());
    }
}
