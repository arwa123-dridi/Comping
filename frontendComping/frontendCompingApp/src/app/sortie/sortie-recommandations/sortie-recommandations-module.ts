import { Component, OnInit, OnChanges, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';
import { SortieScoreDTO } from '../../models/sortie-score.model';
import { CloudinaryService } from '../../services/cloudinary.service';

@Component({
  selector: 'app-sortie-recommandations',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sortie-recommandations.component.html',
  styleUrls: ['./sortie-recommandations.component.css']
})
export class SortieRecommandationsComponent implements OnInit, OnChanges {
  @Input() allSorties: SortieResponse[] = [];

  recommandations: SortieResponse[] = [];
  loading = false;
  toastMessage: string | null = null;
  toastType: 'success' | 'error' | 'info' = 'info';
  private toastTimer: any;
  error: string | null = null;
  userId: string = '';
  hasHistory = false;

  constructor(
    private sortieService: SortieService,
    private router: Router,
    private cloudinary: CloudinaryService
  ) {}

  ngOnInit(): void {
    this.userId = localStorage.getItem('userId') || '';
    if (this.userId && this.allSorties.length > 0) {
      this.loadRecommandations();
    }
  }

  ngOnChanges(): void {
    // Recharger dès que le parent fournit les sorties
    if (this.userId && this.allSorties.length > 0) {
      this.loadRecommandations();
    }
  }

  loadRecommandations(): void {
    this.loading = true;
    this.error = null;

    this.sortieService.getRecommandations(this.userId).subscribe({
      next: (scores: SortieScoreDTO[]) => {
        this.recommandations = scores.map(score => score.sortie);
        this.hasHistory = this.allSorties.some(s => s.participantIds?.includes(this.userId));
        this.loading = false;
      },
      error: (err) => {
        console.warn('Backend indisponible, fallback local', err);
        this.recommandations = this.sortieService.getRecommandationsLocales(
          this.userId,
          this.allSorties
        );
        this.hasHistory = this.allSorties.some(s => s.participantIds?.includes(this.userId));
        this.loading = false;
      }
    });
  }

  // ✅ Utilisation de Cloudinary (comme dans sortie-list)
  getImageUrl(sortie: SortieResponse): string {
    return this.cloudinary.getImageUrl(
      sortie.imageUrl,
      sortie.difficulte,
      sortie.id,
      sortie.titre
    );
  }

  // ✅ Calcul des places disponibles (car le modèle n'a pas de champ direct)
  getPlacesDisponibles(sortie: SortieResponse): number {
    const participants = sortie.participantIds?.length ?? sortie.nombreParticipants ?? 0;
    const capacite = sortie.capaciteMax ?? 0;
    return Math.max(capacite - participants, 0);
  }

  getPlacesPercent(sortie: SortieResponse): number {
    const participants = sortie.participantIds?.length ?? sortie.nombreParticipants ?? 0;
    const cap = sortie.capaciteMax ?? 1;
    return Math.min((participants / cap) * 100, 100);
  }

  getDiffClass(diff: string): string {
    return diff?.toLowerCase() || 'facile';
  }

  getDiffLabel(diff: string): string {
    const map: Record<string, string> = {
      FACILE: '🟢 Facile',
      MOYEN: '🟡 Modéré',
      DIFFICILE: '🔴 Difficile'
    };
    return map[diff] || diff;
  }

  viewDetail(id: string): void {
    this.router.navigate(['/sorties', id]);
  }

  inscriptionRapide(sortieId: string, event: Event): void {
    event.stopPropagation();
    if (!this.userId) {
      this.router.navigate(['/login']);
      return;
    }
    this.sortieService.inscrire(sortieId).subscribe({
      next: () => {
        this.showToast('✅ Inscription réussie !', 'success');
        this.loadRecommandations();
      },
      error: (err) => {
        if (err.status === 409) {
          this.showToast('ℹ️ Vous êtes déjà inscrit.', 'info');
        } else {
          this.showToast('❌ ' + (err.error?.message || 'Erreur inscription'), 'error');
        }
      }
    });
  }

  formatDate(date: Date | string): string {
    return new Date(date).toLocaleDateString('fr-FR', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }

  private showToast(msg: string, type: 'success' | 'error' | 'info' = 'info'): void {
    this.toastMessage = msg;
    this.toastType = type;
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => { this.toastMessage = null; }, 3500);
  }

  trackById(_: number, item: SortieResponse): string {
    return item.id;
  }
}