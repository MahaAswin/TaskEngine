package com.taskengine.backend.dto;

import java.time.Instant;

public record NotificationEvent(String type, String message, Object data, Instant timestamp) {}
