package com.ktcloud.daangn.config.aop.trace;

public final class TraceStatus {

    private final TraceId traceId;
    private final long startTime;
    private final String methodSignature;

    public TraceStatus(TraceId traceId, long startTime, String methodSignature) {
        this.traceId = traceId;
        this.startTime = startTime;
        this.methodSignature = methodSignature;
    }

    public TraceId getTraceId() {
        return traceId;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getMethodSignature() {
        return methodSignature;
    }
}
