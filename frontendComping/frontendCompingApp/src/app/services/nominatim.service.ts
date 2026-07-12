import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface NominatimSuggestion {
  display_name: string;
  lat: string;
  lon: string;
}

// API publique OpenStreetMap — gratuite, sans clé. Limiter les appels (debounce côté
// composant) : politique d'usage Nominatim = 1 requête/seconde max.
// https://operations.osmfoundation.org/policies/nominatim/
@Injectable({ providedIn: 'root' })
export class NominatimService {
  private readonly apiUrl = 'https://nominatim.openstreetmap.org/search';

  constructor(private http: HttpClient) {}

  search(query: string, countryCode = 'tn'): Observable<NominatimSuggestion[]> {
    const params = new URLSearchParams({
      format: 'json',
      q: query,
      countrycodes: countryCode,
      addressdetails: '0',
      limit: '5'
    });
    return this.http.get<NominatimSuggestion[]>(`${this.apiUrl}?${params.toString()}`);
  }
}
