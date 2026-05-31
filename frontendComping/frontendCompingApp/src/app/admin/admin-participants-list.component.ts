// src/app/admin/admin-participants-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { SortieService } from '../services/sortie.service';
import { SortieResponse } from '../models/sortie.model';
import { ParticipationDTO } from '../models/participation.model';

interface ParticipantRow extends ParticipationDTO {
  sortieTitle: string;
  sortieDateDebut: string;
  sortieDifficulte: string;
  sortieRegion: string;
  sortieOrganisateur: string;
}

@Component({
  selector: 'app-admin-participants-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="admin-list-page">

      <div class="page-header">
        <div>
          <h2>📋 Gestion des Participants</h2>
          <span class="subtitle">Vue globale de toutes les inscriptions</span>
        </div>
        <div class="header-stats">
          <div class="hstat">
            <span class="hstat-num">{{ allRows.length }}</span>
            <span class="hstat-lbl">Total inscriptions</span>
          </div>
          <div class="hstat">
            <span class="hstat-num">{{ getUniquesCount() }}</span>
            <span class="hstat-lbl">Participants uniques</span>
          </div>
          <div class="hstat">
            <span class="hstat-num">{{ getSortiesAvecPart() }}</span>
            <span class="hstat-lbl">Sorties concernées</span>
          </div>
        </div>
      </div>

      <div class="filters-bar">
        <input type="text" [(ngModel)]="searchTerm" placeholder="🔍 Rechercher par nom, email, sortie..."
               (input)="applyFilters()" class="search-input"/>
        <select [(ngModel)]="filterSortie" (change)="applyFilters()" class="filter-select">
          <option value="">Toutes les sorties</option>
          <option *ngFor="let s of sortiesOptions" [value]="s.id">{{ s.titre }}</option>
        </select>
        <select [(ngModel)]="filterDifficulte" (change)="applyFilters()" class="filter-select">
          <option value="">Toute difficulté</option>
          <option value="FACILE">Facile</option>
          <option value="MOYEN">Moyen</option>
          <option value="DIFFICILE">Difficile</option>
        </select>
        <select [(ngModel)]="filterStatut" (change)="applyFilters()" class="filter-select">
          <option value="">Tous statuts</option>
          <option value="PRESENT">Présent</option>
          <option value="ABSENT">Absent</option>
          <option value="EN_ATTENTE">En attente</option>
        </select>
        <button class="btn-reset" (click)="resetFilters()">⟳ Réinitialiser</button>
        <span class="results-count">{{ filteredRows.length }} résultat(s)</span>
      </div>

      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        Chargement des participants...
      </div>

      <div *ngIf="errorMsg && !loading" class="error-banner">
        ⚠️ {{ errorMsg }}
      </div>

      <div class="table-container" *ngIf="!loading && !errorMsg">
        <table>
          <thead>
            <tr>
              <th>Participant</th>
              <th>Email</th>
              <th>Sortie</th>
              <th>Difficulté</th>
              <th>Région</th>
              <th>Date sortie</th>
              <th>Inscrit le</th>
              <th>Statut présence</th>
              <th>Checklist</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let p of filteredRows">
              <td>
                <div class="participant-cell">
                  <div class="avatar" [style.background]="getAvatarColor(p.utilisateurNom)">
                    {{ getInitiales(p.utilisateurNom) }}
                  </div>
                  <span class="participant-nom">
                    {{ p.utilisateurPrenom || '' }} {{ p.utilisateurNom || '???' }}
                  </span>
                </div>
              </td>
              <td class="email-cell">{{ p.utilisateurEmail || '???' }}</td>
              <td>
                <a class="sortie-link" [routerLink]="['/admin/sorties', p.sortieId]">
                  {{ p.sortieTitle || 'Sortie inconnue' }}
                </a>
              </td>
              <td>
                <span [class]="getDifficultyClass(p.sortieDifficulte)">
                  {{ p.sortieDifficulte || 'N/A' }}
                </span>
              </td>
              <td>{{ p.sortieRegion || '???' }}</td>
              <td>{{ formatDate(p.sortieDateDebut) }}</td>
              <td>{{ formatDate(p.dateInscription) }}</td>
              <td>
                <select [ngModel]="p.statutPresence || 'EN_ATTENTE'"
                        (ngModelChange)="updateStatut(p, $event)"
                        class="statut-select"
                        [class]="'statut-' + (p.statutPresence || 'EN_ATTENTE').toLowerCase()">
                  <option value="EN_ATTENTE">⏳ En attente</option>
                  <option value="PRESENT">✅ Présent</option>
                  <option value="ABSENT">❌ Absent</option>
                </select>
              </td>
              <td>
                <span [class]="p.aValideChecklist ? 'badge badge-ok' : 'badge badge-pending'">
                  {{ p.aValideChecklist ? '✅ Validée' : '⏳ Non faite' }}
                </span>
              </td>
              <td class="actions">
                <button class="btn-delete" (click)="confirmDesinscription(p)">🗑️</button>
              </td>
            </tr>
            <tr *ngIf="filteredRows.length === 0">
              <td colspan="10" class="empty">
                <div class="empty-state">
                  <span>👥</span>
                  <p *ngIf="allRows.length === 0">Aucune inscription trouvée.</p>
                  <p *ngIf="allRows.length > 0">Aucun résultat pour ces filtres.</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="modal-overlay" *ngIf="rowToDelete" (click)="cancelDelete()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>Confirmer la désinscription</h3>
          <p>Désinscrire <strong>{{ rowToDelete.utilisateurPrenom }} {{ rowToDelete.utilisateurNom }}</strong>
             de <strong>{{ rowToDelete.sortieTitle }}</strong> ?</p>
          <div class="modal-actions">
            <button class="btn-cancel" (click)="cancelDelete()">Annuler</button>
            <button class="btn-confirm-delete" (click)="executeDesinscription()">Désinscrire</button>
          </div>
        </div>
      </div>

    </div>
  `,
  styles: [`
    .admin-list-page { padding: 24px; font-family: 'DM Sans', sans-serif; }
    .page-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:20px; flex-wrap:wrap; gap:16px; }
    .page-header h2 { color:#2d6a4f; margin:0 0 4px; font-size:22px; }
    .subtitle { font-size:13px; color:#888; }
    .header-stats { display:flex; gap:16px; }
    .hstat { background:white; border:1px solid #e5e7eb; border-radius:12px; padding:10px 18px; text-align:center; min-width:90px; }
    .hstat-num { display:block; font-size:22px; font-weight:800; color:#2d6a4f; }
    .hstat-lbl { font-size:10px; color:#888; font-weight:600; }
    .filters-bar { display:flex; gap:10px; align-items:center; margin-bottom:16px; flex-wrap:wrap; }
    .search-input { flex:2; min-width:220px; padding:9px 14px; border:1px solid #ddd; border-radius:8px; font-size:13px; }
    .search-input:focus { outline:none; border-color:#2d6a4f; }
    .filter-select { padding:9px 12px; border:1px solid #ddd; border-radius:8px; font-size:13px; background:white; }
    .btn-reset { padding:9px 14px; border:1px solid #e5e7eb; background:white; border-radius:8px; font-size:12px; cursor:pointer; color:#888; }
    .btn-reset:hover { background:#fef2f2; color:#d9534f; border-color:#d9534f; }
    .results-count { color:#888; font-size:12px; margin-left:auto; }
    .loading { display:flex; align-items:center; gap:12px; justify-content:center; padding:48px; color:#666; }
    .spinner { width:22px; height:22px; border:2px solid #e5e7eb; border-top-color:#2d6a4f; border-radius:50%; animation:spin 0.7s linear infinite; }
    @keyframes spin { to { transform:rotate(360deg); } }
    .error-banner { background:#fff3f3; border:1px solid #fca5a5; border-radius:8px; padding:12px 16px; color:#b91c1c; font-size:13px; margin-bottom:12px; }
    .table-container { overflow-x:auto; border-radius:14px; }
    table { width:100%; border-collapse:collapse; background:white; border-radius:14px; overflow:hidden; box-shadow:0 2px 12px rgba(0,0,0,0.06); }
    th { background:#f8fafc; padding:12px 14px; text-align:left; font-size:11px; font-weight:700; color:#64748b; text-transform:uppercase; letter-spacing:0.04em; border-bottom:2px solid #e9ecef; white-space:nowrap; }
    td { padding:12px 14px; border-bottom:1px solid #f1f5f9; font-size:13px; }
    tr:hover td { background:#f8fff8; }
    .participant-cell { display:flex; align-items:center; gap:10px; }
    .avatar { width:34px; height:34px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:11px; font-weight:800; color:white; flex-shrink:0; }
    .participant-nom { font-weight:600; color:#1e293b; white-space:nowrap; }
    .email-cell { color:#64748b; font-size:12px; }
    .sortie-link { color:#2d6a4f; font-weight:600; cursor:pointer; font-size:12px; text-decoration:none; }
    .sortie-link:hover { text-decoration:underline; }
    .badge { padding:3px 10px; border-radius:10px; font-size:11px; font-weight:700; display:inline-block; }
    .diff-facile    { background:#dcfce7; color:#15803d; }
    .diff-moyen     { background:#fef3c7; color:#b45309; }
    .diff-difficile { background:#fee2e2; color:#b91c1c; }
    .badge-ok      { background:#dcfce7; color:#15803d; }
    .badge-pending { background:#f1f5f9; color:#94a3b8; }
    .statut-select { border:1px solid #e5e7eb; border-radius:8px; padding:4px 8px; font-size:12px; font-weight:600; cursor:pointer; background:white; }
    .statut-present    { border-color:#86efac; background:#f0fdf4; color:#15803d; }
    .statut-absent     { border-color:#fca5a5; background:#fff5f5; color:#b91c1c; }
    .statut-en_attente { border-color:#fcd34d; background:#fffbeb; color:#b45309; }
    .actions { white-space:nowrap; }
    .btn-delete { border:none; background:none; cursor:pointer; font-size:1.1rem; padding:4px 8px; border-radius:6px; }
    .btn-delete:hover { background:#fde8e8; }
    .empty-state { display:flex; flex-direction:column; align-items:center; gap:8px; padding:32px; color:#94a3b8; }
    .empty-state span { font-size:32px; }
    .empty-state p { font-size:14px; margin:0; }
    .modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,0.45); display:flex; align-items:center; justify-content:center; z-index:1000; }
    .modal { background:white; border-radius:14px; padding:28px; max-width:420px; width:90%; }
    .modal h3 { margin:0 0 12px; color:#d9534f; font-size:17px; }
    .modal p  { font-size:14px; color:#475569; line-height:1.6; margin:0; }
    .modal-actions { display:flex; gap:12px; justify-content:flex-end; margin-top:20px; }
    .btn-cancel { padding:9px 20px; border:1px solid #ddd; border-radius:8px; background:white; cursor:pointer; font-size:13px; }
    .btn-confirm-delete { padding:9px 20px; border:none; border-radius:8px; background:#d9534f; color:white; cursor:pointer; font-weight:700; font-size:13px; }
    .btn-confirm-delete:hover { background:#c0392b; }
  `]
})
export class AdminParticipantsListComponent implements OnInit {

  allRows: ParticipantRow[] = [];
  filteredRows: ParticipantRow[] = [];
  sortiesOptions: SortieResponse[] = [];
  loading = false;
  errorMsg = '';
  searchTerm = '';
  filterSortie = '';
  filterDifficulte = '';
  filterStatut = '';
  rowToDelete: ParticipantRow | null = null;

  constructor(private sortieService: SortieService, private http: HttpClient) {}

  ngOnInit(): void { this.loadAll(); }

  loadAll(): void {
    this.loading = true;
    this.errorMsg = '';

    this.sortieService.getAllSorties().subscribe({
      next: (sorties) => {
        if (sorties.length === 0) {
          this.loading = false;
          this.errorMsg = 'Aucune sortie trouvée.';
          return;
        }
        this.sortiesOptions = sorties;
        let completed = 0;
        const rows: ParticipantRow[] = [];

        sorties.forEach(sortie => {
          const url = `http://localhost:8087/api/sorties/${sortie.id}/participants`;
          this.http.get<ParticipationDTO[]>(url, {
            headers: new HttpHeaders({ Authorization: `Bearer ${localStorage.getItem('authToken') ?? ''}` })
          }).subscribe({
            next: (participants) => {
              participants.forEach(p => {
                rows.push({
                  ...p,
                  sortieId: String(sortie.id),
                  sortieTitle: sortie.titre,
                  sortieDateDebut: String(sortie.dateDebut),
                  sortieDifficulte: sortie.difficulte,
                  sortieRegion: sortie.region ?? '',
                  sortieOrganisateur: sortie.organisateurNom ?? '',
                });
              });
              completed++;
              if (completed === sorties.length) {
                this.allRows = rows.sort((a, b) =>
                  new Date(b.dateInscription ?? 0).getTime() - new Date(a.dateInscription ?? 0).getTime()
                );
                // ✅ Afficher les données même si certaines sorties ont échoué
                this.errorMsg = this.allRows.length === 0
                  ? 'Aucune inscription trouvée.'
                  : '';
                this.applyFilters();
                this.loading = false;
              }
            },
            error: (err) => {
              console.error(`Erreur sortie ${sortie.id}:`, err.status);
              completed++;
              if (completed === sorties.length) {
                this.allRows = rows;
                // ✅ Ne pas bloquer si on a des données
                this.errorMsg = this.allRows.length === 0
                  ? 'Aucune inscription trouvée.'
                  : '';
                this.applyFilters();
                this.loading = false;
              }
            }
          });
        });
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'Impossible de charger les sorties. Vérifiez que le backend est démarré.';
      }
    });
  }

  applyFilters(): void {
    const term = this.searchTerm.toLowerCase();
    this.filteredRows = this.allRows.filter(p => {
      const matchText = !term
        || (p.utilisateurNom ?? '').toLowerCase().includes(term)
        || (p.utilisateurPrenom ?? '').toLowerCase().includes(term)
        || (p.utilisateurEmail ?? '').toLowerCase().includes(term)
        || (p.sortieTitle ?? '').toLowerCase().includes(term);
      const matchSortie  = !this.filterSortie      || p.sortieId === this.filterSortie;
      const matchDiff    = !this.filterDifficulte  || p.sortieDifficulte === this.filterDifficulte;
      const matchStatut  = !this.filterStatut      || (p.statutPresence ?? 'EN_ATTENTE') === this.filterStatut;
      return matchText && matchSortie && matchDiff && matchStatut;
    });
  }

  resetFilters(): void {
    this.searchTerm = ''; this.filterSortie = '';
    this.filterDifficulte = ''; this.filterStatut = '';
    this.applyFilters();
  }

  updateStatut(p: ParticipantRow, newStatut: string): void {
    p.statutPresence = newStatut;
    const url = `http://localhost:8087/api/sorties/${p.sortieId}/participants/${p.utilisateurId}/statut`;
    this.http.patch(url, { statutPresence: newStatut }, {
      headers: new HttpHeaders({ Authorization: `Bearer ${localStorage.getItem('authToken') ?? ''}` })
    }).subscribe({ error: (err) => console.warn('Statut non mis à jour', err) });
  }

  confirmDesinscription(p: ParticipantRow): void { this.rowToDelete = p; }
  cancelDelete(): void { this.rowToDelete = null; }

  executeDesinscription(): void {
    if (!this.rowToDelete) return;
    const { sortieId, utilisateurId } = this.rowToDelete;
    this.sortieService.desinscrire(sortieId!).subscribe({
      next: () => {
        this.allRows = this.allRows.filter(r => !(r.sortieId === sortieId && r.utilisateurId === utilisateurId));
        this.applyFilters();
        this.rowToDelete = null;
      },
      error: () => { this.rowToDelete = null; }
    });
  }

  getUniquesCount(): number { return new Set(this.allRows.map(r => r.utilisateurId)).size; }
  getSortiesAvecPart(): number { return new Set(this.allRows.map(r => r.sortieId)).size; }
  getDifficultyClass(difficulte?: string): string {
    const diff = (difficulte || 'FACILE').toLowerCase();
    return `badge diff-${diff}`;
  }
  getInitiales(nom?: string): string { return (nom || '?').split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2); }
  getAvatarColor(nom?: string): string {
    const colors = ['#16a34a','#1d4ed8','#d97706','#dc2626','#7c3aed','#0891b2'];
    return colors[(nom?.charCodeAt(0) ?? 0) % colors.length];
  }
  formatDate(d: any): string {
    if (!d) return '???';
    return new Date(d).toLocaleDateString('fr-FR', { day:'2-digit', month:'short', year:'numeric' });
  }
}