package tn.comping.spring.backendcomping.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

public class WeatherRequest {
    private String city;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}


