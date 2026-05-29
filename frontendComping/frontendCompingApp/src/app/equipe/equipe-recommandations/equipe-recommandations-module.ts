import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { EquipeService } from '../../services/equipe.service';
import { EquipeResponse } from '../../models/equipe.model';
import { EquipeScoreDTO } from '../../models/equipe-score.model';

@Component({
  selector: 'app-equipe-recommandations',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './equipe-recommandations.component.html',
  styleUrls: ['./equipe-recommandations.component.css']
})
export class EquipeRecommandationsComponent implements OnInit {
  @Input() allEquipes: EquipeResponse[] = [];

  recommandations: EquipeResponse[] = [];
  loading = false;
  toastMessage: string | null = null;
  toastType: 'success' | 'error' | 'info' = 'info';
  private toastTimer: any;

  showToast(msg: string, type: 'success' | 'error' | 'info' = 'info'): void {
    this.toastMessage = msg;
    this.toastType = type;
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => { this.toastMessage = null; }, 3500);
  }

  error: string | null = null;
  userId: string = '';
  hasHistory = false;

  private readonly images: string[] = [
    'https://images.unsplash.com/photo-1533240332313-0db3b4591e1b?w=500&h=250&fit=crop',
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=500&h=250&fit=crop',
    'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=500&h=250&fit=crop'
  ];

  constructor(
    private equipeService: EquipeService,
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

    this.equipeService.getRecommandationsEquipes(this.userId).subscribe({
      next: (scores: EquipeScoreDTO[]) => {
        this.recommandations = scores.map(score => score.equipe);
        this.hasHistory = scores.length > 0;
        this.loading = false;
      },
      error: (err) => {
        console.warn('Backend recommandations équipes indisponible, fallback local', err);
        // Fallback local : équipes avec places disponibles
        this.recommandations = this.getEquipesDisponibles();
        this.hasHistory = false;
        this.loading = false;
      }
    });
  }

  // Fallback local : équipes qui ont encore des places libres
  private getEquipesDisponibles(): EquipeResponse[] {
    return this.allEquipes.filter(eq => 
      eq.membres && eq.nbMembresMax && eq.membres.length < eq.nbMembresMax
    ).slice(0, 4);
  }

  getImageUrl(equipe: EquipeResponse): string {
    const niveau = equipe.niveau || 'Tous niveaux';
    let hash = 0;
    for (let i = 0; i < niveau.length; i++) {
      hash = (hash + niveau.charCodeAt(i)) % this.images.length;
    }
    return this.images[hash];
  }

  getNiveauLabel(niveau?: string): string {
    const map: Record<string, string> = {
      'Débutant': '🥾 Débutant',
      'Intermédiaire': '🧗 Intermédiaire',
      'Avancé': '⛰️ Avancé',
      'Tous niveaux': '🌿 Tous niveaux'
    };
    return map[niveau || 'Tous niveaux'] || niveau || 'Tous niveaux';
  }

  getMembreCount(equipe: EquipeResponse): number {
    return equipe.membres?.length || 0;
  }

  getPlacesLibres(equipe: EquipeResponse): number {
    const max = equipe.nbMembresMax || 0;
    const actuel = this.getMembreCount(equipe);
    return Math.max(0, max - actuel);
  }

  viewDetail(id: string): void {
    this.router.navigate(['/equipes', id]);
  }

  rejoindre(equipeId: string, event: Event): void {
    event.stopPropagation();
    if (!this.userId) {
      this.router.navigate(['/login']);
      return;
    }
    const userNom = localStorage.getItem('userNom') || 'Utilisateur';
    this.equipeService.ajouterMembre(equipeId, this.userId, userNom).subscribe({
      next: () => {
        this.showToast('✅ Vous avez rejoint l\'équipe !', 'success');
        this.loadRecommandations();
      },
      error: (err) => {
        if (err.status === 409) this.showToast('ℹ️ Vous êtes déjà membre.', 'info');
        else if (err.status === 400) this.showToast('⛔ Équipe complète.', 'error');
        else this.showToast('❌ ' + (err.error?.message || 'Erreur.'), 'error');
      }
    });
  }

  trackById(_: number, item: EquipeResponse): string {
    return item.id;
  }
}