export function validatePassword(password) {
  const regexUppercase = /[A-Z]/;
  const regexLowercase = /[a-z]/;
  const regexDigit = /\d/;
  const regexSpecialChar = /[~!@#$%^&*()]/;
  const validationErrors = [];

  if (password.length > 0) {
    if (password.length < 8) {
      validationErrors.push("Must be a minimum of 8 characters in length");
    }
    if (!regexUppercase.test(password)) {
      validationErrors.push("Must contain at least 1 uppercase letter");
    }
    if (!regexLowercase.test(password)) {
      validationErrors.push("Must contain at least 1 lowercase letter");
    }
    if (!regexDigit.test(password)) {
      validationErrors.push("Must contain at least 1 digit");
    }
    if (!regexSpecialChar.test(password)) {
      validationErrors.push(
        "Must contain at least 1 special character ~!@#$%^&*()"
      );
    }
    if (validationErrors.length > 0) {
      return validationErrors;
    }
  }
  return null;
}

export function validateConfirmPassword(password, confirmPassword) {
  if (password !== confirmPassword && confirmPassword.length > 0) {
    return ["Passwords did not match"];
  }
  return null;
}
