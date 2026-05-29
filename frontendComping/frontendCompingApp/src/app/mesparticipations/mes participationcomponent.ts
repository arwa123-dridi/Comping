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
      <div class="hero-stats" *ngIf="participations.length > 0">
        <div class="hstat">
          <span class="hstat-n">{{ participations.length }}</span>
          <span class="hstat-l">Inscriptions</span>
        </div>
        <div class="hstat-div"></div>
        <div class="hstat">
          <span class="hstat-n">{{ getPresentsCount() }}</span>
          <span class="hstat-l">Présent(e)s</span>
        </div>
      </div>
    </div>
  </div>

  <!-- Toast -->
  <div *ngIf="toastMessage" class="toast-notif"
       [class.toast-success]="toastType==='success'"
       [class.toast-error]="toastType==='error'"
       [class.toast-info]="toastType==='info'">
    {{ toastMessage }}
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
    :host {
      --vert:   #2E7D32;
      --vert-d: #1B5E20;
      --vert-l: #4CAF50;
      --accent: #FF7043;
      --navy:   #1A2B1A;
      --sky:    #0288D1;
      --jaune:  #FFE082;
      --rose:   #D32F2F;
      --bg:     #F5F5DC;
      --border: #D8E8D8;
      --muted:  #6D7F6D;
      display: block;
      font-family: 'DM Sans', system-ui, sans-serif;
    }

    .participations-page { background: var(--bg); min-height: 100vh; }

    .part-hero {
      background:
        linear-gradient(155deg, rgba(27,94,32,0.94) 0%, rgba(46,125,50,0.78) 60%, rgba(255,112,67,0.3) 100%),
        url('https://images.unsplash.com/photo-1501854140801-50d01698950b?w=1400&q=80') center/cover no-repeat;
      padding: 90px 64px 70px; color: #fff; position: relative; overflow: hidden;
    }
    .part-hero::after {
      content: '🎟️'; position: absolute; right: 40px; bottom: -10px;
      font-size: 200px; opacity: 0.06; pointer-events: none;
    }

    .hero-badge {
      display: inline-flex; align-items: center; gap: 6px;
      background: rgba(255,255,255,0.12); border: 1px solid rgba(255,255,255,0.3);
      color: rgba(255,255,255,0.9); font-size: 11px; letter-spacing: 2.5px; font-weight: 700;
      padding: 6px 16px; border-radius: 4px; margin-bottom: 22px;
    }
    .hero-title {
      font-family: 'DM Serif Display', Georgia, serif;
      font-size: clamp(32px, 4.5vw, 56px); margin-bottom: 12px; line-height: 1.08; letter-spacing: -0.02em;
    }
    .hero-accent { color: var(--jaune); font-style: italic; }
    .hero-sub { color: rgba(255,255,255,0.65); font-size: 16px; max-width: 440px; margin-bottom: 40px; }

    .hero-stats { display: flex; align-items: center; gap: 24px; }
    .hstat { display: flex; flex-direction: column; gap: 3px; }
    .hstat-n { font-family: 'DM Serif Display', serif; font-size: 32px; color: #fff; line-height: 1; }
    .hstat-l { font-size: 11px; color: rgba(255,255,255,0.5); font-weight: 600; letter-spacing: 0.5px; }
    .hstat-div { width: 1px; height: 36px; background: rgba(255,255,255,0.2); }

    .part-body { max-width: 1100px; margin: 0 auto; padding: 40px 24px 60px; }

    .state-center { text-align: center; padding: 60px 24px; color: #888; }
    .campino-spinner {
      width: 44px; height: 44px; border: 4px solid #e0ebe0;
      border-top-color: #2E7D32; border-radius: 50%;
      animation: spin 0.8s linear infinite; margin: 0 auto 16px;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .state-empty { text-align: center; padding: 60px 24px; }
    .empty-icon { font-size: 64px; margin-bottom: 16px; }
    .state-empty h3 { font-size: 22px; color: #1b2a4a; margin-bottom: 8px; }
    .state-empty p { color: #888; margin-bottom: 24px; }
    .btn-discover {
      display: inline-block; padding: 13px 32px; background: #2E7D32;
      color: #fff; border-radius: 10px; font-size: 15px; font-weight: 600;
      text-decoration: none; box-shadow: 0 6px 20px rgba(61,168,89,0.35);
      transition: all 0.2s;
    }
    .btn-discover:hover { background: var(--vert-d); transform: translateY(-2px); }

    .part-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 22px;
    }

    .part-card {
      background: #fff; border-radius: 16px; border: 1px solid var(--border); overflow: hidden;
      box-shadow: 0 4px 18px rgba(27,94,32,0.08);
      transition: transform 0.25s, box-shadow 0.25s;
      border-top: 4px solid var(--vert);
    }
    .part-card:hover { transform: translateY(-5px); box-shadow: 0 14px 36px rgba(27,94,32,0.16); }

    .part-card-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 18px 20px 0;
    }
    .part-card-icon { font-size: 36px; }

    .part-status {
      padding: 5px 14px; border-radius: 20px;
      font-size: 11px; font-weight: 700;
    }
    .status-present  { background: #e8f5e9; color: var(--vert); }
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

    @media (max-width: 900px) {
      .part-hero { padding: 70px 28px 56px; }
      .part-grid { grid-template-columns: 1fr 1fr; }
    }
    @media (max-width: 640px) {
      .part-hero { padding: 60px 18px 44px; }
      .part-grid { grid-template-columns: 1fr; }
      .hero-stats { gap: 14px; }
    }

    .toast-notif {
      position: fixed; bottom: 24px; right: 24px; z-index: 9999;
      padding: 14px 22px; border-radius: 10px;
      font-size: 14px; font-weight: 700; color: #fff; min-width: 260px;
      box-shadow: 0 10px 32px rgba(0,0,0,0.18);
      animation: toast-in 0.3s ease;
    }
    .toast-success { background: linear-gradient(135deg, #2E7D32, #1B5E20); }
    .toast-error   { background: linear-gradient(135deg, #D32F2F, #B71C1C); }
    @keyframes toast-in { from{opacity:0;transform:translateX(20px)} to{opacity:1;transform:translateX(0)} }
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

  toastMessage: string | null = null;
  toastType: 'success' | 'error' | 'info' = 'info';
  private toastTimer: any;

  showToast(msg: string, type: 'success' | 'error' | 'info' = 'info'): void {
    this.toastMessage = msg;
    this.toastType = type;
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => { this.toastMessage = null; }, 3500);
  }

  getStatutLabel(statut: string): string {
    const map: Record<string, string> = {
      PRESENT: '✅ Présent',
      INSCRIT: '📝 Inscrit',
      ABSENT:  '❌ Absent'
    };
    return map[statut] || statut || 'Inscrit';
  }

  getPresentsCount(): number {
    return this.participations.filter(p => p.statutPresence === 'PRESENT').length;
  }

  desinscrire(sortieId: string): void {
    this.showToast('⏳ Désinscription en cours…', 'info');
    const userId = localStorage.getItem('userId') || '';
    this.participationService.deleteParticipation(sortieId, userId).subscribe({
      next: () => this.loadMyParticipations(),
      next: () => { this.showToast('✅ Désinscription effectuée.', 'success'); this.loadMyParticipations(); },
      error: () => this.showToast('❌ Erreur lors de la désinscription.', 'error')
    });
  }
}