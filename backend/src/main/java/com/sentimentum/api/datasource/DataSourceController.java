package com.sentimentum.api.datasource;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-sources")
public class DataSourceController {

    private final DataSourceService service;

    public DataSourceController(DataSourceService service) {
        this.service = service;
    }

    @GetMapping
    public List<DataSourceDto> list(@RequestParam(required = false) UUID projectId) {
        return service.list(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DataSourceDto create(@Valid @RequestBody CreateDataSourceRequest request) {
        return service.create(request);
    }
}
