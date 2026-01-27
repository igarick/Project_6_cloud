package org.file.cloud.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.file.cloud.dto.ErrorMessageDto;
import org.file.cloud.dto.RequestUserDto;
import org.file.cloud.dto.UsernameDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authorization")
public interface AuthSwagger {
    @Operation(
//            tags = {"Authorization"},
            summary = "Register a new user",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User created",
                            content = @Content(schema = @Schema(implementation = UsernameDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Username already exists",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    )
            }
    )
    public ResponseEntity<UsernameDto> signUpUser(@RequestBody RequestUserDto requestUserDto,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response);

    @Operation(
//            tags = {"Authorization"},
            summary = "Login a user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login successful",
                            content = @Content(schema = @Schema(implementation = UsernameDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid username or password",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    )
            }
    )
    public ResponseEntity<UsernameDto> signInUser(@RequestBody RequestUserDto requestUserDto,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response);
    @Operation(
//            tags = {"Authorization"},
            summary = "Logout a user",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Logout successful"
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
    public void signOut();
}



















