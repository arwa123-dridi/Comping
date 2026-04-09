import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-sortie-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sortie-list.component.html',
  styleUrls: ['./sortie-list.component.css']
})
export class SortieListComponent implements OnInit {
  sorties: SortieResponse[] = [];
  loading = false;
  error: string | null = null;
  activeFilter: string = 'ALL';

  private readonly mountainImages: string[] = [
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600&h=300&fit=crop',
    'https://images.unsplash.com/photo-1551632786-fc0b4cd1235b?w=600&h=300&fit=crop',
    'https://images.unsplash.com/photo-1466521486212-810506078bff?w=600&h=300&fit=crop',
    'https://images.unsplash.com/photo-1519904981063-b0cf448d479e?w=600&h=300&fit=crop',
    'https://images.unsplash.com/photo-1464822008023-531b5c8b8049?w=600&h=300&fit=crop',
    'https://images.unsplash.com/photo-1469022563149-aa64dbd37dae?w=600&h=300&fit=crop'
  ];

  constructor(
    private sortieService: SortieService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadSorties();
  }

  loadSorties(): void {
    this.loading = true;
    this.error = null;

    this.sortieService.getAllSorties().subscribe({
      next: (data: SortieResponse[]) => {
        this.sorties = data || [];
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 401) {
          this.router.navigate(['/login']);
        } else {
          this.error = 'Erreur lors du chargement des randonnées.';
        }
        this.loading = false;
      }
    });
  }

  setFilter(filter: string): void {
    this.activeFilter = filter;
  }

  getFilteredSorties(): SortieResponse[] {
    if (this.activeFilter === 'ALL') return this.sorties;
    return this.sorties.filter(s => s.difficulte === this.activeFilter);
  }

  getSortiesByDifficulte(diff: string): SortieResponse[] {
    return this.sorties.filter(s => s.difficulte === diff);
  }

  getNombreParticipants(s: SortieResponse): number {
    // Essayer participantIds (backend principal) puis participants (fallback)
    if (s.participantIds?.length !== undefined) return s.participantIds.length;
    return (s as any).participants?.length || 0;
  }

  getPlacesPercent(s: SortieResponse): number {
    if (!s.capaciteMax || s.capaciteMax === 0) return 0;
    return Math.min((this.getNombreParticipants(s) / s.capaciteMax) * 100, 100);
  }

  getTotalParticipants(): number {
    return this.sorties.reduce((sum, s) => sum + this.getNombreParticipants(s), 0);
  }

  isOrganisateur(organisateurId: string): boolean {
    const userId = localStorage.getItem('userId');
    return !!userId && String(userId) === String(organisateurId);
  }

  getDiffLabel(diff: string): string {
    const map: Record<string, string> = {
      FACILE: '🥾 Facile',
      MOYEN: '🧗 Modéré',
      DIFFICILE: '⛰️ Difficile'
    };
    return map[diff] || diff;
  }

  getImageUrl(titre: string): string {
    let hash = 0;
    for (let i = 0; i < titre.length; i++) {
      hash = (hash + titre.charCodeAt(i)) % this.mountainImages.length;
    }
    return this.mountainImages[hash];
  }

  viewDetail(id: string): void {
    this.router.navigate(['/sorties', id]);
  }

  inscrire(sortieId: string): void {
    const token = localStorage.getItem('authToken');
    const userId = localStorage.getItem('userId');

    if (!token || !userId) {
      this.router.navigate(['/login']);
      return;
    }

    this.sortieService.inscrire(sortieId).subscribe({
      next: () => {
        this.loadSorties();
        alert('✅ Inscription réussie !');
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 401) {
          this.router.navigate(['/login']);
        } else if (err.status === 409) {
          alert('Vous êtes déjà inscrit à cette sortie.');
        } else {
          alert(err.error?.message || 'Erreur lors de l\'inscription');
        }
      }
    });
  }

  trackById(index: number, item: SortieResponse): string {
    return item.id;
  }
}