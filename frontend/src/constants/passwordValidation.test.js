import { validatePassword } from "./passwordValidation";

describe("passwordValidation", () => {
  const errorMessages = {
    minLength: "Must be a minimum of 8 characters in length",
    uppercase: "Must contain at least 1 uppercase letter",
    lowercase: "Must contain at least 1 lowercase letter",
    digit: "Must contain at least 1 digit",
    special: "Must contain at least 1 special character ~!@#$%^&*()",
  };

  it("returns null for empty password", () => {
    expect(validatePassword("")).toBeNull();
  });

  it("returns error for password less than 8 characters", () => {
    expect(validatePassword("Ab1!")).toContain(errorMessages.minLength);
  });

  it("returns error for missing uppercase letter", () => {
    expect(validatePassword("password1!")).toContain(errorMessages.uppercase);
  });

  it("returns error for missing lowercase letter", () => {
    expect(validatePassword("PASSWORD1!")).toContain(errorMessages.lowercase);
  });

  it("returns error for missing digit", () => {
    expect(validatePassword("Password!")).toContain(errorMessages.digit);
  });

  it("returns error for missing special character", () => {
    expect(validatePassword("Password1")).toContain(errorMessages.special);
  });

  it("returns all errors for a weak password", () => {
    const errors = validatePassword("pass");
    expect(errors).toContain(errorMessages.minLength);
    expect(errors).toContain(errorMessages.uppercase);
    expect(errors).toContain(errorMessages.digit);
    expect(errors).toContain(errorMessages.special);
  });

  it("returns null for a valid password", () => {
    expect(validatePassword("Password1!")).toBeNull();
  });
});
