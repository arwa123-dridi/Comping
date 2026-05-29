// src/app/dashboard/dashboard-organizer/dashboard-organizer.component.ts
// ✅ COMPLÉTÉ : liens checklist-ia, planning, recommandations + stats
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';

@Component({
  selector: 'app-dashboard-organizer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
<div class="org-dash">

  <!-- HEADER -->
  <div class="org-header">
    <div class="org-header-left">
      <div class="org-avatar">{{ getInitiales() }}</div>
      <div>
        <h1>Dashboard Organisateur</h1>
        <p class="org-sub">Bonjour {{ userName }} 👋</p>
      </div>
    </div>
    <div class="org-header-actions">
      <button routerLink="/admin/sorties/create" class="btn-create">+ Nouvelle sortie</button>
      <button routerLink="/admin/equipes/create" class="btn-create btn-create--sec">+ Nouvelle équipe</button>
    </div>
  </div>

  <!-- STATS -->
  <div class="org-stats">
    <div class="org-stat-card org-stat--green" routerLink="/admin/sorties">
      <div class="org-stat-val">{{ totalSortiesCrees }}</div>
      <div class="org-stat-label">Sorties créées</div>
      <div class="org-stat-sub">{{ sortiesAVenir }} à venir</div>
    </div>
    <div class="org-stat-card org-stat--blue" routerLink="/admin/equipes">
      <div class="org-stat-val">{{ totalEquipes }}</div>
      <div class="org-stat-label">Équipes gérées</div>
      <div class="org-stat-sub">{{ totalMembres }} membres au total</div>
    </div>
    <div class="org-stat-card org-stat--amber">
      <div class="org-stat-val">{{ totalParticipants }}</div>
      <div class="org-stat-label">Participants inscrits</div>
      <div class="org-stat-sub">toutes sorties confondues</div>
    </div>
  </div>

  <!-- ACTIONS RAPIDES IA -->
  <div class="org-ia-section">
    <h2 class="org-section-title">🤖 Outils IA</h2>
    <div class="org-ia-grid">
      <div class="org-ia-card" routerLink="/admin/checklist-ia">
        <span class="org-ia-icon">🎒</span>
        <div>
          <h4>Checklist sécurité</h4>
          <p>Générer une liste d'équipements selon la météo et la difficulté</p>
        </div>
        <span class="org-ia-arrow">→</span>
      </div>
      <div class="org-ia-card" routerLink="/admin/planning">
        <span class="org-ia-icon">📅</span>
        <div>
          <h4>Planning IA</h4>
          <p>Voir un planning optimisé basé sur votre historique</p>
        </div>
        <span class="org-ia-arrow">→</span>
      </div>
      <div class="org-ia-card" routerLink="/admin/sorties">
        <span class="org-ia-icon">✨</span>
        <div>
          <h4>Recommandations</h4>
          <p>Sorties et équipes recommandées pour vos prochaines aventures</p>
        </div>
        <span class="org-ia-arrow">→</span>
      </div>
    </div>
  </div>

  <!-- SORTIES RÉCENTES -->
  <div class="org-recent" *ngIf="mesSortiesCreees.length > 0">
    <h2 class="org-section-title">Mes sorties récentes</h2>
    <div class="org-sorties-grid">
      <div class="org-sortie-card" *ngFor="let s of mesSortiesCreees.slice(0,3)"
           [routerLink]="['/admin/sorties', s.id]">
        <div class="org-sortie-diff" [ngClass]="getDiffClass(s.difficulte)">{{ s.difficulte }}</div>
        <h4>{{ s.titre }}</h4>
        <p class="org-sortie-lieu">📍 {{ s.lieuDepart }}</p>
        <p class="org-sortie-date">🗓 {{ formatDate(s.dateDebut) }}</p>
        <div class="org-sortie-places">
          <div class="org-places-bar">
            <div class="org-places-fill"
                 [style.width.%]="((s.participantIds?.length ?? s.nombreParticipants ?? 0) / s.capaciteMax) * 100">
            </div>
          </div>
          <span>{{ s.participantIds?.length ?? s.nombreParticipants ?? 0 }} / {{ s.capaciteMax }}</span>
        </div>
        <div class="org-sortie-actions">
          <button [routerLink]="['/admin/sorties/edit', s.id]" class="btn-sm btn-sm--edit">✏️ Modifier</button>
          <button (click)="deleteSortie(s, $event)" class="btn-sm btn-sm--del">🗑</button>
        </div>
      </div>
    </div>
    <a routerLink="/admin/sorties" class="org-voir-tout">Voir toutes mes sorties →</a>
  </div>

  <!-- ÉTAT VIDE -->
  <div class="org-empty" *ngIf="!loading && mesSortiesCreees.length === 0">
    <div class="org-empty-icon">🏔️</div>
    <h3>Aucune sortie créée</h3>
    <p>Commencez par créer votre première randonnée !</p>
    <button routerLink="/admin/sorties/create" class="btn-create">+ Créer une sortie</button>
  </div>

</div>
  `,
  styles: [`
    .org-dash { padding: 28px 32px; max-width: 1200px; }
    .org-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:28px; flex-wrap:wrap; gap:16px; }
    .org-header-left { display:flex; align-items:center; gap:16px; }
    .org-avatar { width:52px; height:52px; border-radius:50%; background:linear-gradient(135deg,#2d6a4f,#1b4332); color:#fff; display:flex; align-items:center; justify-content:center; font-size:18px; font-weight:700; }
    .org-header h1 { font-size:22px; font-weight:600; color:#1a2e1a; margin:0; }
    .org-sub { color:#666; font-size:14px; margin:2px 0 0; }
    .org-header-actions { display:flex; gap:10px; }
    .btn-create { padding:10px 20px; background:#2d6a4f; color:#fff; border:none; border-radius:10px; font-size:14px; font-weight:600; cursor:pointer; transition:all .2s; }
    .btn-create:hover { background:#1b4332; }
    .btn-create--sec { background:#fff; color:#2d6a4f; border:2px solid #2d6a4f; }
    .btn-create--sec:hover { background:#f0faf4; }

    .org-stats { display:grid; grid-template-columns:repeat(3,1fr); gap:20px; margin-bottom:32px; }
    .org-stat-card { padding:24px; border-radius:16px; cursor:pointer; transition:transform .2s; }
    .org-stat-card:hover { transform:translateY(-3px); }
    .org-stat--green { background:linear-gradient(135deg,#e8f5e9,#c8e6c9); border:1px solid #a5d6a7; }
    .org-stat--blue  { background:linear-gradient(135deg,#e3f2fd,#bbdefb); border:1px solid #90caf9; }
    .org-stat--amber { background:linear-gradient(135deg,#fff8e1,#ffecb3); border:1px solid #ffe082; }
    .org-stat-val { font-size:36px; font-weight:700; color:#1a2e1a; }
    .org-stat-label { font-size:14px; font-weight:600; color:#2d6a4f; margin:4px 0 2px; }
    .org-stat-sub { font-size:12px; color:#666; }

    .org-ia-section { margin-bottom:32px; }
    .org-section-title { font-size:16px; font-weight:600; color:#1a2e1a; margin-bottom:16px; }
    .org-ia-grid { display:flex; flex-direction:column; gap:12px; }
    .org-ia-card { display:flex; align-items:center; gap:16px; padding:18px 20px; background:#fff; border:1px solid #e8f5e9; border-radius:12px; cursor:pointer; transition:all .2s; }
    .org-ia-card:hover { border-color:#2d6a4f; box-shadow:0 4px 16px rgba(45,106,79,0.1); transform:translateX(4px); }
    .org-ia-icon { font-size:28px; flex-shrink:0; }
    .org-ia-card h4 { font-size:14px; font-weight:600; color:#1a2e1a; margin:0 0 4px; }
    .org-ia-card p { font-size:12px; color:#666; margin:0; }
    .org-ia-arrow { margin-left:auto; color:#2d6a4f; font-size:18px; font-weight:600; }

    .org-recent { margin-bottom:32px; }
    .org-sorties-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; margin-bottom:16px; }
    .org-sortie-card { background:#fff; border:1px solid #e8f5e9; border-radius:14px; padding:20px; cursor:pointer; transition:all .2s; }
    .org-sortie-card:hover { border-color:#2d6a4f; box-shadow:0 4px 16px rgba(45,106,79,0.1); }
    .org-sortie-diff { display:inline-block; padding:3px 10px; border-radius:12px; font-size:10px; font-weight:700; letter-spacing:1px; margin-bottom:10px; }
    .diff-facile { background:#e8f5e9; color:#1b5e20; }
    .diff-moyen   { background:#fff8e1; color:#e65100; }
    .diff-difficile { background:#ffebee; color:#b71c1c; }
    .org-sortie-card h4 { font-size:14px; font-weight:600; color:#1a2e1a; margin:0 0 8px; }
    .org-sortie-lieu, .org-sortie-date { font-size:12px; color:#888; margin:2px 0; }
    .org-sortie-places { margin:12px 0; }
    .org-sortie-places span { font-size:11px; color:#888; }
    .org-places-bar { height:4px; background:#e8f5e9; border-radius:2px; overflow:hidden; margin-bottom:4px; }
    .org-places-fill { height:100%; background:#2d6a4f; border-radius:2px; }
    .org-sortie-actions { display:flex; gap:8px; margin-top:12px; }
    .btn-sm { padding:6px 12px; border:none; border-radius:8px; font-size:12px; cursor:pointer; transition:all .2s; }
    .btn-sm--edit { background:#e8f5e9; color:#2d6a4f; flex:1; }
    .btn-sm--edit:hover { background:#c8e6c9; }
    .btn-sm--del { background:#ffebee; color:#c62828; }
    .btn-sm--del:hover { background:#ffcdd2; }
    .org-voir-tout { color:#2d6a4f; text-decoration:none; font-size:13px; font-weight:600; }

    .org-empty { text-align:center; padding:60px 20px; }
    .org-empty-icon { font-size:56px; margin-bottom:16px; }
    .org-empty h3 { font-size:18px; color:#1a2e1a; margin-bottom:8px; }
    .org-empty p { color:#666; font-size:14px; margin-bottom:20px; }
  `]
})
export class DashboardOrganizerComponent implements OnInit {
  userName = '';
  userId: string | null = null;
  loading = true;

  totalSortiesCrees = 0;
  totalEquipes = 0;
  totalParticipants = 0;
  totalMembres = 0;
  sortiesAVenir = 0;
  mesSortiesCreees: SortieResponse[] = [];

  constructor(
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userId   = localStorage.getItem('userId');
    this.userName = localStorage.getItem('userNom') || localStorage.getItem('userEmail')?.split('@')[0] || 'Organisateur';
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.sortieService.getAllSorties().subscribe(data => {
      const now = new Date();
      this.mesSortiesCreees = data.filter(s => String(s.organisateurId) === String(this.userId));
      this.totalSortiesCrees = this.mesSortiesCreees.length;
      this.sortiesAVenir = this.mesSortiesCreees.filter(s => new Date(s.dateDebut) > now).length;
      this.totalParticipants = this.mesSortiesCreees.reduce((acc, s) =>
        acc + (s.participantIds?.length ?? s.nombreParticipants ?? 0), 0);
      this.loading = false;
    });
    this.equipeService.getAllEquipes().subscribe(data => {
      const mesEquipes = data.filter(e => String(e.organisateurId) === String(this.userId));
      this.totalEquipes = mesEquipes.length;
      this.totalMembres = mesEquipes.reduce((acc, e) => acc + (e.nbMembresActuels ?? e.membres?.length ?? 0), 0);
    });
  }

  deleteSortie(s: SortieResponse, event: Event): void {
    event.stopPropagation();
    if (!confirm(`Supprimer "${s.titre}" ?`)) return;
    this.sortieService.deleteSortie(String(s.id)).subscribe({
      next: () => this.loadData()
    });
  }

  getInitiales(): string { return (this.userName || '?').split(' ').map(w => w[0]).join('').toUpperCase().slice(0,2); }
  getDiffClass(d: string): string { return { FACILE:'diff-facile', MOYEN:'diff-moyen', DIFFICILE:'diff-difficile' }[d] || ''; }
  formatDate(d: any): string { return new Date(d).toLocaleDateString('fr-FR', { day:'numeric', month:'short', year:'numeric' }); }
}
