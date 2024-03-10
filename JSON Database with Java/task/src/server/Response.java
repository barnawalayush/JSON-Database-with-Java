package server;

public class Response {

    String response;
    String reason;
    String value;

    public Response() {
    }

    public Response(String response, String reason, String value) {
        this.response = response;
        this.reason = reason;
        this.value = value;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
