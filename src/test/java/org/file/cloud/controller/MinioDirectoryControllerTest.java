package org.file.cloud.controller;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import lombok.extern.slf4j.Slf4j;
import org.file.cloud.configuration.MinioProperties;
import org.file.cloud.dto.RequestUserDto;
import org.file.cloud.dto.folder.ResponseResourceDto;
import org.file.cloud.exception.folder.ResourceException;
import org.file.cloud.repository.UserRepository;
import org.file.cloud.service.MinioDirectoryService;
import org.file.cloud.service.MinioResourceService;
import org.file.cloud.service.StorageResourceValidator;
import org.file.cloud.service.UserRootFolderManager;
import org.file.cloud.service.minio.MinioStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class MinioDirectoryControllerTest extends BaseIntegrationTest {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioProperties minioProperties;

    @Autowired
    MinioResourceService minioResourceService;

    @Autowired
    MinioStorageService minioStorageService;

    @Autowired
    MinioDirectoryService minioDirectoryService;

    @Autowired
    StorageResourceValidator storageResourceValidator;

    @Autowired
    UserRootFolderManager userRootFolderManager;

    @Autowired
    UserRepository userRepository;

    Long userId;
    String userRootFolder;

    private static final String USERNAME_2 = "user_two";
    private static final String PASSWORD_2 = "password_two";

    private static final String FIRST_FILE_NAME = "first.txt";
    private static final String SECOND_FILE_NAME = "second.txt";
    private static final String THIRD_FILE_NAME = "third.pdf";

    private static final int EXPECTED_FILES_COUNT_IN_FOLDER = 3;
    private static final int EXPECTED_FILES_COUNT_IN_FOLDER_BY_QUERY = 2;
    private static final int EXPECTED_FILES_COUNT = 1;

    private List<MultipartFile> testFile;
    protected List<MultipartFile> testFolder;

    @BeforeEach
    void setUp() throws Exception {
        createBucket(minioClient);

        RequestUserDto requestUserDto = RequestUserDto.builder()
                .username(USERNAME_1)
                .password(PASSWORD_1)
                .build();

        userService.signUp(requestUserDto);
        userService.createUserRootFolder(requestUserDto);
        userId = userRepository.findIdByUsername(USERNAME_1);
        userRootFolder = userRootFolderManager.getUserRootFolder(userId);
        testFile = createTestFile();
        testFolder = createTestFolder();
    }

    @AfterEach
    void cleanup() {
        removeBucket();
    }

    void removeBucket() {
        minioStorageService.getObjects("")
                .forEach(itemResult -> {
                    try {
                        minioStorageService.deleteFile(itemResult.get().objectName());
                    } catch (Exception e) {
                        fail("Failed to delete file in test bucket", e);
                    }
                });

        try {
            minioClient.removeBucket(
                    RemoveBucketArgs.builder()
                            .bucket(minioProperties.bucket())
                            .build());
        } catch (Exception e) {
            fail("Failed to remove test bucket", e);
        }
    }

    void createBucket(MinioClient minioClient) throws Exception {
        boolean found =
                minioClient.bucketExists(
                        BucketExistsArgs.builder()
                                .bucket(minioProperties.bucket())
                                .build());
        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(minioProperties.bucket())
                            .build());
            log.info("Created bucket = {}", minioProperties.bucket());
        }
    }

    List<MultipartFile> createTestFile() {
        List<MultipartFile> files = new ArrayList<>();
        files.add(
                new MockMultipartFile(
                        "files",
                        FIRST_FILE_NAME,
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello my friend!".getBytes()
                )
        );
        log.info("Created multipartFile = {}", FIRST_FILE_NAME);
        return files;
    }

    List<MultipartFile> createTestFolder() {
        List<MultipartFile> files = new ArrayList<>();
        files.add(
                new MockMultipartFile(
                        "files",
                        FIRST_FILE_NAME,
                        MediaType.TEXT_PLAIN_VALUE,
                        "Hello my friend!".getBytes()
                ));
        files.add(
                new MockMultipartFile(
                        "files",
                        SECOND_FILE_NAME,
                        MediaType.APPLICATION_PDF_VALUE,
                        "I want pdf!".getBytes()
                ));
        files.add(
                new MockMultipartFile(
                        "files",
                        THIRD_FILE_NAME,
                        MediaType.APPLICATION_XML_VALUE,
                        "Take it, xml!".getBytes()
                )
        );
        log.info("Created folder with files: {}, {}, {}", FIRST_FILE_NAME, SECOND_FILE_NAME, THIRD_FILE_NAME);
        return files;
    }

    @Test
    void shouldCreateOneFileInUserRootFolder() {
        String uploadPath = "";
        List<ResponseResourceDto> responseResourceDtos = minioResourceService.uploadResource(USERNAME_1, uploadPath, testFile);
        assertFileExists(uploadPath, FIRST_FILE_NAME);

        StreamingResponseBody fileStream = minioResourceService.getFileStream(USERNAME_1, FIRST_FILE_NAME);
        assertThat(fileStream).isNotNull();

        assertThatOnlyFilesPresentInFolder(userRootFolder ,uploadPath, EXPECTED_FILES_COUNT);
    }

    @Test
    void shouldDenyAccessToOtherUsersResources() {
        String uploadPath = "";
        String resourcePath = uploadPath + FIRST_FILE_NAME;
        String otherUser = USERNAME_1;
        String otherUserRootFolder = userRootFolder;
        String fullResourcePathForOtherUser = otherUserRootFolder + resourcePath;

        assertThat(minioStorageService.isFileExists(fullResourcePathForOtherUser))
                .as("File should NOT exist: " + fullResourcePathForOtherUser)
                .isFalse();

        RequestUserDto ownerDto = RequestUserDto.builder()
                .username(USERNAME_2)
                .password(PASSWORD_2)
                .build();

        userService.signUp(ownerDto);
        Long ownerId = userRepository.findIdByUsername(ownerDto.getUsername());
        userService.createUserRootFolder(ownerDto);

        String ownerRootFolder = userRootFolderManager.getUserRootFolder(ownerId);
        String fullResourcePath = ownerRootFolder + resourcePath;

        uploadTestFile(ownerDto.getUsername(), uploadPath);

        assertThat(minioStorageService.isFileExists(fullResourcePath))
                .as("File should exist: " + fullResourcePath)
                .isTrue();
        assertThatExceptionOfType(ResourceException.class).isThrownBy(() -> storageResourceValidator.ensureResourceExists(otherUser, resourcePath))
                .withMessageContaining("Resource not found");
    }

    @Test
    void shouldMoveFileFromUserRootFolderToAnotherFolder() {
        String rootDirectory = "";
        String newFolder = "gold/";

        createFolder(newFolder);
        uploadTestFile(USERNAME_1, rootDirectory);

        String resourcePathTo = newFolder + FIRST_FILE_NAME;
        ResponseResourceDto movedResourceDto = minioResourceService.moveResource(USERNAME_1, FIRST_FILE_NAME, resourcePathTo);
        assertThat(movedResourceDto.getPath()).isEqualTo(newFolder);
        assertFileExists(resourcePathTo);

        assertFileNotExists(FIRST_FILE_NAME);
    }

    @Test
    void shouldMoveFolderToAnotherFolder() {
        String folderPathFrom = "sun/";
        String folderPathTo = "gold/sold/";

        createFolder(folderPathFrom);
        createFolder(folderPathTo);

        uploadTestFolder(USERNAME_1, folderPathFrom);

        assertFileExists(folderPathFrom, FIRST_FILE_NAME);
        assertFileExists(folderPathFrom, SECOND_FILE_NAME);
        assertFileExists(folderPathFrom, THIRD_FILE_NAME);

        String newFolder = folderPathTo + folderPathFrom;
        ResponseResourceDto movedResourceDto = minioResourceService.moveResource(USERNAME_1, folderPathFrom, newFolder);

        assertFileNotExists(folderPathFrom, FIRST_FILE_NAME);
        assertFileNotExists(folderPathFrom, SECOND_FILE_NAME);
        assertFileNotExists(folderPathFrom, THIRD_FILE_NAME);

        assertThatOnlyFilesPresentInFolder(userRootFolder, newFolder, EXPECTED_FILES_COUNT_IN_FOLDER);

        assertFileExists(folderPathTo, folderPathFrom, FIRST_FILE_NAME);
        assertFileExists(folderPathTo, folderPathFrom, SECOND_FILE_NAME);
        assertFileExists(folderPathTo, folderPathFrom, THIRD_FILE_NAME);
    }

    @Test
    void shouldDeleteOneTestFile() {
        String uploadPath = "";
        String fullTestFilePath = uploadPath + FIRST_FILE_NAME;

        uploadTestFile(USERNAME_1, uploadPath);

        assertFileExists(uploadPath, FIRST_FILE_NAME);
        assertThatOnlyFilesPresentInFolder(userRootFolder, uploadPath, EXPECTED_FILES_COUNT);

        minioResourceService.deleteResource(USERNAME_1, fullTestFilePath);
        assertFileNotExists(fullTestFilePath);
    }

    @Test
    void shouldDeleteTestFolder() {
        String uploadPath = "fora/";
        createFolder(uploadPath);

        uploadTestFolder(USERNAME_1, uploadPath);
        assertFileExists(uploadPath, FIRST_FILE_NAME);
        assertFileExists(uploadPath, SECOND_FILE_NAME);
        assertFileExists(uploadPath, THIRD_FILE_NAME);

        minioResourceService.deleteResource(USERNAME_1, uploadPath);
        assertFileNotExists(uploadPath, FIRST_FILE_NAME);
        assertFileNotExists(uploadPath, SECOND_FILE_NAME);
        assertFileNotExists(uploadPath, THIRD_FILE_NAME);
    }

    @Test
    void shouldFindFilesByQuery() {
        String uploadPath = "";
        String query = "txt";

        String resourcePath = uploadPath + FIRST_FILE_NAME;
        String ownerFile = USERNAME_1;
        String ownerFileRootFolder = userRootFolder;
//        String fullResourcePathForOwnerFileUser = ownerFileRootFolder + resourcePath;

        uploadTestFile(ownerFile, uploadPath);
        assertFileExists(resourcePath);

        RequestUserDto ownerFolderDto = RequestUserDto.builder()
                .username(USERNAME_2)
                .password(PASSWORD_2)
                .build();

        userService.signUp(ownerFolderDto);
        Long ownerFolderId = userRepository.findIdByUsername(ownerFolderDto.getUsername());
        userService.createUserRootFolder(ownerFolderDto);
        String ownerFolderRootFolder = userRootFolderManager.getUserRootFolder(ownerFolderId);

        uploadTestFolder(ownerFolderDto.getUsername(), uploadPath);
        assertFileExists_NO_ROOT(ownerFolderRootFolder, uploadPath, FIRST_FILE_NAME);
        assertFileExists_NO_ROOT(ownerFolderRootFolder, uploadPath, SECOND_FILE_NAME);
        assertFileExists_NO_ROOT(ownerFolderRootFolder, uploadPath, THIRD_FILE_NAME);

        List<ResponseResourceDto> byFileQuery = minioResourceService.searchResource(ownerFile, query);
        assertThat(byFileQuery).hasSize(EXPECTED_FILES_COUNT);
        assertThat(byFileQuery.get(0).getName()).isEqualTo(FIRST_FILE_NAME);

        List<ResponseResourceDto> byFolderQuery = minioResourceService.searchResource(ownerFolderDto.getUsername(), query);
        assertThat(byFolderQuery)
                .as("Should be found files: " + EXPECTED_FILES_COUNT_IN_FOLDER_BY_QUERY)
                .hasSize(EXPECTED_FILES_COUNT_IN_FOLDER_BY_QUERY);
    }

    private void assertFileExists_NO_ROOT(String... parts) {
        String fullPath = fullPath_NO_ROOT(parts);
        assertThat(minioStorageService.isFileExists(fullPath))
                .as("File should exist: " + fullPath)
                .isTrue();
    }
    private String fullPath_NO_ROOT(String... parts) {
        return String.join("", parts);
    }


    private void assertFileExists(String... parts) {
        String fullPath = fullPath(parts);
        assertThat(minioStorageService.isFileExists(fullPath))
                .as("File should exist: " + fullPath)
                .isTrue();
    }

    private void assertFileNotExists(String... parts) {
        String fullPath = fullPath(parts);
        assertThat(minioStorageService.isFileExists(fullPath))
                .as("File should NOT exist: " + fullPath)
                .isFalse();
    }

    private void uploadTestFolder(String username, String path) {
        List<ResponseResourceDto> createdResourceDtos = minioResourceService.uploadResource(username, path, testFolder);
        assertThat(createdResourceDtos)
                .as("Folder should be uploaded to " + path)
                .hasSize(EXPECTED_FILES_COUNT_IN_FOLDER);
    }

    private void uploadTestFile(String username, String path) {
        List<ResponseResourceDto> createdResourceDtos = minioResourceService.uploadResource(username, path, testFile);
        assertThat(createdResourceDtos)
                .as("File should be uploaded to: " + path)
                .hasSize(EXPECTED_FILES_COUNT);
        assertThat(createdResourceDtos.get(0).getName()).isEqualTo(FIRST_FILE_NAME);
    }

    private void assertThatOnlyFilesPresentInFolder(String userRootFolder, String path, int filesCount) {
        String fullFolderPath = userRootFolder + path;
        List<String> result = new ArrayList<>();
        minioStorageService.getObjects(fullFolderPath, false)
                .forEach(itemResult -> {
                    try {
                        String objectName = itemResult.get().objectName();
                        log.info("Object name = {}", objectName);
                        result.add(objectName);
                    } catch (Exception e) {
                        fail("Failed to read object from MinIO", e);
                    }
                });
        List<String> files = result.stream()
                .filter(s -> !s.endsWith("/"))
                .toList();
        assertThat(files).hasSize(filesCount);
    }

    private String fullPath(String... parts) {
        return userRootFolder + String.join("", parts);
    }

    private void createFolder(String path) {
        ResponseResourceDto folderTo = minioDirectoryService.createFolder(USERNAME_1, path);
        String fullPath = fullPath(path);
        assertThat(minioStorageService.isFolderExists(fullPath))
                .as("Folder should exists: " + fullPath)
                .isTrue();
    }
}




















