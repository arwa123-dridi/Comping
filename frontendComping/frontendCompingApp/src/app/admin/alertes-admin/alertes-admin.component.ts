import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AlerteResponse, UrgenceService } from '../../services/urgence.service';
import { WeatherMapService, WeatherData } from '../../services/weather-map.service';

@Component({
  selector: 'app-alertes-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './alertes-admin.component.html',
  styleUrl: './alertes-admin.component.css'
})
export class AlertesAdminComponent implements OnInit {
  alertes: AlerteResponse[] = [];
  loading = false;
  statutFilter = '';
  siteCampingIdFilter = '';
  weatherData: { [key: string]: WeatherData } = {};

  constructor(
    private urgenceService: UrgenceService,
    private weatherMapService: WeatherMapService
  ) {}

  ngOnInit(): void {
    this.loadAlertes();
  }

  loadAlertes(): void {
    this.loading = true;
    this.urgenceService.getAlertes(this.statutFilter, this.siteCampingIdFilter).subscribe({
      next: (data) => {
        this.alertes = data;
        this.loadWeatherForAlertes();
        this.loading = false;
      },
      error: () => {
        this.alertes = [];
        this.loading = false;
      }
    });
  }

  private loadWeatherForAlertes(): void {
    const today = new Date().toISOString().split('T')[0];
    this.alertes.forEach(alerte => {
      this.weatherMapService.getWeather('tunis', today).subscribe({
        next: weather => {
          this.weatherData[alerte.id] = weather;
        },
        error: () => {}
      });
    });
  }

  getWeatherForAlerte(alerteId: string): WeatherData | null {
    return this.weatherData[alerteId] || null;
  }

  prendreEnCharge(alerte: AlerteResponse): void {
    this.urgenceService.prendreEnCharge(alerte.id).subscribe(() => this.loadAlertes());
  }

  cloturer(alerte: AlerteResponse): void {
    this.urgenceService.cloturer(alerte.id).subscribe(() => this.loadAlertes());
  }
}
