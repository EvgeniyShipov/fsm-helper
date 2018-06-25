package ru.sbt.integration.orchestration.fsmhelper;

import java.util.Map;

/**
 * Класс отвечающий за хранение системного состояния транзации.
 */
public class FSMHolder {

    public static final String FSM_HOLDER_NAME = "FSM_HOLDER";

    protected final Map<String, Object> stateData;
    protected Map<String, Object> headers;
    protected Object start;
    protected Object body;
    protected Service service;

    public FSMHolder(Map<String, Object> stateData) {
        this.stateData = stateData;
    }

    public <T> T getStart() {
        return (T) start;
    }

    public <T> T getBody() {
        return (T) body;
    }

    public Service getService() {
        return service;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public FSMHolder withStart(Object start) {
        this.start = start;
        return update();
    }

    public FSMHolder withBody(Object body) {
        this.body = body;
        return update();
    }

    public FSMHolder withService(Service service) {
        this.service = service;
        return update();
    }

    public FSMHolder withHeaders(Map<String, Object> headers) {
        this.headers = headers;
        return update();
    }

    public FSMHolder update() {
        stateData.put(FSM_HOLDER_NAME, this);
        return this;
    }
}
