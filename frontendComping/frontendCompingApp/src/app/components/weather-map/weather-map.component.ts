import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { WeatherMapService, MapLocation, WeatherData, RiskAssessment } from '../../services/weather-map.service';
import { Urgence } from '../../services/urgence-advanced.service';
import { Securite } from '../../services/securite.service';

@Component({
  selector: 'app-weather-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './weather-map.component.html',
  styleUrl: './weather-map.component.css'
})
export class WeatherMapComponent implements OnInit, OnDestroy {
  @Input() urgences: Urgence[] = [];
  @Input() securites: Securite[] = [];

  mapLocations: MapLocation[] = [];
  selectedLocation: MapLocation | null = null;
  selectedWeather: WeatherData | null = null;
  selectedRisk: RiskAssessment | null = null;
  centerLat: number = 36.8065;
  centerLng: number = 10.1815;
  zoom: number = 12;

  private destroy$ = new Subject<void>();

  constructor(private weatherMapService: WeatherMapService) {}

  ngOnInit(): void {
    this.weatherMapService.mapLocations$
      .pipe(takeUntil(this.destroy$))
      .subscribe(locations => {
        this.mapLocations = locations;
      });

    this.weatherMapService.centerLocation$
      .pipe(takeUntil(this.destroy$))
      .subscribe(center => {
        this.centerLat = center.lat;
        this.centerLng = center.lng;
      });

    this.updateMap();
  }

  ngOnChanges(): void {
    this.updateMap();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private updateMap(): void {
    this.weatherMapService.updateMapLocations(this.urgences, this.securites);
  }

  selectLocation(location: MapLocation): void {
    this.selectedLocation = location;
    this.getWeatherAndRisk(location);
  }

  private getWeatherAndRisk(location: MapLocation): void {
    const today = new Date().toISOString().split('T')[0];
    this.weatherMapService.getWeather('tunis', today).subscribe({
      next: weather => {
        this.selectedWeather = weather;
        this.weatherMapService.getRiskAssessment(location.data, weather).subscribe(risk => {
          this.selectedRisk = risk;
        });
      },
      error: () => {
        this.selectedWeather = null;
        this.selectedRisk = null;
      }
    });
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'urgence': return '🔴';
      case 'securite': return '🔵';
      case 'alerte': return '🟡';
      default: return '📍';
    }
  }

  getRiskColor(level?: string): string {
    switch (level) {
      case 'CRITICAL': return '#dc3545';
      case 'HIGH': return '#fd7e14';
      case 'MEDIUM': return '#ffc107';
      default: return '#28a745';
    }
  }

  getMarkerLeft(location: MapLocation): number {
    return 50 + (location.lng - this.centerLng) * 10;
  }

  getMarkerTop(location: MapLocation): number {
    return 50 + (location.lat - this.centerLat) * -10;
  }

  centerOnLocation(location: MapLocation): void {
    this.centerLat = location.lat;
    this.centerLng = location.lng;
    this.weatherMapService.setCenterLocation(location.lat, location.lng);
  }
}