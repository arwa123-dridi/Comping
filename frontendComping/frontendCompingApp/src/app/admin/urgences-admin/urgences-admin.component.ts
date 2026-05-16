import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { UrgenceService, Urgence } from '../../services/urgence-advanced.service';
import { SecuriteService, Securite } from '../../services/securite.service';
import { WeatherService, WeatherForecastResponse, WeatherForecastItem, GeocodeLocation } from '../../services/weather.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-urgences-admin',
  templateUrl: './urgences-admin.component.html',
  styleUrls: ['./urgences-admin.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule]
})
export class UrgencesAdminComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('mapCanvas') mapCanvas?: ElementRef<HTMLDivElement>;

  // Urgences & Securites
  urgences: Urgence[] = [];
  allUrgences: Urgence[] = [];
  securites: Securite[] = [];

  // Map & Weather
  searchTerm = 'Tunis';
  mapCenter = { lat: 36.8065, lng: 10.1815 };
  markerPosition = { lat: 36.8065, lng: 10.1815 };
  zoom = 7;
  currentLocationLabel = 'Tunisia';
  forecastResponse: WeatherForecastResponse | null = null;
  mapLoadError = '';
  readonly loading$: Observable<boolean>;
  readonly error$: Observable<string | null>;

  // Form & UI
  urgenceForm: FormGroup;
  selectedUrgence: Urgence | null = null;
  showForm = false;
  isEditing = false;
  loading = false;
  errorMessage = '';
  successMessage = '';
  filterStatus = 'TOUS';
  filterNiveau = 'TOUS';
  niveauOptions = ['IMMEDIATE', 'TRES_URGENT', 'URGENT', 'NORMAL', 'BASSE'];
  statutOptions = ['ATTENDANT', 'ACCEPTE', 'REJETEE', 'COMPLETEE'];
  categorieOptions = ['MAINTENANCE', 'MEDICAL', 'SECURITE', 'PERSONNEL', 'AUTRE'];
  prioriteOptions = ['BASSE', 'MOYENNE', 'HAUTE', 'CRITIQUE'];
  
  // Private
  private destroy$ = new Subject<void>();
  private googleMap?: any;
  private googleMarker?: any;
  private mapInitialized = false;

  constructor(
    private urgenceService: UrgenceService,
    private securiteService: SecuriteService,
    private weatherService: WeatherService,
    private fb: FormBuilder
  ) {
    this.urgenceForm = this.createForm();
    this.loading$ = weatherService.loading$;
    this.error$ = weatherService.error$;
  }

  ngOnInit(): void {
    this.loadUrgences();
    this.loadSecurites();
    this.loadWeatherForDefaultLocation();
    this.urgenceService.urgences$
      .pipe(takeUntil(this.destroy$))
      .subscribe(urgences => {
        this.allUrgences = urgences;
        this.applyFilters();
      });
  }

  ngAfterViewInit(): void {
    void this.initializeMap();
  }

  loadSecurites(): void {
    this.securiteService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (securites) => {
          this.securites = securites;
        },
        error: (err) => {
          console.error('Error loading securites:', err);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.googleMap = undefined;
    this.googleMarker = undefined;
  }

  createForm(): FormGroup {
    return this.fb.group({
      titre: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      siteCampingId: ['', Validators.required],
      userId: ['', Validators.required],
      niveauUrgence: ['URGENT', Validators.required],
      estimatedMinutesBeforeResolution: [60, [Validators.required, Validators.min(1)]],
      categorie: ['MAINTENANCE', Validators.required],
      priorite: ['HAUTE', Validators.required],
      reporterId: ['', Validators.required],
      impactScore: [5, [Validators.required, Validators.min(1), Validators.max(10)]],
      estimatedCost: [0],
      contactName: ['', Validators.required],
      contactPhone: ['', Validators.required],
      contactEmail: ['', [Validators.required, Validators.email]],
      location: [''],
      tags: [''],
      notes: ['']
    });
  }

  loadUrgences(): void {
    this.loading = true;
    this.urgenceService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.loading = false,
        error: (err) => {
          this.errorMessage = 'Erreur lors du chargement des urgences';
          this.loading = false;
        }
      });
  }

  applyFilters(): void {
    let filtered = [...this.allUrgences];

    if (this.filterStatus !== 'TOUS') {
      filtered = filtered.filter(u => u.statut === this.filterStatus);
    }

    if (this.filterNiveau !== 'TOUS') {
      filtered = filtered.filter(u => u.niveauUrgence === this.filterNiveau);
    }

    this.urgences = filtered;
  }

  openForm(): void {
    this.showForm = true;
    this.isEditing = false;
    this.urgenceForm.reset();
  }

  editUrgence(urgence: Urgence): void {
    this.isEditing = true;
    this.selectedUrgence = urgence;
    this.urgenceForm.patchValue(urgence);
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.urgenceForm.reset();
    this.selectedUrgence = null;
  }

  submitForm(): void {
    if (!this.urgenceForm.valid) {
      this.errorMessage = 'Formulaire invalide';
      return;
    }

    this.loading = true;

    const formData = this.urgenceForm.value;
    if (formData.tags && typeof formData.tags === 'string') {
      formData.tags = formData.tags.split(',').map((t: string) => t.trim());
    }

    if (this.isEditing && this.selectedUrgence?.id) {
      this.urgenceService.update(this.selectedUrgence.id, formData)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.successMessage = 'Urgence mise à jour avec succès';
            this.closeForm();
            this.loading = false;
          },
          error: (err) => {
            this.errorMessage = 'Erreur lors de la mise à jour';
            this.loading = false;
          }
        });
    } else {
      this.urgenceService.creer(formData)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.successMessage = 'Urgence créée avec succès';
            this.closeForm();
            this.loading = false;
          },
          error: (err) => {
            this.errorMessage = 'Erreur lors de la création';
            this.loading = false;
          }
        });
    }

    setTimeout(() => this.successMessage = '', 3000);
  }

  assign(urgence: Urgence, currentAssigneId: string): void {
    const assigneId = prompt('Entrez l\'ID de la personne à assigner :', currentAssigneId || '');
    if (assigneId === null || !assigneId.trim()) return;

    this.urgenceService.assign(urgence.id!, assigneId.trim())
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.successMessage = 'Urgence assignée avec succès';
          this.loadUrgences();
        },
        error: (err) => this.errorMessage = 'Erreur lors de l\'assignation'
      });
  }

  resolve(urgence: Urgence, currentResolution: string): void {
    const resolution = prompt('Entrez les détails de la résolution :', currentResolution || '');
    if (resolution === null || !resolution.trim()) return;

    this.urgenceService.resolve(urgence.id!, resolution.trim())
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.successMessage = 'Urgence résolue';
          this.loadUrgences();
        },
        error: (err) => this.errorMessage = 'Erreur lors de la résolution'
      });
  }

  delete(urgence: Urgence): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette urgence?')) return;
    this.urgenceService.delete(urgence.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.successMessage = 'Urgence supprimée',
        error: (err) => this.errorMessage = 'Erreur lors de la suppression'
      });
  }

  getStatusColor(statut?: string): string {
    switch (statut) {
      case 'ATTENDANT': return 'warning';
      case 'ACCEPTE': return 'info';
      case 'REJETEE': return 'danger';
      case 'COMPLETEE': return 'success';
      default: return 'secondary';
    }
  }

  getUrgencyColor(niveau?: string): string {
    switch (niveau) {
      case 'IMMEDIATE': return 'danger';
      case 'TRES_URGENT': return 'danger';
      case 'URGENT': return 'warning';
      case 'NORMAL': return 'info';
      case 'BASSE': return 'success';
      default: return 'secondary';
    }
  }

  /**
   * Searches the backend geocoding endpoint, then loads weather for the returned coordinates.
   * FIXED implementation with proper error handling.
   */
  searchLocation(): void {
    const query = this.searchTerm.trim();
    if (!query) {
      return;
    }

    this.weatherService.geocodeAddress(query)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (location) => {
          this.setMapLocation(location);
          this.weatherService.getForecastByCoordinates(location.lat, location.lng)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (forecast) => {
                this.forecastResponse = forecast;
                this.zoom = 8;
              },
              error: () => {} // error$ handled by WeatherService
            });
        },
        error: () => {} // error$ handled by WeatherService
      });
  }

  /**
   * Centers the map on Tunisia and loads a default forecast on startup.
   */
  loadWeatherForDefaultLocation(): void {
    this.weatherService.getForecastByCoordinates(this.mapCenter.lat, this.mapCenter.lng)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (forecast) => {
          this.forecastResponse = forecast;
          this.currentLocationLabel = forecast.formattedAddress || 'Tunisia';
        }
      });
  }

  trackByDate(_: number, item: WeatherForecastItem): string {
    return item.date;
  }

  /**
   * Returns weather risk context for an urgence based on current forecast.
   */
  getWeatherRiskForUrgence(urgence: Urgence): string | null {
    if (!this.forecastResponse?.forecast?.length) return null;
    const today = this.forecastResponse.forecast[0];
    if (!today) return null;
    if (today.windSpeed > 15) return 'Vent fort';
    if (today.temperature > 38) return 'Chaleur extrême';
    if (today.description?.toLowerCase().includes('rain') ||
        today.description?.toLowerCase().includes('pluie') ||
        today.description?.toLowerCase().includes('storm')) return 'Risque météo';
    return null;
  }

  /**
   * Google Maps initialization.
   */
  private async initializeMap(): Promise<void> {
    if (this.mapInitialized) {
      return;
    }

    try {
      await this.ensureGoogleMapsLoaded();

      if (!this.mapCanvas?.nativeElement) {
        return;
      }

      const mapsGlobal = (window as Window & { google?: { maps?: any } }).google;

      this.googleMap = new mapsGlobal!.maps.Map(this.mapCanvas.nativeElement, {
        center: this.mapCenter,
        zoom: this.zoom,
        mapTypeControl: false,
        streetViewControl: false,
        fullscreenControl: false
      });

      this.googleMarker = new mapsGlobal!.maps.Marker({
        position: this.markerPosition,
        map: this.googleMap
      });

      this.mapInitialized = true;
    } catch (error) {
      this.mapLoadError = error instanceof Error ? error.message : 'Unable to load Google Maps.';
    }
  }

  /**
   * Load Google Maps from CDN if not already loaded.
   */
  private ensureGoogleMapsLoaded(): Promise<void> {
    if ((window as Window & { google?: { maps?: any } }).google?.maps) {
      return Promise.resolve();
    }

    if (!environment.googleMapsApiKey) {
      return Promise.reject(new Error('Google Maps API key is missing in src/environments/environment.ts'));
    }

    return new Promise((resolve, reject) => {
      const existingScript = document.querySelector<HTMLScriptElement>('script[data-google-maps="true"]');
      if (existingScript) {
        existingScript.addEventListener('load', () => resolve());
        existingScript.addEventListener('error', () => reject(new Error('Failed to load Google Maps script')));
        return;
      }

      const script = document.createElement('script');
      script.setAttribute('data-google-maps', 'true');
      script.src = `https://maps.googleapis.com/maps/api/js?key=${environment.googleMapsApiKey}`;
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error('Failed to load Google Maps script'));
      document.head.appendChild(script);
    });
  }

  /**
   * Update map center and marker position when location is selected.
   */
  private setMapLocation(location: GeocodeLocation): void {
    this.mapCenter = { lat: location.lat, lng: location.lng };
    this.markerPosition = { lat: location.lat, lng: location.lng };
    this.currentLocationLabel = location.formattedAddress;
    this.zoom = 11;

    if (this.googleMap) {
      this.googleMap.setCenter(this.mapCenter);
      this.googleMap.setZoom(this.zoom);
    }

    if (this.googleMarker) {
      this.googleMarker.setPosition(this.markerPosition);
    }
  }

  /**
   * Get current weather (first item in forecast).
   */
  get currentWeather(): WeatherForecastItem | null {
    return this.forecastResponse?.forecast?.[0] ?? null;
  }
}
