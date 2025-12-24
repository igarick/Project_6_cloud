//package org.file.cloud.validation;
//
//import jakarta.validation.ConstraintValidator;
//import jakarta.validation.ConstraintValidatorContext;
//import org.file.cloud.dto.UserDto;
//
//public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
//
//    @Override
//    public void initialize(PasswordMatches constraintAnnotation) {
//    }
//
//    @Override
//    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
//        UserDto user = (UserDto) object;
//        return user.getPassword().equals(user.getConfirmPassword());
//    }
//}
