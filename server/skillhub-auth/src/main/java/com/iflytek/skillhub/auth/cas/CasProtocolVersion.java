package com.iflytek.skillhub.auth.cas;

/**
 * CAS protocol version: determines the validation endpoint and response format.
 */
public enum CasProtocolVersion {
    /** CAS 2.0: /serviceValidate, XML response. */
    V2_0("2.0", "/serviceValidate", false),
    /** CAS 3.0: /p3/serviceValidate, JSON response (with format=JSON). */
    V3_0("3.0", "/p3/serviceValidate", true);

    private final String wireValue;
    private final String validatePath;
    private final boolean json;

    CasProtocolVersion(String wireValue, String validatePath, boolean json) {
        this.wireValue = wireValue;
        this.validatePath = validatePath;
        this.json = json;
    }

    public String wireValue() {
        return wireValue;
    }

    public String validatePath() {
        return validatePath;
    }

    public boolean isJson() {
        return json;
    }

    public static CasProtocolVersion from(String value) {
        if (value == null) {
            return V3_0;
        }
        return switch (value.trim()) {
            case "2.0" -> V2_0;
            case "3.0" -> V3_0;
            default -> throw new IllegalArgumentException(
                "Unsupported CAS protocol version: " + value + " (expected '2.0' or '3.0')"
            );
        };
    }
}
