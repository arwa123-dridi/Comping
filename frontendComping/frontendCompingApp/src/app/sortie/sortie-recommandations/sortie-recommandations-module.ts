import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';
import { SortieScoreDTO } from '../../models/sortie-score.model';

@Component({
  selector: 'app-sortie-recommandations',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sortie-recommandations.component.html',
})
export class SortieRecommandationsComponent implements OnInit {
  @Input() allSorties: SortieResponse[] = [];

  recommandations: SortieResponse[] = [];
  loading = false;
  error: string | null = null;
  userId: string = '';
  hasHistory = false;

  private readonly images: string[] = [
    'https://images.unsplash.com/photo-1551632786-fc0b4cd1235b?w=500&h=250&fit=crop',
    'https://images.unsplash.com/photo-1469022563149-aa64dbd37dae?w=500&h=250&fit=crop',
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=500&h=250&fit=crop',
    'https://images.unsplash.com/photo-1464822008023-531b5c8b8049?w=500&h=250&fit=crop'
  ];

  constructor(
    private sortieService: SortieService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userId = localStorage.getItem('userId') || '';
    if (this.userId) {
      this.loadRecommandations();
    }
  }

  loadRecommandations(): void {
    this.loading = true;
    this.error = null;

    // Appel au backend (retourne des SortieScoreDTO)
    this.sortieService.getRecommandations(this.userId).subscribe({
      next: (scores: SortieScoreDTO[]) => {
        // Extraire les sorties depuis les scores
        this.recommandations = scores.map(score => score.sortie);
        this.hasHistory = scores.length > 0;
        this.loading = false;
      },
      error: (err) => {
        console.warn('Backend indisponible, fallback local', err);
        // Fallback : calcul local basé sur les sorties chargées
        this.recommandations = this.sortieService.getRecommandationsLocales(
          this.userId,
          this.allSorties
        );
        this.hasHistory = this.allSorties.some(s => s.participantIds?.includes(this.userId));
        this.loading = false;
      }
    });
  }

  getImageUrl(titre: string): string {
    let hash = 0;
    for (let i = 0; i < titre.length; i++) {
      hash = (hash + titre.charCodeAt(i)) % this.images.length;
    }
    return this.images[hash];
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
        alert('✅ Inscription réussie !');
        this.loadRecommandations();
      },
      error: (err) => {
        if (err.status === 409) {
          alert('Vous êtes déjà inscrit à cette sortie.');
        } else {
          alert(err.error?.message || 'Erreur lors de l\'inscription');
        }
      }
    });
  }

  formatDate(date: Date | string): string {
    return new Date(date).toLocaleDateString('fr-FR', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }

  trackById(_: number, item: SortieResponse): string {
    return item.id;
  }
}