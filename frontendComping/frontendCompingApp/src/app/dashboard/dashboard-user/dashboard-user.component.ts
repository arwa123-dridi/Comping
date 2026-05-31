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
  userName = '';
  userId = '';
  userEmail = '';

  totalSortiesInscrites = 0;
  totalSortiesRealisees = 0;
  totalEquipesMembre = 0;
  totalRecommandations = 0;

  mesSortiesInscrites: SortieResponse[] = [];
  mesEquipes: EquipeResponse[] = [];
  recommandations: SortieResponse[] = [];

  loading = {
    sorties: false,
    equipes: false,
    reco: false
  };
  errors = {
    sorties: '',
    equipes: '',
    reco: ''
  };

  readonly skeletons = [1, 2, 3];

  constructor(
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.userId = localStorage.getItem('userId') ?? '';
    this.userEmail = localStorage.getItem('userEmail') ?? '';
    const prenom = localStorage.getItem('userPrenom') ?? '';
    const nom = localStorage.getItem('userNom') ?? '';
    this.userName = (prenom || nom) ? `${prenom} ${nom}`.trim() : (this.userEmail.split('@')[0] || 'Campeur');

    if (this.userId && this.userId !== 'undefined') {
      this.loadMesSorties();
      this.loadMesEquipes();
    }
  }

  loadMesSorties(): void {
    this.loading.sorties = true;
    this.sortieService.getAllSorties().subscribe({
      next: (data) => {
        this.mesSortiesInscrites = data.filter(s =>
          (s.participantIds ?? []).map(String).includes(String(this.userId))
        );
        this.totalSortiesInscrites = this.mesSortiesInscrites.length;
        this.totalSortiesRealisees = this.mesSortiesInscrites.filter(
          s => new Date(s.dateDebut) < new Date()
        ).length;

        this.loadRecommandations(data);
        this.loading.sorties = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading.sorties = false;
        this.errors.sorties = 'Impossible de charger les sorties.';
        this.cdr.markForCheck();
      }
    });
  }

  loadRecommandations(allSorties: SortieResponse[]): void {
    this.loading.reco = true;
    this.sortieService.getRecommandations(this.userId).subscribe({
      next: (scores) => {
        this.recommandations = scores.map(score => score.sortie).slice(0, 4);
        this.totalRecommandations = this.recommandations.length;
        this.loading.reco = false;
        this.cdr.markForCheck();
      },
      error: () => {
        // Fallback local : sorties futures non inscrites, triées par difficulté préférée
        const futures = allSorties.filter(s =>
          new Date(s.dateDebut) > new Date() &&
          !(s.participantIds ?? []).map(String).includes(String(this.userId))
        );
        const doneDifficulties = this.mesSortiesInscrites
          .filter(s => new Date(s.dateDebut) < new Date())
          .map(s => s.difficulte);
        const freq: Record<string, number> = {};
        doneDifficulties.forEach(d => freq[d] = (freq[d] || 0) + 1);
        const topDiff = Object.entries(freq).sort((a,b) => b[1]-a[1])[0]?.[0] || 'MOYEN';
        const sorted = futures.sort((a,b) => {
          if (a.difficulte === topDiff && b.difficulte !== topDiff) return -1;
          if (a.difficulte !== topDiff && b.difficulte === topDiff) return 1;
          return 0;
        });
        this.recommandations = sorted.slice(0, 4);
        this.totalRecommandations = this.recommandations.length;
        this.loading.reco = false;
        this.cdr.markForCheck();
      }
    });
  }

  loadMesEquipes(): void {
    this.loading.equipes = true;
    this.equipeService.getAllEquipes().subscribe({
      next: (data) => {
        this.mesEquipes = data.filter(e =>
          (e.membres ?? []).some(m => String(m?.id ?? m) === String(this.userId))
        );
        this.totalEquipesMembre = this.mesEquipes.length;
        this.loading.equipes = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading.equipes = false;
        this.errors.equipes = 'Impossible de charger les équipes.';
        this.cdr.markForCheck();
      }
    });
  }

  seDesinscrire(sortieId: string, event: Event): void {
    event.stopPropagation();
    if (!confirm('Confirmer la désinscription ?')) return;
    this.sortieService.desinscrire(sortieId).subscribe({
      next: () => this.loadMesSorties(),
      error: (e) => alert(e.error?.message ?? 'Erreur de désinscription')
    });
  }

  sInscrire(sortieId: string, event: Event): void {
    event.stopPropagation();
    this.sortieService.inscrire(sortieId).subscribe({
      next: () => this.loadMesSorties(),
      error: (e) => alert(e.status === 409 ? 'Déjà inscrit.' : (e.error?.message ?? 'Erreur'))
    });
  }

  voirSortie(id: string): void {
    this.router.navigate(['/dashboard/sorties', id]);
  }
  voirEquipe(id: string): void {
    this.router.navigate(['/dashboard/equipes', id]);
  }

  getInitiales(): string {
    return (this.userName || '?')
      .split(' ')
      .map(w => w[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  getDiffClass(diff: string): string {
    const map: Record<string, string> = {
      FACILE: 'du-diff-facile',
      MOYEN: 'du-diff-moyen',
      DIFFICILE: 'du-diff-difficile'
    };
    return map[diff] || '';
  }

  getDiffLabel(diff: string): string {
    const map: Record<string, string> = {
      FACILE: '🥾 Facile',
      MOYEN: '🧗 Modéré',
      DIFFICILE: '⛰️ Difficile'
    };
    return map[diff] || diff;
  }

  formatDate(d: any): string {
    if (!d) return '—';
    const date = new Date(d);
    return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  // ✅ CORRECTION : Accepte Date | string
  isPastDate(date: Date | string): boolean {
    const d = typeof date === 'string' ? new Date(date) : date;
    return d < new Date();
  }

  getParticipantsCount(s: SortieResponse): number {
    return s.participantIds?.length ?? s.nombreParticipants ?? 0;
  }

  isPlein(s: SortieResponse): boolean {
    return this.getParticipantsCount(s) >= (s.capaciteMax ?? 0);
  }

  getSortieImage(s: SortieResponse): string {
    if (s.imageUrl && s.imageUrl !== 'null') return s.imageUrl;
    const fallbacks = [
      'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=200&fit=crop',
      'https://images.unsplash.com/photo-1551632786-fc0b4cd1235b?w=400&h=200&fit=crop',
      'https://images.unsplash.com/photo-1469022563149-aa64dbd37dae?w=400&h=200&fit=crop',
      'https://images.unsplash.com/photo-1501854140801-50d01698950b?w=400&h=200&fit=crop'
    ];
    let hash = 0;
    const seed = s.id || s.titre || '';
    for (let i = 0; i < seed.length; i++) hash = (hash * 31 + seed.charCodeAt(i)) >>> 0;
    return fallbacks[hash % fallbacks.length];
  }

  getEquipeAvatarColor(equipe: EquipeResponse): string {
    const id = equipe.id;
    if (!id) return '#2e7d32';
    let hash = 0;
    for (let i = 0; i < id.length; i++) hash = (hash * 31 + id.charCodeAt(i)) >>> 0;
    return `hsl(${hash % 360}, 70%, 45%)`;
  }

  getInitialesEquipe(nom: string): string {
    return (nom || '?').split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }

  getActivityCount(diff: string): number {
    return this.mesSortiesInscrites.filter(s =>
      s.difficulte === diff && this.isPastDate(s.dateDebut)
    ).length;
  }

  getActivityPercent(diff: string): number {
    const max = Math.max(1, this.getActivityCount('FACILE'), this.getActivityCount('MOYEN'), this.getActivityCount('DIFFICILE'));
    const count = this.getActivityCount(diff);
    return (count / max) * 100;
  }
}