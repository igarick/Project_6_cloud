package org.file.cloud.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Tag(name = "Resource")
public interface ResourceSwagger {
    @Operation(
            summary = "Get resource information",
            description = "Retrieve information about a file or directory. Path must be URL-encoded. Directories must end with '/'",
            parameters = {
                    @Parameter(
                            name = "path",
                            description = "Full URL-encoded path to the resource",
                            required = true,
                            in = ParameterIn.QUERY
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Resource information retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ResponseResourceDto.class))
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
                            description = "Resource not found",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    )
            }
    )
    public ResponseEntity<ResponseResourceDto> getResourceInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path);


    @Operation(
            summary = "Download a resource",
            description = "The folder is downloaded as a ZIP archive of its contents. " +
                          "File as a binary stream (content-type: application/octet-stream). " +
                          "Path must be URL-encoded. Directories must end with '/'",
            parameters = {
                    @Parameter(
                            name = "path",
                            description = "Full URL-encoded path to the resource. Directories must end with '/'",
                            required = true,
                            in = ParameterIn.QUERY
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File or ZIP archive downloaded successfully",
                            content = @Content(
                                    mediaType = "application/octet-stream")
//                                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
//                                    schema = @Schema(implementation = StreamingResponseBody.class))
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
                            description = "Resource not found",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    )
            }
    )
    public ResponseEntity<StreamingResponseBody> downloadResource(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path);

    @Operation(
            summary = "Delete a resource",
            description = "Deletes a folder or file by its full path. " + "Path must be URL-encoded. Directories must end with '/'",
            parameters = {
                    @Parameter(
                            name = "path",
                            description = "Full URL-encoded path to the resource",
                            required = true,
                            in = ParameterIn.QUERY
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "File or folder deleted successfully"
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
                            description = "Resource not found",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    )
            }
    )
    public void deleteResource(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String path);

    @Operation(
            summary = "Rename or move a resource",
            description = "Renames or moves a file/folder. " + "Path must be URL-encoded. Directories must end with '/'",
            parameters = {
                    @Parameter(
                            name = "from",
                            description = "Current full path to the resource in URL-encoded format",
                            required = true,
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "to",
                            description = "New full path to the resource in URL-encoded format",
                            required = true,
                            in = ParameterIn.QUERY
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Resource moved/renamed successfully",
                            content = @Content(schema = @Schema(implementation = ResponseResourceDto.class))
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
                            description = "Resource not found",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Resource with the same name at the target path already exists",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
                    )
            }
    )
    public ResponseEntity<ResponseResourceDto> moveResource(@AuthenticationPrincipal UserDetails userDetails,
                                                            @RequestParam String from,
                                                            @RequestParam String to);

    @Operation(
            summary = "Search recourses",
            description = "Search for files and folders across the entire storage",
            parameters = {
                    @Parameter(
                            name = "query",
                            description = "Search query in URL-encoded format",
                            required = true,
                            in = ParameterIn.QUERY
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Search completed successfully",
                            content = @Content(schema = @Schema(implementation = ResponseResourceDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid or missing search query",
                            content = @Content(schema = @Schema(implementation = ErrorMessageDto.class))
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
    public ResponseEntity<List<ResponseResourceDto>> searchResource(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String query);

    @Operation(
            summary = "Upload resources",
            description = "Uploads one or more files to the specified directory path. " +
                          "If path is empty or null, files will be uploaded to the root directory."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Files uploaded successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ResponseResourceDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authorized",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "File already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorMessageDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorMessageDto.class))
            )
    })

    public ResponseEntity<List<ResponseResourceDto>> uploadResource(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(
                    description = "Files to upload",
                    required = true
            )
            @RequestPart("object") List<MultipartFile> files,
            @Parameter(
                    name = "path",
                    description = "Target directory path. (URL-encoded, must end with '/')",
                    allowEmptyValue = true
            )
            @RequestParam("path") String path);
}

































