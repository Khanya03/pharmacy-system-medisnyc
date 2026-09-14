// GlobalExceptionHandler returns either:
//   { success: false, message: "..." }               -> IllegalArgumentException, BadCredentials, etc.
//   { fieldName: "error message", ... }               -> @Valid MethodArgumentNotValidException
export function parseApiError(error) {
    if (!error.response) {
        return { message: "Can't reach the server. Is the backend running on localhost:8080?", fieldErrors: {} };
    }

    const data = error.response.data;
    const status = error.response.status;

    if (data && typeof data.message === "string") {
        return { message: data.message, fieldErrors: {} };
    }

    // Spring Boot's default error body looks like:
    // { timestamp, status, error, path } — not a field-validation map.
    // Without this check, Object.values(data)[0] below would grab the
    // "timestamp" field and display a raw ISO date as the error message.
    const isDefaultSpringError =
        data && typeof data === "object" && "timestamp" in data && "status" in data;

    if (isDefaultSpringError) {
        if (status === 403) {
            return { message: "You don't have permission to do that.", fieldErrors: {} };
        }
        if (status === 401) {
            return { message: "Your session has expired. Please log in again.", fieldErrors: {} };
        }
        return { message: data.error || "Something went wrong. Please try again.", fieldErrors: {} };
    }

    if (data && typeof data === "object") {
        // Field-validation error map
        const fieldErrors = data;
        const firstMessage = Object.values(fieldErrors)[0];
        return { message: firstMessage || "Please check the highlighted fields.", fieldErrors };
    }

    return { message: "Something went wrong. Please try again.", fieldErrors: {} };
}
