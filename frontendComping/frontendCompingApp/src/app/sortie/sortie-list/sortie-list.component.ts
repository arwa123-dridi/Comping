// src/app/sortie/sortie-list/sortie-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieResponse } from '../../models/sortie.model';
import { SortieRecommandationsComponent } from '../sortie-recommandations/sortie-recommandations-module';
import { CloudinaryService } from '../../services/cloudinary.service';

@Component({
  selector: 'app-sortie-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, DatePipe, SortieRecommandationsComponent],
  templateUrl: './sortie-list.component.html',
  styleUrls: ['./sortie-list.component.css']
})
export class SortieListComponent implements OnInit {
  sorties: SortieResponse[] = [];
  loading = false;
  error: string | null = null;
  activeFilter = 'ALL';
  isOrganizer = false;
  userRole: string | null = null;
  searchTerm = '';
  toastMessage: string | null = null;
  toastType: 'success' | 'error' | 'info' = 'success';
  private toastTimer: any;

  constructor(
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private router: Router,
    private cloudinary: CloudinaryService
  ) {}

  ngOnInit(): void {
    this.userRole = localStorage.getItem('userRole');
    this.isOrganizer = this.userRole === 'ORGANISATEUR' || this.userRole === 'ROLE_ORGANISATEUR';
    this.loadSorties();
  }

  isUser(): boolean {
    const r = this.userRole ?? '';
    return r === 'USER' || r === 'ROLE_USER';
  }

  isAdmin(): boolean {
    const r = this.userRole ?? '';
    return r === 'ADMIN' || r === 'ROLE_ADMIN';
  }

  isConnected(): boolean {
    const token = localStorage.getItem('authToken');
    return !!token && token !== 'null' && token !== 'undefined';
  }

  isOrganisateur(organisateurId: string): boolean {
    const uid = localStorage.getItem('userId');
    return !!uid && String(uid) === String(organisateurId);
  }

  isInscrit(s: SortieResponse): boolean {
    const uid = localStorage.getItem('userId') ?? '';
    return (s.participantIds ?? []).map(String).includes(String(uid));
  }

  loadSorties(): void {
    this.loading = true;
    this.error = null;
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

  setFilter(filter: string): void {
    this.activeFilter = filter;
  }

  applySearch(): void {}

  clearSearch(): void {
    this.searchTerm = '';
  }

  getFilteredSorties(): SortieResponse[] {
    let result = this.activeFilter === 'ALL'
      ? this.sorties
      : this.sorties.filter(s => s.difficulte === this.activeFilter);

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(s =>
        s.titre?.toLowerCase().includes(term) ||
        s.lieuDepart?.toLowerCase().includes(term) ||
        s.region?.toLowerCase().includes(term) ||
        s.organisateurNom?.toLowerCase().includes(term)
      );
    }
    return result;
  }

  getSortiesByDifficulte(diff: string): SortieResponse[] {
    return this.sorties.filter(s => s.difficulte === diff);
  }

  getNombreParticipants(s: SortieResponse): number {
    return s.participantIds?.length ?? s.nombreParticipants ?? (s as any).participants?.length ?? 0;
  }

  getTotalParticipants(): number {
    return this.sorties.reduce((sum, s) => sum + this.getNombreParticipants(s), 0);
  }

  getPlacesPercent(s: SortieResponse): number {
    if (!s.capaciteMax) return 0;
    return Math.min((this.getNombreParticipants(s) / s.capaciteMax) * 100, 100);
  }

  viewDetail(id: string): void {
    this.router.navigate(['/sorties', id]);
  }

  inscrire(sortieId: string): void {
    if (!this.isConnected()) {
      localStorage.setItem('redirect_after_login', `/sorties/${sortieId}`);
      this.router.navigate(['/login']);
      return;
    }
    if (this.isAdmin() || this.isOrganizer) {
      this.showToast('Seuls les utilisateurs peuvent s\'inscrire.', 'error');
      return;
    }

    this.sortieService.inscrire(sortieId).subscribe({
      next: () => {
        const sortie = this.sorties.find(s => String(s.id) === String(sortieId));
        if (sortie?.equipeId) {
          const userId = localStorage.getItem('userId') ?? '';
          const userNom = `${localStorage.getItem('userPrenom') ?? ''} ${localStorage.getItem('userNom') ?? ''}`.trim();
          this.equipeService.ajouterMembre(sortie.equipeId, userId, userNom).subscribe();
        }
        this.loadSorties();
        this.showToast('✓ Inscription réussie !', 'success');
      },
      error: (err) => {
        if (err.status === 401) this.router.navigate(['/login']);
        else if (err.status === 409) this.showToast('Déjà inscrit à cette sortie', 'error');
        else this.showToast(err.error?.message ?? "Erreur d'inscription", 'error');
      }
    });
  }

  desinscrire(sortieId: string): void {
    this.sortieService.desinscrire(sortieId).subscribe({
      next: () => {
        const sortie = this.sorties.find(s => String(s.id) === String(sortieId));
        if (sortie?.equipeId) {
          const userId = localStorage.getItem('userId') ?? '';
          this.equipeService.retirerMembre(sortie.equipeId, userId).subscribe();
        }
        this.loadSorties();
        this.showToast('Désinscription effectuée', 'info');
      },
      error: (err) => {
        this.showToast(err.error?.message ?? 'Erreur de désinscription', 'error');
      }
    });
  }

  deleteSortie(id: string): void {
    if (!confirm('Supprimer définitivement cette randonnée ?')) return;
    this.sortieService.deleteSortie(id).subscribe({
      next: () => {
        this.sorties = this.sorties.filter(s => String(s.id) !== String(id));
        this.showToast('Randonnée supprimée', 'success');
      },
      error: (err) => {
        this.showToast(err.error?.message || 'Erreur lors de la suppression', 'error');
      }
    });
  }

  getDiffLabel(diff: string): string {
    const map: Record<string, string> = {
      FACILE: '🥾 Facile',
      MOYEN: '🧗 Modéré',
      DIFFICILE: '⛰️ Difficile'
    };
    return map[diff] ?? diff;
  }

  // ✅ Utilisation du service Cloudinary
  getImageUrl(s: SortieResponse): string {
    return this.cloudinary.getImageUrl(s.imageUrl, s.difficulte, s.id, s.titre);
  }

  onImgError(event: Event, s: SortieResponse): void {
    const img = event.target as HTMLImageElement;
    img.src = this.cloudinary.getImageUrl(undefined, s.difficulte, s.id, s.titre);
  }

  private showToast(msg: string, type: 'success' | 'error' | 'info' = 'success'): void {
    this.toastMessage = msg;
    this.toastType = type;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => { this.toastMessage = null; }, 4000);
  }

  trackById(_: number, item: SortieResponse): string {
    return item.id;
  }
}