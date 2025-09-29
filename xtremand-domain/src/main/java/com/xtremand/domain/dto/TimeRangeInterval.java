package com.xtremand.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TimeRangeInterval {
	  private LocalDateTime start;
	    private LocalDateTime end;
}