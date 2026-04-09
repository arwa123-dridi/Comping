import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { EquipeService } from '../../services/equipe.service';
import { EquipeResponse } from '../../models/equipe.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-equipe-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './equipe-list.component.html',
  styleUrls: ['./equipe-list.component.css']
})
export class EquipeListComponent implements OnInit {
  equipes: EquipeResponse[] = [];
  loading = false;
  error: string | null = null;
  userId: string | null = null;
  activeFilter = 'ALL';

  private readonly levelImages: Record<string, string> = {
    'Débutant':       'https://images.unsplash.com/photo-1551632811-561732d1e306?w=400&h=200&fit=crop',
    'Intermédiaire':  'https://images.unsplash.com/photo-1533240332313-0db3b4591e1b?w=400&h=200&fit=crop',
    'Avancé':         'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400&h=200&fit=crop',
    'Tous niveaux':   'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=200&fit=crop'
  };

  constructor(
    private equipeService: EquipeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userId = localStorage.getItem('userId');
    this.loadEquipes();
  }

  loadEquipes(): void {
    this.loading = true;
    this.error = null;

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
      case 'MIENNE':  return this.getMesEquipes();
      case 'MEMBRE':  return this.getEquipesMembre();
      case 'DISPO':   return this.getEquipesDisponibles();
      default:        return this.equipes;
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
      !this.isMembre(e) && !this.isOrganisateur(e)
      && this.getMembreCount(e) < (e.nbMembresMax || 10)
    );
  }

  // ── Helpers ───────────────────────────────────────────────

  isMembre(eq: EquipeResponse): boolean {
    if (!this.userId || !eq.membres) return false;
    return eq.membres.some(m => m?.id === this.userId);
  }

  isOrganisateur(eq: EquipeResponse): boolean {
    if (!this.userId) return false;
    return eq.organisateurId === this.userId;
  }

  getMembreCount(eq: EquipeResponse): number {
    return eq.membres?.length || 0;
  }

  getTotalMembres(): number {
    return this.equipes.reduce((s, e) => s + this.getMembreCount(e), 0);
  }

  getEquipeImage(eq: EquipeResponse): string {
    const niveau = eq.niveau || 'Tous niveaux';
    return this.levelImages[niveau] ?? this.levelImages['Tous niveaux'];
  }

  // ── Actions ───────────────────────────────────────────────

  rejoindre(equipeId: string): void {
    if (!this.userId) { this.router.navigate(['/login']); return; }
    const userNom = localStorage.getItem('userNom') || 'Utilisateur';

    this.equipeService.ajouterMembre(equipeId, this.userId, userNom).subscribe({
      next: () => { this.loadEquipes(); alert('✅ Vous avez rejoint l\'équipe !'); },
      error: (err: HttpErrorResponse) => {
        const msg = err.status === 409 ? 'Vous êtes déjà membre.'
                  : err.status === 400 ? 'Équipe complète.'
                  : err.error?.message || 'Erreur.';
        alert(msg);
      }
    });
  }

  quitter(equipeId: string): void {
    if (!this.userId || !confirm('Voulez-vous vraiment quitter cette équipe ?')) return;
    this.equipeService.retirerMembre(equipeId, this.userId).subscribe({
      next: () => { this.loadEquipes(); alert('✅ Vous avez quitté l\'équipe.'); },
      error: (err) => alert(err.error?.message || 'Erreur.')
    });
  }

  deleteEquipe(equipeId: string): void {
    if (!confirm('⚠️ Supprimer définitivement cette équipe ?')) return;
    this.equipeService.deleteEquipe(equipeId).subscribe({
      next: () => { this.loadEquipes(); alert('✅ Équipe supprimée.'); },
      error: (err) => alert(err.error?.message || 'Erreur.')
    });
  }

  voirDetail(id: string): void {
    this.router.navigate(['/equipes', id]);
  }

  trackById(_: number, eq: EquipeResponse): string { return eq.id; }
}