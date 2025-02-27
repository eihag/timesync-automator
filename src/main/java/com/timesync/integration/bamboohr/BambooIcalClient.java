package com.timesync.integration.bamboohr;

import com.timesync.DateUtil;
import com.timesync.integration.bamboohr.model.CompanyHolidaySimpleDto;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


@org.springframework.stereotype.Component
public class BambooIcalClient {

    @Value("${bamboohr_ical_url}")
    private String bambooIcalUrl;


    public List<CompanyHolidaySimpleDto> getCompanyHolidays() {
        try {
            URL url = new URI(bambooIcalUrl).toURL();
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(inputStream);

            return calendar.getComponents(Component.VEVENT).stream()
                    .map(i -> new CompanyHolidaySimpleDto(
                            DateUtil.parseDate(i.getProperty(Property.DTSTART).get().getValue()),
                            i.getProperty(Property.SUMMARY).get().getValue()))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
