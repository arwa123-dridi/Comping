// src/app/sortie/sortie-list/sortie-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';
import { SortieRecommandationsComponent } from '../sortie-recommandations/sortie-recommandations-module';

@Component({
  selector: 'app-sortie-list',
  standalone: true,
  imports: [CommonModule, RouterModule, SortieRecommandationsComponent],
  templateUrl: './sortie-list.component.html',
  styleUrls: ['./sortie-list.component.css']
})
export class SortieListComponent implements OnInit {

  sorties:      SortieResponse[] = [];
  loading       = false;
  error:        string | null = null;
  activeFilter  = 'ALL';
  isOrganizer   = false;
  userRole:     string | null = null;

  // ── Images Tunisie — une image unique par lieu ──────────────────────
  private readonly tunisiaImages: string[] = [
    'https://images.unsplash.com/photo-1548013146-72479768bada?w=600&h=300&fit=crop&auto=format', // Sahara
    'https://images.unsplash.com/photo-1464822008023-531b5c8b8049?w=600&h=300&fit=crop&auto=format', // Atlas
    'https://images.unsplash.com/photo-1551632786-fc0b4cd1235b?w=600&h=300&fit=crop&auto=format', // Forêt Ain Draham
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600&h=300&fit=crop&auto=format', // Crête Zaghouan
    'https://images.unsplash.com/photo-1469022563149-aa64dbd37dae?w=600&h=300&fit=crop&auto=format', // Sentier
    'https://images.unsplash.com/photo-1533240332313-0db3b4591e1b?w=600&h=300&fit=crop&auto=format', // Forêt verte Tabarka
    'https://images.unsplash.com/photo-1501854140801-50d01698950b?w=600&h=300&fit=crop&auto=format', // Vallée
    'https://images.unsplash.com/photo-1519904981063-b0cf448d479e?w=600&h=300&fit=crop&auto=format', // Rural Dougga
    'https://images.unsplash.com/photo-1510784722466-f2aa240af7c4?w=600&h=300&fit=crop&auto=format', // Désert dunes
    'https://images.unsplash.com/photo-1586348943529-beaae6c28db9?w=600&h=300&fit=crop&auto=format', // Lac Bizerte
  ];

  private readonly regionImageIndex: Record<string, number> = {
    'zaghouan': 3,  'ain draham': 2, 'aïn draham': 2,
    'tabarka':  5,  'bizerte':    9, 'jendouba':   2,
    'béja':     6,  'beja':       6, 'siliana':    6,
    'kasserine':8,  'sbeitla':    7, 'matmata':    8,
    'nabeul':   6,  'tunis':      9, 'dougga':     7,
    'cap bon':  6,  'el kef':     6, 'kairouan':   7,
  };

  constructor(private sortieService: SortieService, private router: Router) {}

  ngOnInit(): void { 
    this.userRole = localStorage.getItem('userRole');
    this.isOrganizer = this.userRole === 'ORGANISATEUR' || this.userRole === 'ROLE_ORGANISATEUR' || this.userRole === 'ADMIN';
    this.loadSorties(); 
  }

  // ── Chargement ─────────────────────────────────────────────────────
  loadSorties(): void {
    this.loading = true;
    this.error   = null;
    this.sortieService.getAllSorties().subscribe({
      next: (data) => {
        this.sorties = data || [];
        this.loading = false;
      },
      error: (err) => {
        if (err.status === 401) this.router.navigate(['/login']);
        else this.error = 'Erreur lors du chargement des randonnées.';
        this.loading = false;
      }
    });
  }

  // ── Filtres ─────────────────────────────────────────────────────────
  setFilter(filter: string): void { this.activeFilter = filter; }

  getFilteredSorties(): SortieResponse[] {
    return this.activeFilter === 'ALL'
      ? this.sorties
      : this.sorties.filter(s => s.difficulte === this.activeFilter);
  }

  // ✅ RESTAURÉ — utilisé par le HTML pour les compteurs de filtres
  getSortiesByDifficulte(diff: string): SortieResponse[] {
    return this.sorties.filter(s => s.difficulte === diff);
  }

  // ── Participants ─────────────────────────────────────────────────────
  getNombreParticipants(s: SortieResponse): number {
    if (s.participantIds?.length !== undefined) return s.participantIds.length;
    return (s as any).participants?.length ?? 0;
  }

  getPlacesPercent(s: SortieResponse): number {
    if (!s.capaciteMax) return 0;
    return Math.min((this.getNombreParticipants(s) / s.capaciteMax) * 100, 100);
  }

  getTotalParticipants(): number {
    return this.sorties.reduce((sum, s) => sum + this.getNombreParticipants(s), 0);
  }

  isPlein(s: SortieResponse): boolean {
    return this.getNombreParticipants(s) >= (s.capaciteMax ?? 0);
  }

  isInscrit(s: SortieResponse): boolean {
    const uid = localStorage.getItem('userId') ?? '';
    return (s.participantIds ?? []).map(String).includes(String(uid));
  }

  isOrganisateur(organisateurId: string): boolean {
    const uid = localStorage.getItem('userId');
    return !!uid && String(uid) === String(organisateurId);
  }

  isConnected(): boolean {
    return !!localStorage.getItem('authToken');
  }

  // ── Labels ──────────────────────────────────────────────────────────
  getDiffLabel(diff: string): string {
    return ({ FACILE: '🥾 Facile', MOYEN: '🧗 Modéré', DIFFICILE: '⛰️ Difficile' } as any)[diff] ?? diff;
  }

  getDiffClass(diff: string): string {
    return ({ FACILE: 'diff-easy', MOYEN: 'diff-med', DIFFICILE: 'diff-hard' } as any)[diff] ?? '';
  }

  formatDate(d: Date | string): string {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  // ── Image — Cloudinary en priorité, Unsplash Tunisie en fallback ───
  getImageUrl(sortie: SortieResponse): string {
    // ✅ Priorité 1 : URL Cloudinary uploadée par l'organisateur
    if (sortie?.imageUrl?.trim()) return sortie.imageUrl;

    // Priorité 2 : image selon le lieu de départ
    const lieu = (sortie?.lieuDepart ?? sortie?.region ?? '').toLowerCase();
    for (const [key, idx] of Object.entries(this.regionImageIndex)) {
      if (lieu.includes(key)) return this.tunisiaImages[idx];
    }

    // Priorité 3 : hash stable sur sortie.id (même sortie → même image toujours)
    const seed = sortie?.id ?? sortie?.titre ?? '';
    let hash = 0;
    for (let i = 0; i < seed.length; i++) {
      hash = (hash * 31 + seed.charCodeAt(i)) >>> 0;
    }
    return this.tunisiaImages[hash % this.tunisiaImages.length];
  }

  // ── Navigation & Actions ────────────────────────────────────────────
  viewDetail(id: string): void { this.router.navigate(['/sorties', id]); }

  inscrire(sortieId: string): void {
    if (!this.isConnected()) { this.router.navigate(['/login']); return; }
    this.sortieService.inscrire(sortieId).subscribe({
      next: () => this.loadSorties(),
      error: (err) => {
        if (err.status === 401) this.router.navigate(['/login']);
        else if (err.status === 409) alert('Vous êtes déjà inscrit à cette sortie.');
        else alert(err.error?.message ?? "Erreur lors de l'inscription");
      }
    });
  }

  trackById(_: number, item: SortieResponse): string { return item.id; }
}