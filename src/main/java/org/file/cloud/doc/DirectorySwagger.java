package org.file.cloud.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.file.cloud.dto.ErrorMessageDto;
import org.file.cloud.dto.folder.ResponseResourceDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Directory")
public interface DirectorySwagger {
    @Operation(
            summary = "List folder contents",
            description = "Returns contents of the specified folder (non-recursive). " +
                          "If path is empty, returns the contents of the root folder"
    )
    @Parameter(
            name = "path",
            description = "Path to the target folder",
            required = true,
            allowEmptyValue = true,
            in = ParameterIn.QUERY
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Folder contents retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ResponseResourceDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or empty path",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authorized",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Folder not found",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
            )
    }
    )

    public ResponseEntity<List<ResponseResourceDto>> getFolderContents(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String path);


    @Operation(
            summary = "Create a new folder",
            description = "Creates a new empty folder at the specified path"
    )
    @Parameter(
            name = "path",
            description = "Directory path where a new folder will be created",
            required = true,
            in = ParameterIn.QUERY
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Folder created successfully",
                    content = @Content(schema = @Schema(implementation = ResponseResourceDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or empty path to the new folder",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authorized",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Parent folder does not exist",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Folder with the same name already exists",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
            )
    })

    public ResponseEntity<ResponseResourceDto> createFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String path) throws Exception;
    }