import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface ChecklistRequest {
  temperature: number;
  precipitation: number;
  windSpeed: number;
  humidity: number;
  difficulte: number;
}

export interface ChecklistResponse {
  success: boolean;
  checklistItem: string;
  confidence: number;
  details: string;
  alertLevel: string;
  recommendations: string[];
  error?: string;
}

@Component({
  selector: 'app-checklist-ia',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './checklist-ia.component.html',
  styleUrls: ['./checklist-ia.component.css']
})
export class ChecklistIaComponent {
  
  request: ChecklistRequest = {
    temperature: 25,
    precipitation: 0,
    windSpeed: 15,
    humidity: 60,
    difficulte: 2
  };
  
  response: ChecklistResponse | null = null;
  loading = false;
  errorMessage: string | null = null;
  
  constructor(private http: HttpClient) {}
  
  generateChecklist(): void {
    this.loading = true;
    this.response = null;
    this.errorMessage = null;
    
    this.http.post<ChecklistResponse>('http://localhost:8080/api/checklist/predict', this.request)
      .subscribe({
        next: (res) => {
          this.loading = false;
          if (res.success) {
            this.response = res;
          } else {
            this.errorMessage = res.error || 'Erreur de prédiction';
          }
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage = 'Erreur de connexion au serveur. Vérifiez que Spring Boot est lancé.';
          console.error(err);
        }
      });
  }
  
  getAlertClass(alertLevel: string): string {
    if (alertLevel.includes('VERT')) return 'alert-vert';
    if (alertLevel.includes('JAUNE')) return 'alert-jaune';
    if (alertLevel.includes('ORANGE')) return 'alert-orange';
    if (alertLevel.includes('ROUGE')) return 'alert-rouge';
    return 'alert-default';
  }
  
  getIconForItem(item: string): string {
    const icons: { [key: string]: string } = {
      'vetement_chaud': '🧥',
      'veste_chaude_imperm': '🧥',
      'protection_solaire': '🧴',
      'protection_solaire_extreme': '☀️',
      'impermable_light': '🌂',
      'impermable_complet': '☔',
      'coupe_vent': '💨',
      'coupe_vent_renforce': '💨',
      'equipement_securite': '🆘',
      'alerte_canicule': '🌡️',
      'alerte_vent_rouge': '⚠️',
      'standard': '🎒'
    };
    return icons[item] || '📋';
  }
}