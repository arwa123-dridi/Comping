import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { EquipeService } from '../../services/equipe.service';
import { EquipeResponse } from '../../models/equipe.model';
import { HttpErrorResponse } from '@angular/common/http';
import { EquipeRecommandationsComponent } from '../equipe-recommandations/equipe-recommandations-module';



@Component({
  selector: 'app-equipe-list',
  standalone: true,
  imports: [CommonModule, RouterModule ,EquipeRecommandationsComponent],
  templateUrl: './equipe-list.component.html',
  styleUrls: ['./equipe-list.component.css']
})
export class EquipeListComponent implements OnInit {
  equipes: EquipeResponse[] = [];
  loading = false;
  error: string | null = null;
  userId: string | null = null;
  userRole: string | null = null;
  activeFilter = 'ALL';
  isOrganizer = false;

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
    this.userRole = localStorage.getItem('userRole');
    this.isOrganizer = this.userRole === 'ORGANISATEUR' || this.userRole === 'ROLE_ORGANISATEUR' || this.userRole === 'ADMIN';
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
    // ✅ USERS: voir seulement leurs équipes (membre ou orga)
    if (!this.isOrganizer) {
      const mesEquipes = this.equipes.filter(e => 
        this.isMembre(e) || this.isOrganisateur(e)
      );
      
      switch (this.activeFilter) {
        case 'MIENNE':  return mesEquipes.filter(e => this.isOrganisateur(e));
        case 'MEMBRE':  return mesEquipes.filter(e => this.isMembre(e) && !this.isOrganisateur(e));
        default:        return mesEquipes;
      }
    }
    
    // ✅ ORGANISATORS/ADMINS: voir toutes les équipes
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

   private readonly equipeImgByLevel: Record<string, string> = {
    'Débutant':       'https://images.unsplash.com/photo-1551632811-561732d1e306?w=400&h=200&fit=crop&auto=format',
    'Intermédiaire':  'https://images.unsplash.com/photo-1533240332313-0db3b4591e1b?w=400&h=200&fit=crop&auto=format',
    'Avancé':         'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400&h=200&fit=crop&auto=format',
    'Tous niveaux':   'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=200&fit=crop&auto=format',
  };
 
  private readonly equipeImgFallback: string[] = [
    'https://images.unsplash.com/photo-1469022563149-aa64dbd37dae?w=400&h=200&fit=crop&auto=format',
    'https://images.unsplash.com/photo-1501854140801-50d01698950b?w=400&h=200&fit=crop&auto=format',
    'https://images.unsplash.com/photo-1519904981063-b0cf448d479e?w=400&h=200&fit=crop&auto=format',
    'https://images.unsplash.com/photo-1548013146-72479768bada?w=400&h=200&fit=crop&auto=format',
  ];
 
  getEquipeImage(eq: EquipeResponse & { imageUrl?: string }): string {
    // ✅ Cloudinary d'abord
    if (eq?.imageUrl && eq.imageUrl.trim() !== '') {
      return eq.imageUrl;
    }
    // Par niveau
    if (eq?.niveau && this.equipeImgByLevel[eq.niveau]) {
      return this.equipeImgByLevel[eq.niveau];
    }
    // Hash stable sur id
    const seed = eq?.id ?? eq?.nom ?? '';
    let hash = 0;
    for (let i = 0; i < seed.length; i++) {
      hash = (hash * 31 + seed.charCodeAt(i)) >>> 0;
    }
    return this.equipeImgFallback[hash % this.equipeImgFallback.length];
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