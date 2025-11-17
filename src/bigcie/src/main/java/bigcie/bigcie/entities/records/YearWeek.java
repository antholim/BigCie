package bigcie.bigcie.entities.records;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;

public record YearWeek(int year, int week) {

    public static YearWeek from(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        int week = date.get(WeekFields.ISO.weekOfWeekBasedYear());
        int year = date.get(WeekFields.ISO.weekBasedYear());
        return new YearWeek(year, week);
    }

    public static YearWeek from(LocalDate date) {
        int week = date.get(WeekFields.ISO.weekOfWeekBasedYear());
        int year = date.get(WeekFields.ISO.weekBasedYear());
        return new YearWeek(year, week);
    }
}
