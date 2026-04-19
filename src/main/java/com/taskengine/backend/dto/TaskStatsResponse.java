package com.taskengine.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskStatsResponse {

  private final long total;
  private final long todo;
  private final long inProgress;
  private final long inReview;
  private final long done;
}
