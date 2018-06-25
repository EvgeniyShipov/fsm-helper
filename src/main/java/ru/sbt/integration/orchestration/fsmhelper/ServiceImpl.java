package ru.sbt.integration.orchestration.fsmhelper;

/**
 * Пример имплементации интерфейса Service.
 */
public enum ServiceImpl implements Service {

    REMOTE_API_SAMPLE("remoteApiSample", "doSomeWork", 10_000, 3);

    private final String service;
    private final String method;
    private final int timeout;
    private final int retries;

    ServiceImpl(String service, String method, int timeout, int retries) {
        this.service = service;
        this.method = method;
        this.timeout = timeout;
        this.retries = retries;
    }

    @Override
    public String service() {
        return service;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public int timeout() {
        return timeout;
    }

    @Override
    public int retries() {
        return retries;
    }
}