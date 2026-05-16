// src/app/dashboard/dashboard-user/dashboard-user.component.ts
import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';

@Component({
  selector: 'app-dashboard-user',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard-user.component.html',
  styleUrls: ['./dashboard-user.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardUserComponent implements OnInit {

  userName  = '';
  userId    = '';
  userRole  = 'USER';
  userEmail = '';

  totalSortiesInscrites = 0;
  totalEquipesMembre    = 0;
  totalSortiesCompletees = 0;

  mesSortiesInscrites: SortieResponse[] = [];
  mesEquipes:          EquipeResponse[] = [];
  recommandations:     SortieResponse[] = [];

  loading = { sorties: false, equipes: false, reco: false };
  errors  = { sorties: '', equipes: '' };

  readonly skeletons = [1,2,3];

  // Images Tunisie pour fallback
  private readonly tunisiaImages = [
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=200&fit=crop&auto=format',
    'https://images.unsplash.com/photo-1551632786-fc0b4cd1235b?w=400&h=200&fit=crop&auto=format',
    'https://images.unsplash.com/photo-1469022563149-aa64dbd37dae?w=400&h=200&fit=crop&auto=format',
    'https://images.unsplash.com/photo-1501854140801-50d01698950b?w=400&h=200&fit=crop&auto=format',
    'https://images.unsplash.com/photo-1548013146-72479768bada?w=400&h=200&fit=crop&auto=format',
  ];

  constructor(
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.userId    = localStorage.getItem('userId')    ?? '';
    this.userEmail = localStorage.getItem('userEmail') ?? '';
    this.userRole  = localStorage.getItem('userRole')  ?? 'USER';

    const nom    = localStorage.getItem('userNom')    ?? '';
    const prenom = localStorage.getItem('userPrenom') ?? '';
    this.userName = (prenom || nom)
      ? `${prenom} ${nom}`.trim()
      : (this.userEmail.split('@')[0] || 'Campeur');

    if (this.userId && this.userId !== 'undefined') {
      this.loadMesSorties();
      this.loadMesEquipes();
    }
  }

  // ── Sorties inscrites ──────────────────────────────────────
  loadMesSorties(): void {
    this.loading.sorties = true;
    this.sortieService.getAllSorties().subscribe({
      next: (data) => {
        this.mesSortiesInscrites = data.filter(s =>
          (s.participantIds ?? []).map(String).includes(String(this.userId))
        );
        this.totalSortiesInscrites   = this.mesSortiesInscrites.length;
        this.totalSortiesCompletees  = this.mesSortiesInscrites.filter(
          s => new Date(s.dateDebut) < new Date()
        ).length;

        // Recommandations : sorties futures auxquelles l'user n'est pas inscrit
        this.recommandations = data.filter(s =>
          new Date(s.dateDebut) > new Date() &&
          !(s.participantIds ?? []).map(String).includes(String(this.userId))
        ).slice(0, 3);

        this.loading.sorties = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading.sorties = false;
        this.errors.sorties  = 'Impossible de charger les sorties.';
        this.cdr.markForCheck();
      }
    });
  }

  // ── Équipes ────────────────────────────────────────────────
  loadMesEquipes(): void {
    this.loading.equipes = true;
    this.equipeService.getAllEquipes().subscribe({
      next: (data) => {
        this.mesEquipes = data.filter(e =>
          e.membres?.some(m => String(m?.id) === String(this.userId))
        );
        this.totalEquipesMembre = this.mesEquipes.length;
        this.loading.equipes    = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading.equipes = false;
        this.errors.equipes  = 'Impossible de charger les équipes.';
        this.cdr.markForCheck();
      }
    });
  }

  // ── Actions ────────────────────────────────────────────────
  seDesinscrire(sortieId: string): void {
    if (!confirm('Confirmer la désinscription ?')) return;
    this.sortieService.desinscrire(sortieId).subscribe({
      next: () => this.loadMesSorties(),
      error: (e) => alert(e.error?.message ?? 'Erreur de désinscription')
    });
  }

  sInscrire(sortieId: string): void {
    this.sortieService.inscrire(sortieId).subscribe({
      next: () => this.loadMesSorties(),
      error: (e) => alert(e.status === 409 ? 'Déjà inscrit.' : (e.error?.message ?? 'Erreur'))
    });
  }

  // ── Navigation ────────────────────────────────────────────
  voirSortie(id: string):  void { this.router.navigate(['/sorties', id]); }
  voirEquipe(id: string):  void { this.router.navigate(['/equipes', id]); }

  // ── Helpers UI ────────────────────────────────────────────
  isOrg(): boolean {
    return this.userRole === 'ORGANISATEUR' || this.userRole === 'ROLE_ORGANISATEUR'
        || this.userRole === 'ADMIN'        || this.userRole === 'ROLE_ADMIN';
  }

  getInitiales(): string {
    return (this.userName || '?').split(' ')
      .map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }

  formatDate(d: any): string {
    if (!d) return '—';
    const date = new Date(d);
    return date.toLocaleDateString('fr-FR', { month: 'short', day: 'numeric' });
  }

  getDiffClass(diff: string): string {
    return {
      'FACILE': 'du-diff-facile',
      'MOYEN': 'du-diff-moyen',
      'DIFFICILE': 'du-diff-difficile'
    }[diff] || '';
  }

  getDiffLabel(diff: string): string {
    return { FACILE: '🥾 Facile', MOYEN: '🧗 Modéré', DIFFICILE: '⛰️ Difficile' }[diff] || diff;
  }

  isPlein(s: SortieResponse): boolean {
    return (s.participantIds?.length ?? s.nombreParticipants ?? 0) >= (s.capaciteMax ?? 0);
  }

  getInitialesEquipe(nom: string): string {
    return (nom || '?').split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }

  getSortieImage(s: SortieResponse): string {
    if (s.imageUrl?.trim()) return s.imageUrl;
    const seed = s.id ?? s.titre ?? '';
    let h = 0;
    for (let i = 0; i < seed.length; i++) h = (h * 31 + seed.charCodeAt(i)) >>> 0;
    return this.tunisiaImages[h % this.tunisiaImages.length];
  }

  isSortiePassee(s: SortieResponse): boolean {
    return new Date(s.dateDebut) < new Date();
  }

  getActivityCount(diff: string): number {
    return this.mesSortiesInscrites.filter(s => s.difficulte === diff && this.isSortiePassee(s)).length;
  }

  getActivityHeight(diff: string): number {
    const max = Math.max(1, this.totalSortiesCompletees);
    return Math.round((this.getActivityCount(diff) / max) * 80) + 10;
  }

}