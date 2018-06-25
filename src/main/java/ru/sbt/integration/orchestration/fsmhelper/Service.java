package ru.sbt.integration.orchestration.fsmhelper;

public interface Service {

    String service();

    String method();

    int timeout();

    int retries();
}
