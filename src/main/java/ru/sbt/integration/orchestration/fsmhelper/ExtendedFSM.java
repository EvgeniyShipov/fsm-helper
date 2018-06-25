package ru.sbt.integration.orchestration.fsmhelper;

import ru.sbt.integration.orchestration.fsmcore.FSM;
import ru.sbt.integration.orchestration.fsmcore.event.Event;
import ru.sbt.integration.orchestration.fsmcore.event.EventResponseReceived;
import ru.sbt.integration.orchestration.fsmcore.event.EventStartTransaction;
import ru.sbt.integration.orchestration.fsmcore.messages.Action;
import ru.sbt.integration.orchestration.fsmcore.messages.ActionFactory.RemoteCallArgs;
import ru.sbt.integration.orchestration.fsmcore.messages.ActionFactory.ScriptCallArgs;
import ru.sbt.integration.orchestration.fsmcore.messages.SimpleMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public abstract class ExtendedFSM extends FSM {

    /**
     * Ключ, по которому в MDC хранится название сценария
     */
    protected final static String SCRIPT_NAME = "script-name";

    /**
     * Ключ, по которому в MDC хранится текущий шаг (состояние) сценария
     */
    protected final static String STATE = "state";

    protected final LoggerFSM logger = new LoggerFSM(this);

    public LoggerFSM getLogger() {
        return logger;
    }

    /**
     * Метод возвращает Action для вызова удаленного сервиса c ожиданием ответа
     *
     * @param service - идентификатор вызываемого сервиса
     * @param body    - объект отправляемый в запросе
     * @return действие удаленного вызова на ОИП
     */
    protected Action call(Service service, Object body) {
        return call(service, body, (Map<String, Object>) null);
    }

    /**
     * Метод возвращает Action для вызова удаленного сервиса c ожиданием ответа
     *
     * @param service  - идентификатор вызываемого сервиса
     * @param body     - объект отправляемый в запросе
     * @param moduleId - идентификатор модуля, на который будет отправлено сообщение. Если значение пустое или null, то
     *                 запрос будет отправлен через ММТ без каких-либо ограничений на moduleId. (Значение, указанное в
     *                 ConfigurationFactory#declareMmtRemoteApi будет игнорироваться)
     * @return действие удаленного вызова на ОИП
     */
    protected Action call(Service service, Object body, String moduleId) {
        return call(service, body, null, moduleId);
    }

    /**
     * Метод возвращает Action для вызова удаленного сервиса c ожиданием ответа
     * Перегруженный метод, принимает дополнительно &amp;Map<String, Object> headers&amp;, со своими заголовками
     *
     * @param service - идентификатор вызываемого сервиса
     * @param body    - объект отправляемый в запросе
     * @param headers - Map<String, Object>
     * @return действие удаленного вызова на ОИП
     */
    protected Action call(Service service, Object body, Map<String, Object> headers) {
        SimpleMessage message = createNewMessage(service, body, headers);
        logger.logRemoteRequest(service, service.timeout(), headers, body);
        return getContext().getActionFactory().RemoteCall(service.service(), message, service.timeout());
    }

    /**
     * Метод возвращает Action для вызова удаленного сервиса c ожиданием ответа
     * Перегруженный метод, принимает дополнительно &amp;Map<String, Object> headers&amp;, со своими заголовками
     * и moduleId.
     *
     * @param service  - идентификатор вызываемого сервиса
     * @param body     - объект отправляемый в запросе
     * @param headers  - Map<String, Object>
     * @param moduleId - идентификатор модуля, на который будет отправлено сообщение. Если значение пустое или null, то
     *                 запрос будет отправлен через ММТ без каких-либо ограничений на moduleId. (Значение, указанное в
     *                 ConfigurationFactory#declareMmtRemoteApi будет игнорироваться)
     * @return действие удаленного вызова на ОИП
     */
    protected Action call(Service service, Object body, Map<String, Object> headers, String moduleId) {
        SimpleMessage message = createNewMessage(service, body, headers);
        logger.logRemoteRequest(service, service.timeout(), moduleId, headers, body);
        return getContext().getActionFactory().RemoteCall(service.service(), message, service.timeout(), moduleId);
    }

    /**
     * Метод возвращает Action для вызова кастомного метода
     *
     * @param eventName - название кастомного метода
     * @return действие вызова кастомного метода сценария
     */
    protected Action raiseEvent(String eventName) {
        return getContext().getActionFactory().RaiseEvent(eventName);
    }

    /**
     * Метод возвращает Action для ожидания следующего ответа. Используется при параллельном вызове.
     *
     * @param timeout - таймаут в мс, в течении которого будет ожидаться ответ
     * @return действие ожидания ответа.
     */
    protected Action waitAction(long timeout) {
        return getContext().getActionFactory().Wait(timeout);
    }

    /**
     * Метод возвращает Action для вызова удаленного сервиса без ожидания ответа
     *
     * @param service - идентификатор вызываемого сервиса
     * @param body    - объект отправляемый в запросе
     * @return действие удаленного вызова на ОИП
     */
    protected Action callNoResponse(Service service, Object body) {
        return callNoResponse(service, body, null);
    }

    /**
     * Метод возвращает Action для вызова удаленного сервиса без ожидания ответа
     * Перегруженный метод, принимает дополнительно Map<String, Object> headers, со своими заголовками
     *
     * @param service - идентификатор вызываемого сервиса
     * @param body    - объект отправляемый в запросе
     * @param headers - Map<String, Object>
     * @return действие удаленного вызова на ОИП
     */
    protected Action callNoResponse(Service service, Object body, Map<String, Object> headers) {
        SimpleMessage message = createNewMessage(service, body, headers);
        logger.logRemoteRequest(service, headers, body);
        return getContext().getActionFactory().RemoteCallNoResponse(service.service(), message);
    }

    /**
     * Метод возвращает Action для отправки ответа удаленному сервису, инициирующему запуск сценария
     *
     * @param body - объект отправляемый в запросе
     * @return действие удаленного вызова на ОИП
     */
    protected Action reply(Object body) {
        return reply(body, null);
    }

    /**
     * Метод возвращает Action для отправки ответа удаленному сервису, инициирующему запуск сценария
     * Перегруженный метод, принимает дополнительно Map<String, Object> headers, со своими заголовками
     *
     * @param body    - объект отправляемый в запросе
     * @param headers - Map<String, Object>
     * @return действие удаленного вызова на ОИП
     */
    protected Action reply(Object body, Map<String, Object> headers) {
        SimpleMessage message = createNewMessage(null, body, headers);
        logger.logOutgoingReply(headers, body);
        return getContext().getActionFactory().Reply(message);
    }

    /**
     * Метод возвращает Action для паралельного вызова удаленных сервисов c ожиданием ответа
     *
     * @param service - идентификатор вызываемых сервисов
     * @param body    - объекты отправляемые в запросе
     * @return действие удаленного вызова на ОИП
     */
    protected Action parallelCall(List<Service> service, List<Object> body) {
        return parallelCall(service, body, null);
    }

    /**
     * Метод возвращает Action для паралельного вызова удаленных сервисов c ожиданием ответа
     * Перегруженный метод, принимает дополнительно List<Map<String, Object>> headers, со своими заголовками
     *
     * @param service - List<Service> список идентификаторов вызываемых сервисов
     * @param body    - List<Object> список объектов отправляемых в запросе
     * @param headers - List<Map<String, Object>> список Map с заголовками
     * @return действие удаленного вызова на ОИП
     */
    protected Action parallelCall(List<Service> service, List<Object> body, List<Map<String, Object>> headers) {
        RemoteCallArgs[] remoteCallArgs = new RemoteCallArgs[service.size()];

        if (service.size() != body.size() || (headers != null && headers.size() != body.size()))
            return getContext().getActionFactory().ErrorFinishExecution(new IllegalArgumentException("parallel call false parameters"));

        for (int i = 0; i < body.size(); i++) {
            Map<String, Object> curHeader = headers != null ? headers.get(i) : null;
            SimpleMessage message = createNewMessage(service.get(i), body.get(i), curHeader);
            logger.logRemoteRequest(service.get(i), service.get(i).timeout(), curHeader, body.get(i));
            remoteCallArgs[i] = new RemoteCallArgs(service.get(i).service(), message, service.get(i).timeout());
        }
        return getContext().getActionFactory().ParallelCall(remoteCallArgs);
    }

    /**
     * Метод возвращает Action для паралельного вызова подсценариев c ожиданием ответа
     *
     * @param service - идентификатор вызываемых сервисов
     * @param body    - объекты отправляемые в запросе
     * @return действие удаленного вызова на ОИП
     */
    protected Action parallelScriptCall(List<Service> service, List<Object> body) {
        return parallelScriptCall(service, body, null);
    }

    /**
     * Метод возвращает Action для паралельного вызова подсценариев c ожиданием ответа
     * Перегруженный метод, принимает дополнительно List<Map<String, Object>> headers, со своими заголовками
     *
     * @param service - List<Service> список идентификаторов вызываемых подсценариев
     * @param body    - List<Object> список объектов отправляемых в запросе
     * @param headers - List<Map<String, Object>> список Map с заголовками
     * @return действие удаленного вызова на ОИП
     */
    protected Action parallelScriptCall(List<Service> service, List<Object> body, List<Map<String, Object>> headers) {
        ScriptCallArgs[] scriptCallArgs = new ScriptCallArgs[service.size()];

        if (service.size() != body.size() || (headers != null && headers.size() != body.size()))
            return getContext().getActionFactory().ErrorFinishExecution(new IllegalArgumentException("parallel script call false parameters"));

        for (int i = 0; i < body.size(); i++) {
            Map<String, Object> curHeader = headers != null ? headers.get(i) : null;
            SimpleMessage message = createNewMessage(service.get(i), body.get(i), curHeader);
            logger.logRemoteRequest(service.get(i), service.get(i).timeout(), curHeader, body.get(i));
            scriptCallArgs[i] = new ScriptCallArgs(service.get(i).service(), message, service.get(i).timeout());
        }
        return getContext().getActionFactory().ParallelCall(scriptCallArgs);
    }

    /**
     * Метод возвращает Action для вызова подсценария, с ожиданием ответа
     *
     * @param service - идентификатор вызываемого сценария, должен быть объектов класса ServiceImpl
     * @param body    - объект отправляемый в запросе
     * @return действие вызова подценария на ОИП
     */
    protected Action scriptCall(Service service, Object body) {
        return scriptCall(service, body, null);
    }

    /**
     * Метод возвращает Action для вызова подсценария, с ожиданием ответа
     * Перегруженный метод, принимает дополнительно Map<String, Object> headers, со своими заголовками
     *
     * @param service - идентификатор вызываемого сценария, должен быть объектов класса ServiceImpl
     * @param body    - объект отправляемый в запросе
     * @param headers - Map<String, Object>
     * @return действие вызова подценария на ОИП
     */
    protected Action scriptCall(Service service, Object body, Map<String, Object> headers) {
        SimpleMessage message = createNewMessage(service, body, headers);
        logger.logScriptRequest(service, service.timeout(), headers, body);
        return getContext().getActionFactory().ScriptCall(service.service(), message, service.timeout());
    }

    /**
     * Заканчивает исполнение сценария и фиксирует текущую транзакцию (если есть)
     *
     * @return действие завершения сценария
     */
    protected Action end() {
        stateData().clear();
        return getContext().getActionFactory().FinishExecution();
    }

    /**
     * Метод возвращает объект из входящего события.
     *
     * @param event - входящее сообщение
     * @return объект из входящего сообщения или null
     */
    @SuppressWarnings("unchecked")
    protected <T> ValueOrError<T, String> getInput(Event event, Class<? extends T> clazz) {
        Optional<Object> value = getMessageOptional(event).map(SimpleMessage::getBody);

        if (!value.isPresent())
            return ValueOrError.error("Incoming arguments is null");
        if (!clazz.isAssignableFrom(value.get().getClass()))
            return ValueOrError.error("Incoming arguments class is incorrect: expected " + clazz.getName() + ", actual " + value.get().getClass());
        else
            try {
                ValueOrError<T, String> body = ValueOrError.value((T) value.get());
                logger.logIncomingRequest(body);
                return body;
            } catch (ClassCastException e) {
                return ValueOrError.error(e.getMessage());
            }
    }

    /**
     * Метод возвращает сообщение обернутое в Optional, из входящего события.
     *
     * @param event - входящее сообщение
     * @return Optional<SimpleMessage> или Optional.empty()
     */
    private Optional<SimpleMessage> getMessageOptional(Event event) {
        if (event instanceof EventStartTransaction) {
            Object body = Optional.ofNullable(event)
                    .map(p -> (EventStartTransaction) p)
                    .map(EventStartTransaction::getMessage)
                    .map(SimpleMessage::getBody)
                    .orElse(null);
            holder().withStart(body);
            return Optional.ofNullable((EventStartTransaction) event)
                    .map(EventStartTransaction::getMessage);
        } else if (event instanceof EventResponseReceived) {
            return Optional.ofNullable((EventResponseReceived) event)
                    .map(EventResponseReceived::getMessage);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Получает объект из входящего события. Без проверок, без java 8, для старичков.
     *
     * @param event - входящее сообщение
     * @return T body
     */
    @SuppressWarnings("unchecked")
    protected <T> T getBody(Event event) {
        T body;
        if (event instanceof EventResponseReceived) {
            EventResponseReceived eventResponseReceived = (EventResponseReceived) event;
            body = (T) eventResponseReceived.getMessage().getBody();
        } else if (event instanceof EventStartTransaction) {
            EventStartTransaction eventResponseReceived = (EventStartTransaction) event;
            body = (T) eventResponseReceived.getMessage().getBody();
            holder().withStart(body);
        } else {
            body = null;
        }
        logger.logIncomingRequest(body);
        return body;
    }

    /**
     * Метод возвращает ExtendedFSM
     *
     * @return ExtendedFSM
     */
    public ExtendedFSM context() {
        return this;
    }

    /**
     * Сохраняет объект в локальном контексте сценария
     *
     * @param key   - ключ, по которому добавляем значение в кеш
     * @param value - объект, который добавляем в кеш
     * @return ExtendedFSM
     */
    protected <T> ExtendedFSM putToState(String key, T value) {
        stateData().put(key, value);
        return context();
    }

    /**
     * Сохраняет объект в глобальном контексте сценария
     *
     * @param key             - ключ, по которому добавляем значение в кеш
     * @param value           - объект, который добавляем в кеш
     * @param storageDuration - ttl for key\value in global context
     * @return ExtendedFSM
     */
    protected <T> ExtendedFSM putToGlobal(String key, T value, long storageDuration) {
        getContext().putToGlobalContext(key, value, storageDuration);
        return context();
    }

    /**
     * Получает объект из глобального кеша.
     * Если запрашиваемого ключа нет в кеше - возвращает null
     *
     * @param key - ключ, по которому достаем значение из кеша
     * @return Объект по ключу, <code>null</code>, если ключ не найден
     */
    protected Object getFromGlobal(String key) {
        return getContext().getFromGlobalContext(key);
    }

    /**
     * Получает объект из глобального кеша.
     * Если запрашиваемого ключа нет в кеше - возвращает value
     *
     * @param key   - ключ, по которому достаем значение из кеша
     * @param value - объект, вернется, если в кеше пусто
     * @return Объект по ключу, <code>null</code>, если ключ не найден
     */
    protected <T> T getFromGlobalOrDefault(String key, T value) {
        if (getContext().getFromGlobalContext(key) != null) {
            try {
                return (T) getContext().getFromGlobalContext(key);
            } catch (ClassCastException e) {
                return value;
            }
        }
        return value;
    }

    /**
     * Получает объект из локального кеша.
     * Если запрашиваемого ключа нет в кеше или он другого типа - возвращает value
     *
     * @param key   - ключ, по которому достаем значение из кеша
     * @param value - объект, вернется, если в кеше пусто
     * @return Объект по ключу, или value, если ключ не найден
     */
    protected <T> T getFromStateOrDefault(String key, T value) {
        if (stateData().get(key) != null)
            try {
                return (T) stateData().get(key);
            } catch (ClassCastException e) {
                return value;
            }
        return value;
    }

    /**
     * Получает объект из локального кеша, и оборачивает в ValueOrError
     *
     * @param key - ключ, по которому достаем значение из кеша
     * @return ValueOrError, хранящий value в случае успеха или error в случае неудачи
     */
    protected <T> ValueOrError<T, String> safelyGetFromState(String key) {
        if (getFromStateOrDefault(key, null) == null)
            return ValueOrError.error("Object is null");

        Object obj = stateData().get(key);
        try {
            T result = (T) obj;
            return ValueOrError.value(result);
        } catch (ClassCastException e) {
            return ValueOrError.error(e.getMessage());
        }
    }

    /**
     * Метод возвращает сообщение, необходимое для вызова удаленного сервиса.
     *
     * @param service - идентификатор вызываемого сервиса
     * @param body    - объект отправляемый в запросе
     * @param headers - Map<String, Object> headers
     * @return сообщение
     */
    protected SimpleMessage createNewMessage(Service service, Object body, Map<String, Object> headers) {
        Map<String, Object> curHeaders = headers;
        if (curHeaders == null) {
            curHeaders = new HashMap<>();
        }
        holder()
                .withService(service)
                .withBody(body)
                .withHeaders(curHeaders);
        return getContext().getMessageFactory().createSimpleMessage(curHeaders, body);
    }

    /**
     * Метод возвращает объект отражающий 'системное состояние'
     *
     * @return 'системное состояние'
     */
    protected FSMHolder holder() {
        FSMHolder holder = context().getFromStateOrDefault(FSMHolder.FSM_HOLDER_NAME, null);
        if (holder == null) {
            holder = new FSMHolder(stateData());
            holder.update();
        }
        return holder;
    }

    /**
     * Метод возвращает Map<String, Object>, хранящую состояния текущей транзации, данного сценария.
     *
     * @return Map<String, Object>
     */
    protected Map<String, Object> stateData() {
        return getContext().getStateData();
    }

    /**
     * См. метод scriptCall(Service service, Object body), в котором убран параметр таймаута, он достается из сервиса.
     *
     * @param service - идентификатор вызываемых сервисов
     * @param body    - объект отправляемый в запросе
     * @param timeout - таймаут в мс
     * @return действие вызова подценария на ОИП
     */
    @Deprecated
    protected Action scriptCall(Service service, Object body, long timeout) {
        return scriptCall(service, body, timeout, null);
    }

    /**
     * См. метод scriptCall(Service service, Object body, Map<String, Object> headers), в котором убран параметр таймаута, он достается из сервиса.
     *
     * @param service - идентификатор вызываемых сервисов
     * @param body    - объект отправляемый в запросе
     * @param timeout - таймаут в мс
     * @param headers - Map<String, Object>
     * @return действие вызова подценария на ОИП
     */
    @Deprecated
    protected Action scriptCall(Service service, Object body, long timeout, Map<String, Object> headers) {
        SimpleMessage message = createNewMessage(null, body, headers);
        logger.logScriptRequest(service, timeout, headers, body);
        return getContext().getActionFactory().ScriptCall(service.service(), message, timeout);
    }
}