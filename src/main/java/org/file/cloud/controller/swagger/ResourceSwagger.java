package org.file.cloud.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.file.cloud.dto.folder.ResponseResourceDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Resource")
public interface ResourceSwagger {
    @Operation(
            summary = "Get resource information",
            description = "Retrieve information about a file or directory. Path must be URL-encoded. Directories must end with '/'"
    )

    public ResponseEntity<ResponseResourceDto> showResourceInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path);
    }
