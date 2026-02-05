package org.file.cloud.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceAlreadyExistsException extends RuntimeException {
   private final ErrorInfo errorInfo;
   private final String path;


}
