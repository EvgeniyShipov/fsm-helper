package ru.sbt.integration.orchestration.fsmhelper;

import java.util.Map;

/**
 * Класс отвечающий за хранение системного состояния транзации, учитывающий количество попыток обращений к сервисам
 */
public class RetryFSMHolder extends FSMHolder {

    protected Integer retries = -1;

    public RetryFSMHolder(Map<String, Object> stateData) {
        super(stateData);
    }

    public RetryFSMHolder withRetries(Integer retries) {
        this.retries = retries;
        return update();
    }

    public int getRetries() {
        return retries;
    }

    public boolean canRetry() {
        return retries > 0;
    }

    public int decrementRetriesAndGet() {
        return --retries;
    }

    @Override
    public RetryFSMHolder update() {
        stateData.put(FSM_HOLDER_NAME, this);
        return this;
    }
}
