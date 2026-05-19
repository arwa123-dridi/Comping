package tn.comping.spring.backendcomping.dto;

import lombok.Builder;
import lombok.Data;

@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {
    private String city;
    private String date;
    private double temperature;
    private double precipitation;
    private double windSpeed;
    private int humidity;

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public double getPrecipitation() { return precipitation; }
    public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }
    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public static WeatherDTOBuilder builder() {
        return new WeatherDTOBuilder();
    }

    public static class WeatherDTOBuilder {
        private WeatherDTO dto = new WeatherDTO();
        public WeatherDTOBuilder city(String city) { dto.setCity(city); return this; }
        public WeatherDTOBuilder date(String date) { dto.setDate(date); return this; }
        public WeatherDTOBuilder temperature(double temperature) { dto.setTemperature(temperature); return this; }
        public WeatherDTOBuilder precipitation(double precipitation) { dto.setPrecipitation(precipitation); return this; }
        public WeatherDTOBuilder windSpeed(double windSpeed) { dto.setWindSpeed(windSpeed); return this; }
        public WeatherDTOBuilder humidity(int humidity) { dto.setHumidity(humidity); return this; }
        public WeatherDTO build() { return dto; }
    }
}
