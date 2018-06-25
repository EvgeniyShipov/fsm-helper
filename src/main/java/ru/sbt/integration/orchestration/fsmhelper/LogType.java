package ru.sbt.integration.orchestration.fsmhelper;

public enum LogType {
    REQUEST("request"),
    REPLY("reply"),
    REMOTE_CALL("remote call"),
    REMOTE_RETRY("retries remote call"),
    REMOTE_REPLY("remote reply"),
    MESSAGE("message"),
    EXCEPTION("exception"),
    RESULT("result");

    private final String value;

    LogType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}