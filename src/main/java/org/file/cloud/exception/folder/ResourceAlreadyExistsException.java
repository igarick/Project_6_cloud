package org.file.cloud.exception.folder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.file.cloud.exception.BaseException;
import org.file.cloud.exception.ErrorInfo;

import java.util.prefs.BackingStoreException;

@Getter
@AllArgsConstructor
public class ResourceAlreadyExistsException extends RuntimeException {
   private final ErrorInfo errorInfo;
   private final String path;


}
