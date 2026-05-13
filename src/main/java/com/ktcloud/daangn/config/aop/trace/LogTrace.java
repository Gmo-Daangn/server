package com.ktcloud.daangn.config.aop.trace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogTrace {

    private static final Logger log = LoggerFactory.getLogger("com.ktcloud.daangn.trace");

    private static final String START_PREFIX = "-->";
    private static final String COMPLETE_PREFIX = "<--";
    private static final String EX_PREFIX = "<X-";

    private final ThreadLocal<TraceId> traceIdHolder = new ThreadLocal<>();

    public TraceStatus begin(String methodSignature) {
        TraceId traceId = syncTraceId();
        long startTime = System.currentTimeMillis();
        log.info("[{}] {}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()) + methodSignature);
        return new TraceStatus(traceId, startTime, methodSignature);
    }

    public void end(TraceStatus status) {
        complete(status, null);
    }

    public void exception(TraceStatus status, Throwable e) {
        if (status == null) {
            log.error("호출 추적 begin() 이전 또는 begin() 실패", e);
            return;
        }
        complete(status, e);
    }

    private void complete(TraceStatus status, Throwable e) {
        long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.getStartTime();
        TraceId traceId = status.getTraceId();

        if (e == null) {
            log.info("[{}] {} time={}ms",
                    traceId.getId(),
                    addSpace(COMPLETE_PREFIX, traceId.getLevel()) + status.getMethodSignature(),
                    resultTimeMs);
        } else {
            log.error("[{}] {} time={}ms",
                    traceId.getId(),
                    addSpace(EX_PREFIX, traceId.getLevel()) + status.getMethodSignature(),
                    resultTimeMs,
                    e);
        }

        releaseTraceId();
    }

    private TraceId syncTraceId() {
        TraceId traceId = traceIdHolder.get();
        if (traceId == null) {
            traceId = new TraceId();
        } else {
            traceId = traceId.createNextId();
        }
        traceIdHolder.set(traceId);
        return traceId;
    }

    private void releaseTraceId() {
        TraceId traceId = traceIdHolder.get();
        if (traceId == null) {
            return;
        }
        if (traceId.isFirstLevel()) {
            traceIdHolder.remove();
        } else {
            traceIdHolder.set(traceId.createPreviousId());
        }
    }

    private String addSpace(String prefix, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append((i == level - 1) ? "|" + prefix + " " : "| ");
        }
        return sb.toString();
    }
}
