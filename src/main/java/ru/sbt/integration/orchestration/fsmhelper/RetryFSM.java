package ru.sbt.integration.orchestration.fsmhelper;

import ru.sbt.integration.orchestration.fsmcore.messages.Action;

import java.util.Map;

public abstract class RetryFSM extends ExtendedFSM {

    /**
     * Метод возвращает объект отражающий 'системное состояние'
     *
     * @return 'системное состояние'
     */
    @Override
    protected RetryFSMHolder holder() {
        RetryFSMHolder holder = context().getFromStateOrDefault(FSMHolder.FSM_HOLDER_NAME, null);
        if (holder == null) {
            holder = new RetryFSMHolder(stateData());
            holder.update();
        }
        return holder;
    }

    /**
     * Метод возвращает Action для вызова удаленного сервиса c ожиданием ответа, с учетом количества попыток обращений к сервисам
     *
     * @param service - идентификатор вызываемого сервиса
     * @param body    - объект отправляемый в запросе
     * @param headers - Map<String, Object>
     * @return действие удаленного вызова на ОИП
     */
    @Override
    protected Action call(Service service, Object body, Map<String, Object> headers) {
        Action action = super.call(service, body, headers);
        holder()
                .withRetries(service.retries());
        return action;
    }

    /**
     * Метод возвращает Action для вызова последнего вызванного сервиса.
     * Если количество попыток исчепанно, то вызывается метод retryEndAction.
     *
     * @return действие удаленного вызова на ОИП
     */
    protected Action retryCall() {
        RetryFSMHolder holder = holder();
        Service service = holder.getService();
        if (holder.canRetry()) {
            int retries = holder.decrementRetriesAndGet();
            logger.logInfo("Повторная отправка запроса, осталось {} попыток", retries);
            logger.logRetriedRequest(service, retries, service.timeout(), holder.getHeaders(), holder.getBody());
            return super.call(holder.getService(), holder.getBody(), holder.getHeaders());
        }
        return retryEndAction();
    }

    /**
     * Метод по-умолчанию возвращает FinishExecutionAction
     * Если нужна другая логика по завершению попыток необходимо перегрузить данный метод
     *
     * @return действие удаленного вызова на ОИП
     */
    protected Action retryEndAction() {
        logger.logInfo("Повторная отправка запроса невозможна, исчерпано количество попыток");
        return getContext().getActionFactory().FinishExecution();
    }
}