import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EquipeService } from '../../services/equipe.service';
import { EquipeResponse } from '../../models/equipe.model';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';

@Component({
  selector: 'app-equipe-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './equipe-detail.component.html',
  styleUrls: ['./equipe-detail.component.css']
})
export class EquipeDetailComponent implements OnInit {
  equipe: EquipeResponse | null = null;
  sorties: SortieResponse[] = [];
  loading = false;
  error: string | null = null;

  private readonly heroImages = [
    'https://images.unsplash.com/photo-1533240332313-0db3b4591e1b?w=1200&q=80',
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&q=80',
    'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1200&q=80'
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private equipeService: EquipeService,
    private sortieService: SortieService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadEquipe(id);
    else this.router.navigate(['/equipes']);
  }

  loadEquipe(id: string): void {
    this.loading = true;
    this.equipeService.getEquipeById(id).subscribe({
      next: (data) => {
        this.equipe = data;
        this.loadSorties(id);
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger l\'équipe.';
        this.loading = false;
      }
    });
  }

  loadSorties(equipeId: string): void {
    this.sortieService.getAllSorties().subscribe({
      next: (data) => {
        this.sorties = data.filter(s => s.equipeId === equipeId);
      },
      error: () => { /* silencieux */ }
    });
  }

  // ── Checks ───────────────────────────────────────────────

  isOrganisateur(): boolean {
    const userId = localStorage.getItem('userId');
    return !!userId && this.equipe?.organisateurId === userId;
  }

  isMembre(): boolean {
    const userId = localStorage.getItem('userId');
    return !!userId && !!this.equipe?.membres?.some(m => m.id === userId);
  }

  // ── Helpers ───────────────────────────────────────────────

  getHeroImage(): string {
    if (!this.equipe) return this.heroImages[0];
    return this.heroImages[this.equipe.nom.length % this.heroImages.length];
  }

  getInitiales(nom: string): string {
    if (!nom) return '?';
    return nom.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }

  // ── Actions ───────────────────────────────────────────────

  rejoindre(): void {
    if (!this.equipe) return;
    const userId = localStorage.getItem('userId');
    const userNom = localStorage.getItem('userNom') || 'Utilisateur';
    if (!userId) { this.router.navigate(['/login']); return; }

    this.equipeService.ajouterMembre(this.equipe.id, userId, userNom).subscribe({
      next: () => this.loadEquipe(this.equipe!.id),
      error: (err) => alert(err.error?.message || 'Erreur lors de l\'adhésion.')
    });
  }

  quitter(): void {
    if (!this.equipe || !confirm('Voulez-vous vraiment quitter cette équipe ?')) return;
    const userId = localStorage.getItem('userId');
    if (!userId) return;

    this.equipeService.retirerMembre(this.equipe.id, userId).subscribe({
      next: () => { this.loadEquipe(this.equipe!.id); alert('✅ Vous avez quitté l\'équipe.'); },
      error: (err) => alert(err.error?.message || 'Erreur.')
    });
  }
}