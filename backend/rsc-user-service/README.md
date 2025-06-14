# RSC User Service API Documentation

## API Endpoints

### User Controller

#### Register User
- **POST** `/api/user/register`
    - Registers a new user
    - Request body: Register details
    - Does not requires authentication
    - Results:
        - **Status 200: OK**
            - Returns: User registered!
        - **Status 400: Bad Request**
            - Invalid email format
            - Invalid password format
        - **Status 409: Conflict**
            - User already exists

#### User Login
- **POST** `/api/user/login`
    - Logins a user
    - Request body: Login details
    - Requires authentication
    - Results:
        - **Status 200: OK**
            - Returns:
                "message": "Login successful",
                "token": token,
                "userInfo":
                    "userName": userName
        - **Status 400: Bad Request**
            - Invalid username or password

#### Validate JWT Token
- **POST** `/api/user/auth`
    - Validates a JWT token
    - Request body: Login details
    - Requires authentication
    - Results:
        - **Status 200: OK**
            - Returns:
                "message": "Valid token",
                "username": username
        - **Status 401: Unauthorized**
            - Invalid or expired token

#### Get User Details
- **GET** `/api/user/{userName}`
    - Retrieves specific user details
    - Path parameter: userName
    - Requires authentication
    - Results:
        - **Status 200: Success**
            - Returns: JSON of user details
        - **Status 404: Not Found**
            - User details not found

#### Request Reset Password Link
- **POST** `/api/user/forgot-password`
    - Requests for a reset password link
    - Request body: Reset Password details
    - Does not requires authentication
    - Results:
        - **Status 200: OK**
            - Returns: Reset password email is sent
        - **Status 400: Bad Request**
            - Invalid email format

#### Update User Password
- **POST** `/api/user/reset-password`
    - Updates user new password
    - Request body: Reset Password details
    - Does not requires authentication
    - Results:
        - **Status 200: OK**
            - Returns: Password has been reset successfully
        - **Status 400: Bad Request**
            - Invalid url or token expired

## Authentication
Endpoints require JWT authentication token stored in cookie.

## Error Responses
- **Status 400: Bad Request**
- **Status 401: Unauthorized**
- **Status 404: Not Found**
- **Status 409: Conflict**

## Database
The service uses DynamoDB with the following schema:

### User Table
- **Partition Key**: id (String)
- **Sort Key**: userName (String)

#### Attributes
- id (String)
- email (String)
- hashedPassword (String)
- userName (String)

### Token Table
- **Partition Key**: email (String)
- **Sort Key**: expiresAt (Number)

#### Attributes
- email (String)
- token (String)
- expiresAt (Number)