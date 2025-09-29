package com.xtremand.domain.enums;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import com.xtremand.domain.dto.TimeRangeInterval;

public enum TimeRange {
    LAST_WEEK,
    LAST_MONTH,
    LAST_6_MONTHS,
    LAST_YEAR,
    LAST_2_YEARS;

    public LocalDateTime getStart() {
        LocalDateTime now = LocalDateTime.now();
        switch (this) {
            case LAST_WEEK:
                return now.minusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case LAST_MONTH:
                return now.minusMonths(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case LAST_6_MONTHS:
                return now.minusMonths(6).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case LAST_YEAR:
                return now.minusYears(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case LAST_2_YEARS:
                return now.minusYears(2).withHour(0).withMinute(0).withSecond(0).withNano(0);
            default:
                throw new UnsupportedOperationException("TimeRange not supported: " + this);
        }
    }

    public LocalDateTime getEnd() {
        return LocalDateTime.now();
    }

    public TimeRangeInterval getCurrentInterval() {
        return new TimeRangeInterval(getStart(), getEnd());
    }

    public TimeRangeInterval getPreviousInterval() {
        LocalDateTime currentStart = getStart();
        LocalDateTime currentEnd = getEnd();

        long periodLengthDays = ChronoUnit.DAYS.between(currentStart, currentEnd);
        LocalDateTime prevEnd = currentStart.minusNanos(1); // one nanosecond before current start
        LocalDateTime prevStart = prevEnd.minusDays(periodLengthDays);

        // For months/years, calculate using months/years, not just days
        switch (this) {
            case LAST_WEEK:
                prevStart = currentStart.minusWeeks(1);
                prevEnd = currentStart.minusNanos(1);
                break;
            case LAST_MONTH:
                prevStart = currentStart.minusMonths(1);
                prevEnd = currentStart.minusNanos(1);
                break;
            case LAST_6_MONTHS:
                prevStart = currentStart.minusMonths(6);
                prevEnd = currentStart.minusNanos(1);
                break;
            case LAST_YEAR:
                prevStart = currentStart.minusYears(1);
                prevEnd = currentStart.minusNanos(1);
                break;
            case LAST_2_YEARS:
                prevStart = currentStart.minusYears(2);
                prevEnd = currentStart.minusNanos(1);
                break;
            default:
                throw new UnsupportedOperationException("TimeRange not supported: " + this);
        }

        return new TimeRangeInterval(prevStart, prevEnd);
    }
}
