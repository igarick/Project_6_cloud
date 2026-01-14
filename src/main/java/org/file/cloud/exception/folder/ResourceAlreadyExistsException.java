package org.file.cloud.exception.folder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.file.cloud.exception.BaseException;
import org.file.cloud.exception.ErrorInfo;

import java.util.prefs.BackingStoreException;

@Getter
@AllArgsConstructor
public class ResourceAlreadyExistsException extends RuntimeException {
   private final String errorMessage;
   private final int statusCode;


//    public ResourceAlreadyExistsException(String errorMessage, int statusCode) {
//        this.errorMessage = errorMessage;
//        this.statusCode = statusCode;
//    }
}
