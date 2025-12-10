package io.mosip.print.constant;

public enum CardStatusMessage {

    SENT("Card details sent to Perso for regId %s"),
    PROCESSING("Card details send to Perso is ongoing for regId %s"),
    FAILED("Send to Perso has failed for regId %s with remark: %s"),
    NO_DETAILS("No card details found for regId %s"),
    INTERNAL_ERROR("Internal Server Error");

    private final String template;

    CardStatusMessage(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        return String.format(template, args);
    }
}
