import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ParticipationService } from '../services/participation.service';
import { ParticipationDTO } from '../models/participation.model';

@Component({
  selector: 'app-mes-participations',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
<div class="participations-page">

  <!-- HERO -->
  <div class="part-hero">
    <div class="part-hero-content">
      <div class="hero-badge">🎟️ CAMPINO · Participations</div>
      <h1 class="hero-title">Mes <span class="hero-accent">Participations</span></h1>
      <p class="hero-sub">Retrouvez toutes les randonnées auxquelles vous êtes inscrit(e)</p>
    </div>
  </div>

  <!-- CORPS -->
  <div class="part-body">

    <!-- Loading -->
    <div *ngIf="loading" class="state-center">
      <div class="campino-spinner"></div>
      <p>Chargement de vos participations…</p>
    </div>

    <!-- Vide -->
    <div *ngIf="!loading && participations.length === 0" class="state-empty">
      <div class="empty-icon">🎟️</div>
      <h3>Aucune participation</h3>
      <p>Inscrivez-vous à une randonnée pour la retrouver ici.</p>
      <a routerLink="/sorties" class="btn-discover">🏔️ Trouver une randonnée</a>
    </div>

    <!-- Grille -->
    <div *ngIf="!loading && participations.length > 0" class="part-grid">
      <div *ngFor="let p of participations" class="part-card">
        <div class="part-card-header">
          <div class="part-card-icon">🏔️</div>
          <div class="part-status"
               [class.status-present]="p.statutPresence === 'PRESENT'"
               [class.status-inscrit]="p.statutPresence === 'INSCRIT'"
               [class.status-absent]="p.statutPresence === 'ABSENT'">
            {{ getStatutLabel(p.statutPresence) }}
          </div>
        </div>
        <div class="part-card-body">
          <h4 class="part-titre">{{ p.sortieTitre || 'Randonnée' }}</h4>
          <p class="part-date">
            📅 Inscrit le {{ p.dateInscription | date:'dd/MM/yyyy' }}
          </p>
          <div class="part-actions">
            <a [routerLink]="['/sorties', p.sortieId]" class="btn-voir-sortie">Voir la randonnée →</a>
            <button class="btn-desinscrire" (click)="desinscrire(p.sortieId)">
              ❌ Se désinscrire
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</div>
  `,
  styles: [`
    @import url('https://fonts.googleapis.com/css2?family=DM+Serif+Display:ital@0;1&family=DM+Sans:wght@300;400;500;600;700&display=swap');

    :root { --vert: #3da859; --bleu: #1b2a4a; --ciel: #1f73a3; --rose: #e02f2f; }

    .participations-page { font-family: 'DM Sans', sans-serif; background: #f4f7f4; min-height: 100vh; }

    .part-hero {
      background: linear-gradient(150deg, #1b2a4a 0%, #1f73a3 100%);
      padding: 70px 60px 55px; color: #fff; position: relative; overflow: hidden;
    }
    .part-hero::after {
      content: '🎟️'; position: absolute; right: 40px; bottom: -10px;
      font-size: 180px; opacity: 0.07; pointer-events: none;
    }

    .hero-badge {
      display: inline-block;
      background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.25);
      color: rgba(255,255,255,0.8); font-size: 11px; letter-spacing: 3px; font-weight: 600;
      padding: 6px 18px; border-radius: 4px; margin-bottom: 22px;
    }
    .hero-title {
      font-family: 'DM Serif Display', serif;
      font-size: clamp(32px, 4vw, 52px); margin-bottom: 12px; line-height: 1.1;
    }
    .hero-accent { color: #f5e642; font-style: italic; }
    .hero-sub { color: rgba(255,255,255,0.6); font-size: 15px; }

    .part-body { max-width: 1100px; margin: 0 auto; padding: 36px 24px 60px; }

    .state-center { text-align: center; padding: 60px 24px; color: #888; }
    .campino-spinner {
      width: 44px; height: 44px; border: 4px solid #e0ebe0;
      border-top-color: #3da859; border-radius: 50%;
      animation: spin 0.8s linear infinite; margin: 0 auto 16px;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .state-empty { text-align: center; padding: 60px 24px; }
    .empty-icon { font-size: 64px; margin-bottom: 16px; }
    .state-empty h3 { font-size: 22px; color: #1b2a4a; margin-bottom: 8px; }
    .state-empty p { color: #888; margin-bottom: 24px; }
    .btn-discover {
      display: inline-block; padding: 13px 32px; background: #3da859;
      color: #fff; border-radius: 10px; font-size: 15px; font-weight: 600;
      text-decoration: none; box-shadow: 0 6px 20px rgba(61,168,89,0.35);
      transition: all 0.2s;
    }
    .btn-discover:hover { background: #2d8a45; transform: translateY(-2px); }

    .part-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 22px;
    }

    .part-card {
      background: #fff; border-radius: 16px; overflow: hidden;
      box-shadow: 0 4px 18px rgba(0,0,0,0.07);
      transition: transform 0.25s, box-shadow 0.25s;
      border-top: 4px solid #3da859;
    }
    .part-card:hover { transform: translateY(-5px); box-shadow: 0 14px 36px rgba(0,0,0,0.11); }

    .part-card-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 18px 20px 0;
    }
    .part-card-icon { font-size: 36px; }

    .part-status {
      padding: 5px 14px; border-radius: 20px;
      font-size: 11px; font-weight: 700;
    }
    .status-present  { background: #e8f5e9; color: #3da859; }
    .status-inscrit  { background: #e3f2fd; color: #1f73a3; }
    .status-absent   { background: #ffeaea; color: #e02f2f; }

    .part-card-body { padding: 14px 20px 20px; }
    .part-titre { font-size: 17px; font-weight: 700; color: #1b2a4a; margin-bottom: 8px; }
    .part-date { font-size: 13px; color: #999; margin-bottom: 16px; }

    .part-actions { display: flex; gap: 8px; flex-direction: column; }

    .btn-voir-sortie {
      display: block; text-align: center; padding: 10px;
      background: transparent; border: 2px solid #1f73a3;
      color: #1f73a3; border-radius: 9px; font-size: 13px; font-weight: 700;
      text-decoration: none; transition: all 0.2s;
    }
    .btn-voir-sortie:hover { background: #1f73a3; color: #fff; }

    .btn-desinscrire {
      padding: 10px; background: transparent;
      border: 2px solid #e02f2f; color: #e02f2f;
      border-radius: 9px; font-size: 13px; font-weight: 700;
      cursor: pointer; transition: all 0.2s; font-family: 'DM Sans', sans-serif;
    }
    .btn-desinscrire:hover { background: #e02f2f; color: #fff; }

    @media (max-width: 768px) {
      .part-hero { padding: 60px 24px 44px; }
      .part-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class MesParticipationsComponent implements OnInit {
  participations: ParticipationDTO[] = [];
  loading = false;

  constructor(private participationService: ParticipationService) {}

  ngOnInit(): void {
    this.loadMyParticipations();
  }

  loadMyParticipations(): void {
    this.loading = true;
    this.participationService.getMyParticipations().subscribe({
      next: (data) => { this.participations = data || []; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  getStatutLabel(statut: string): string {
    const map: Record<string, string> = {
      PRESENT: '✅ Présent',
      INSCRIT: '📝 Inscrit',
      ABSENT:  '❌ Absent'
    };
    return map[statut] || statut || 'Inscrit';
  }

  desinscrire(sortieId: string): void {
    if (!confirm('Confirmer la désinscription ?')) return;
    const userId = localStorage.getItem('userId') || '';
    this.participationService.deleteParticipation(sortieId, userId).subscribe({
      next: () => this.loadMyParticipations(),
      error: () => alert('Erreur lors de la désinscription.')
    });
  }
}