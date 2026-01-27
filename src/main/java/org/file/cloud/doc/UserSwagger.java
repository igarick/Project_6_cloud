package org.file.cloud.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.file.cloud.dto.ErrorMessageDto;
import org.file.cloud.dto.UsernameDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Current user")
public interface UserSwagger {
    @Operation(
            summary = "Get current user info",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User info retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UsernameDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User is not authorized",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    )
            }
    )
    public ResponseEntity<UsernameDto> getUser(@AuthenticationPrincipal UserDetails userDetails);
}
