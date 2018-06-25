package ru.sbt.integration.orchestration.fsmhelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.MultilineRecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.MDC;
import ru.sbt.integration.orchestration.fsmcore.FSMContext;

import java.util.Map;

/**
 * Класс отвечающий за логирование.
 */
public class LoggerFSM {

    protected ExtendedFSM fsm;
    private Logger logger;

    /**
     * Переменная, отвечающая за логирование отправки Action и получения Event
     * В связи с добавление логирования со стороны ядра оркестровщика, данный параметр по умолчанию выключен.
     * Т.о. со стороны fsm-helper останутся только пользовательские логи.
     * Для включения - turnOnLogging(), для выключения - turnOffLogging()
     */
    private boolean isLogTurnOn = false;

    public LoggerFSM(ExtendedFSM fsm) {
        this.fsm = fsm;
    }

    protected FSMContext getContext() {
        return fsm.getContext();
    }

    /**
     * Включение логирования отправки Action и получения Event.
     * По умолчанию - false.
     */
    public void turnOnLogging() {
        isLogTurnOn = true;
    }

    /**
     * Выключение логирования отправки Action и получения Event.
     * По умолчанию - false.
     */
    public void turnOffLogging() {
        isLogTurnOn = false;
    }

    // methods for logging message with params
    public void logTrace(String msg, Object... args) {
        logTrace(LogType.MESSAGE, msg, args);
    }

    public void logDebug(String msg, Object... args) {
        logDebug(LogType.MESSAGE, msg, args);
    }

    public void logInfo(String msg, Object... args) {
        logInfo(LogType.MESSAGE, msg, args);
    }

    public void logWarn(String msg, Object... args) {
        logWarn(LogType.MESSAGE, msg, args);
    }

    public void logError(String msg, Object... args) {
        logError(LogType.MESSAGE, msg, args);
    }

    // specific method for logging request to script
    public void logIncomingRequest(Object... objects) {
        if (getLogger().isInfoEnabled() && isLogTurnOn) {
            StringBuilder format = new StringBuilder("payload:\"");
            format.append(StringUtils.repeat("{},", objects.length)).replace(format.length() - 1, format.length(), "\"");
            String[] strings = new String[objects.length];
            for (int i = 0; i < objects.length; i++)
                strings[i] = printContent(objects[i]);
            logInfo(LogType.REQUEST, format.toString(), (Object[]) strings);
        }
    }

    // specific method for logging reply from script
    public void logOutgoingReply(Map<String, Object> headers, Object reply) {
        if (getLogger().isInfoEnabled() && isLogTurnOn)
            logInfo(LogType.REPLY, "headers:\"{}\" payload:\"{}\"", printContent(headers), printContent(reply));
    }

    // specific method for logging request from script to remote service
    public void logScriptRequest(Service svc, long timeout, Map<String, Object> headers, Object request) {
        if (getLogger().isInfoEnabled() && isLogTurnOn)
            logInfo(LogType.REMOTE_CALL, "script:\"{}\" method:\"{}\" timeout:\"{}\" headers:\"{}\" payload:\"{}\"", svc.service(), svc.method(), timeout, printContent(headers), printContent(request));
    }

    // specific method for logging request from script to remote service
    public void logRemoteRequest(Service svc, long timeout, Map<String, Object> headers, Object request) {
        if (getLogger().isInfoEnabled() && isLogTurnOn)
            logInfo(LogType.REMOTE_CALL, "service:\"{}\" method:\"{}\" timeout:\"{}\" headers:\"{}\" payload:\"{}\"", svc.service(), svc.method(), timeout, printContent(headers), printContent(request));
    }

    // specific method for logging request from script to remote service
    public void logRemoteRequest(Service svc, long timeout, String moduleId, Map<String, Object> headers, Object request) {
        if (getLogger().isInfoEnabled() && isLogTurnOn)
            logInfo(LogType.REMOTE_CALL, "service:\"{}\" method:\"{}\" moduleId:\"{}\" timeout:\"{}\" headers:\"{}\" payload:\"{}\"", svc.service(), svc.method(), moduleId, timeout, printContent(headers), printContent(request));
    }

    // specific method for logging request from script to remote service
    public void logRemoteRequest(Service svc, Map<String, Object> headers, Object request) {
        if (getLogger().isInfoEnabled() && isLogTurnOn)
            logInfo(LogType.REMOTE_CALL, "service:\"{}\" method:\"{}\" timeout:\"without response\" headers:\"{}\" payload:\"{}\"", svc.service(), svc.method(), svc.timeout(), printContent(headers), printContent(request));
    }

    // specific method for logging request to remote service indicating that this request was retried after fail
    public void logRetriedRequest(Service svc, int retriesLeft, long timeout, Map<String, Object> headers, Object request) {
        if (getLogger().isInfoEnabled() && isLogTurnOn)
            logInfo(LogType.REMOTE_RETRY, "service:\"{}\" method:\"{}\" retries left:\"{}\" timeout:\"{}\" headers:\"{}\" payload:\"{}\"", svc.service(), svc.method(), retriesLeft, timeout, printContent(headers), printContent(request));
    }

    // specific method for logging reply from remote service
    void logRemoteReply(Service svc, Object response) {
        if (getLogger().isInfoEnabled() && isLogTurnOn)
            logInfo(LogType.REMOTE_REPLY, "service:\"{}\" method:\"{}\" payload:\"{}\"", svc.service(), svc.method(), printContent(response));
    }

    // add value for technical log fields
    private Object[] addCommonLogArgs(LogType type, Object[] args) {
        String state = MDC.get("state");
        Object[] fullArgs = new Object[args.length + 4];
        fullArgs[0] = getContext().getServiceName();
        fullArgs[1] = getContext().getTransactionID();
        fullArgs[2] = state;
        fullArgs[3] = type;
        System.arraycopy(args, 0, fullArgs, 4, args.length);
        return fullArgs;
    }

    // common methods for all log levels
    private void logTrace(LogType type, String msg, Object... args) {
        if (getLogger().isTraceEnabled()) {
            getLogger().trace(addCommonLogFields(msg), addCommonLogArgs(type, args));
        }
    }

    private void logDebug(LogType type, String msg, Object... args) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(addCommonLogFields(msg), addCommonLogArgs(type, args));
        }
    }

    private void logInfo(LogType type, String msg, Object... args) {
        if (getLogger().isInfoEnabled()) {
            getLogger().info(addCommonLogFields(msg), addCommonLogArgs(type, args));
        }
    }

    private void logWarn(LogType type, String msg, Object... args) {
        if (getLogger().isWarnEnabled()) {
            getLogger().warn(addCommonLogFields(msg), addCommonLogArgs(type, args));
        }
    }

    private void logError(LogType type, String msg, Object... args) {
        if (getLogger().isErrorEnabled()) {
            getLogger().error(addCommonLogFields(msg), addCommonLogArgs(type, args));
        }
    }

    /**
     * Метод записи объекта в строку
     *
     * @param object - объект, который хотим записать
     * @return строка содержащая в себе весь объект
     * @throws IllegalArgumentException если объект равен <code>null</code>
     */
    private String printContent(Object object) {
        return object == null ? "null" : ReflectionToStringBuilder.toString(object, new MultilineRecursiveToStringStyle(), true, true);
    }

    // add technical log fields common for any log message
    private String addCommonLogFields(String msg) {
        return "script:\"{}\" tid:\"{}\" state:\"{}\" type:\"{}\" " + msg;
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = getContext().getLogger();
        }
        return logger;
    }
}
