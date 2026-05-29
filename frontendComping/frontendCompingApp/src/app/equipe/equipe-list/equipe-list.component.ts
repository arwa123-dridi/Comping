// src/app/equipe/equipe-list/equipe-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule, SlicePipe } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { EquipeService } from '../../services/equipe.service';
import { EquipeResponse } from '../../models/equipe.model';
import { HttpErrorResponse } from '@angular/common/http';
import { EquipeRecommandationsComponent } from '../equipe-recommandations/equipe-recommandations-module';

@Component({
  selector: 'app-equipe-list',
  standalone: true,
  // ✅ SlicePipe ajouté — nécessaire pour | slice dans le template
  imports: [CommonModule, RouterModule, SlicePipe, EquipeRecommandationsComponent],
  templateUrl: './equipe-list.component.html',
  styleUrls: ['./equipe-list.component.css']
})
export class EquipeListComponent implements OnInit {

  equipes:         EquipeResponse[] = [];
  loading          = false;
  error:           string | null = null;
  leavingEquipeId: string | null = null;

  toastMessage: string | null = null;
  toastType:    'success' | 'error' | 'info' = 'info';
  private toastTimer: any;

  userId:       string | null = null;
  userRole:     string | null = null;
  activeFilter  = 'ALL';
  isOrganizer   = false;

  // Images Unsplash par niveau
  private readonly equipeImgByLevel: Record<string, string> = {
    'Débutant':      'https://images.unsplash.com/photo-1551632811-561732d1e306?w=400&h=200&fit=crop',
    'Intermédiaire': 'https://images.unsplash.com/photo-1533240332313-0db3b4591e1b?w=400&h=200&fit=crop',
    'Avancé':        'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400&h=200&fit=crop',
    'Tous niveaux':  'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=200&fit=crop',
  };

  private readonly equipeImgFallback: string[] = [
    'https://images.unsplash.com/photo-1469022563149-aa64dbd37dae?w=400&h=200&fit=crop',
    'https://images.unsplash.com/photo-1501854140801-50d01698950b?w=400&h=200&fit=crop',
    'https://images.unsplash.com/photo-1519904981063-b0cf448d479e?w=400&h=200&fit=crop',
    'https://images.unsplash.com/photo-1548013146-72479768bada?w=400&h=200&fit=crop',
  ];

  constructor(
    private equipeService: EquipeService,
    private router:        Router
  ) {}

  ngOnInit(): void {
    this.userId     = localStorage.getItem('userId');
    this.userRole   = localStorage.getItem('userRole');
    this.isOrganizer = this.userRole === 'ORGANISATEUR'
                    || this.userRole === 'ROLE_ORGANISATEUR';
    this.loadEquipes();
  }

  // ── Rôles ────────────────────────────────────────────────
  isUser(): boolean {
    const r = this.userRole ?? '';
    return r === 'USER' || r === 'ROLE_USER';
  }

  isAdmin(): boolean {
    const r = this.userRole ?? '';
    return r === 'ADMIN' || r === 'ROLE_ADMIN';
  }

  // ── Chargement ───────────────────────────────────────────
  loadEquipes(): void {
    this.loading = true;
    this.error   = null;
    this.equipeService.getAllEquipes().subscribe({
      next: (data: EquipeResponse[]) => {
        this.equipes = data || [];
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 401) this.router.navigate(['/login']);
        else this.error = 'Erreur lors du chargement des équipes.';
        this.loading = false;
        this.equipes = [];
      }
    });
  }

  // ── Filtres ──────────────────────────────────────────────
  setFilter(f: string): void { this.activeFilter = f; }

  getFilteredEquipes(): EquipeResponse[] {
    switch (this.activeFilter) {
      case 'MIENNE': return this.getMesEquipes();
      case 'MEMBRE': return this.getEquipesMembre();
      case 'DISPO':  return this.getEquipesDisponibles();
      default:       return this.equipes;
    }
  }

  getMesEquipes(): EquipeResponse[] {
    return this.equipes.filter(e => this.isOrganisateur(e));
  }

  getEquipesMembre(): EquipeResponse[] {
    return this.equipes.filter(e => this.isMembre(e) && !this.isOrganisateur(e));
  }

  getEquipesDisponibles(): EquipeResponse[] {
    return this.equipes.filter(e =>
      !this.isMembre(e)
      && !this.isOrganisateur(e)
      && this.getMembreCount(e) < (e.nbMembresMax ?? e.capaciteMax ?? 10)
    );
  }

  // ── Helpers ──────────────────────────────────────────────
  isMembre(eq: EquipeResponse): boolean {
    if (!this.userId || !eq.membres) return false;
    return eq.membres.some(m => String(m?.id ?? m) === String(this.userId));
  }

  isOrganisateur(eq: EquipeResponse): boolean {
    if (!this.userId) return false;
    return String(eq.organisateurId) === String(this.userId);
  }

  getMembreCount(eq: EquipeResponse): number {
    return eq.membres?.length ?? eq.nbMembresActuels ?? 0;
  }

  getTotalMembres(): number {
    return this.equipes.reduce((s, e) => s + this.getMembreCount(e), 0);
  }

  getEquipeImage(eq: EquipeResponse & { imageUrl?: string }): string {
    if (eq?.imageUrl?.trim() && eq.imageUrl !== 'null') return eq.imageUrl;
    if (eq?.niveau && this.equipeImgByLevel[eq.niveau]) {
      return this.equipeImgByLevel[eq.niveau];
    }
    // Hash déterministe sur l'id pour image cohérente
    const seed = String(eq?.id ?? eq?.nom ?? '');
    let hash = 0;
    for (let i = 0; i < seed.length; i++) {
      hash = (hash * 31 + seed.charCodeAt(i)) >>> 0;
    }
    return this.equipeImgFallback[hash % this.equipeImgFallback.length];
  }

  // ── Actions ──────────────────────────────────────────────
  rejoindre(equipeId: string): void {
    if (!this.userId) {
      localStorage.setItem('redirect_after_login', '/equipes');
      this.router.navigate(['/login']);
      return;
    }
    if (this.isAdmin() || this.isOrganizer) {
      this.showToast('Seuls les utilisateurs peuvent rejoindre des équipes.', 'error');
      return;
    }
    const prenom  = localStorage.getItem('userPrenom') ?? '';
    const nom     = localStorage.getItem('userNom') ?? '';
    const userNom = `${prenom} ${nom}`.trim() || 'Utilisateur';

    this.equipeService.ajouterMembre(equipeId, this.userId, userNom).subscribe({
      next: () => {
        this.loadEquipes();
        this.showToast('✅ Vous avez rejoint l\'équipe !', 'success');
      },
      error: (err: HttpErrorResponse) => {
        const msg = err.status === 409 ? 'Vous êtes déjà membre de cette équipe.'
                  : err.status === 400 ? 'Équipe complète ou action non autorisée.'
                  : err.error?.message ?? 'Erreur lors de l\'inscription.';
        this.showToast(msg, 'error');
      }
    });
  }

  quitter(equipeId: string): void {
    if (!this.userId) return;

    const equipe = this.equipes.find(e => String(e.id) === String(equipeId));
    if (!equipe) { this.showToast('Équipe introuvable.', 'error'); return; }
    if (this.isOrganisateur(equipe)) {
      this.showToast('L\'organisateur ne peut pas quitter sa propre équipe.', 'error');
      return;
    }

    this.leavingEquipeId = equipeId;
    this.equipeService.retirerMembre(equipeId, this.userId).subscribe({
      next: () => {
        this.leavingEquipeId = null;
        this.loadEquipes();
        this.showToast('✅ Vous avez quitté l\'équipe.', 'success');
      },
      error: (err: HttpErrorResponse) => {
        this.leavingEquipeId = null;
        const msg = err.status === 403 ? 'Action non autorisée.'
                  : err.status === 404 ? 'Équipe ou utilisateur introuvable.'
                  : err.error?.message ?? 'Erreur lors de la désinscription.';
        this.showToast(msg, 'error');
      }
    });
  }

  deleteEquipe(equipeId: string): void {
    if (!confirm('Supprimer définitivement cette équipe ?')) return;
    this.equipeService.deleteEquipe(equipeId).subscribe({
      next: () => {
        this.equipes = this.equipes.filter(e => String(e.id) !== String(equipeId));
        this.showToast('✅ Équipe supprimée.', 'success');
      },
      error: (err: HttpErrorResponse) => {
        this.showToast(err.error?.message ?? 'Erreur lors de la suppression.', 'error');
      }
    });
  }

  voirDetail(id: string): void { this.router.navigate(['/equipes', id]); }

  trackById(_: number, eq: EquipeResponse): string { return String(eq.id); }

  // ── Toast ────────────────────────────────────────────────
  showToast(msg: string, type: 'success' | 'error' | 'info' = 'info'): void {
    this.toastMessage = msg;
    this.toastType    = type;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer   = setTimeout(() => { this.toastMessage = null; }, 4000);
  }
}