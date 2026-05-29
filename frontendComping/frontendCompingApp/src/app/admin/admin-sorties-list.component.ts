import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SortieService } from '../services/sortie.service';
import { SortieResponse } from '../models/sortie.model';

@Component({
  selector: 'app-admin-sorties-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="admin-list-page">
      <div class="page-header">
        <h2>🏕️ Gestion des Randonnées</h2>
      </div>

      <div class="filters-bar">
        <input type="text" [(ngModel)]="searchTerm" placeholder="Rechercher par titre ou lieu..."
               (input)="applyFilters()" class="search-input"/>
        <select [(ngModel)]="filterDifficulte" (change)="applyFilters()" class="filter-select">
          <option value="">Toutes difficultés</option>
          <option value="FACILE">Facile</option>
          <option value="MOYEN">Moyen</option>
          <option value="DIFFICILE">Difficile</option>
        </select>
        <select [(ngModel)]="filterStatut" (change)="applyFilters()" class="filter-select">
          <option value="">Tous statuts</option>
          <option value="future">À venir</option>
          <option value="passee">Passées</option>
        </select>
        <span class="results-count">{{ filteredSorties.length }} résultat(s)</span>
      </div>

      <div *ngIf="loading" class="loading">Chargement...</div>

      <div class="table-container" *ngIf="!loading">
        <table>
          <thead>
            <tr>
              <th>Titre</th>
              <th>Lieu de départ</th>
              <th>Date</th>
              <th>Difficulté</th>
              <th>Participants</th>
              <th>Capacité</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let s of filteredSorties">
              <td><strong>{{ s.titre }}</strong></td>
              <td>{{ s.lieuDepart }}</td>
              <td>{{ formatDate(s.dateDebut) }}</td>
              <td>
                <span [class]="'badge diff-' + s.difficulte.toLowerCase()">{{ s.difficulte }}</span>
              </td>
              <td>{{ getNbParticipants(s) }}</td>
              <td>{{ s.capaciteMax }}</td>
              <td class="actions">
                <button class="btn-view" [routerLink]="['/admin/sorties', s.id]">👁</button>
                <button class="btn-delete" (click)="confirmDelete(s)">🗑</button>
              </td>
            </tr>
            <tr *ngIf="filteredSorties.length === 0">
              <td colspan="7" class="empty">Aucune randonnée trouvée</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ✅ Modal EN DEHORS du div principal pour éviter le z-index -->
    <div class="modal-overlay" *ngIf="sortieToDelete" (click)="cancelDelete()">
      <div class="modal" (click)="$event.stopPropagation()">
        <h3>Confirmer la suppression</h3>
        <p>Supprimer <strong>{{ sortieToDelete.titre }}</strong> ?<br/>
           L'équipe associée sera dissociée.</p>
        <div class="modal-actions">
          <button class="btn-cancel" (click)="cancelDelete()">Annuler</button>
          <button class="btn-confirm-delete" (click)="deleteSortie()">Supprimer</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-list-page { padding: 24px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .page-header h2 { color: #2d6a4f; margin: 0; }
    .filters-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
    .search-input { flex: 1; min-width: 200px; padding: 8px 12px; border: 1px solid #ddd; border-radius: 8px; }
    .filter-select { padding: 8px 12px; border: 1px solid #ddd; border-radius: 8px; }
    .results-count { color: #666; font-size: 0.9rem; margin-left: auto; }
    .table-container { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
    th { background: #f8f9fa; padding: 12px 16px; text-align: left; font-weight: 600; color: #555; border-bottom: 2px solid #e9ecef; }
    td { padding: 12px 16px; border-bottom: 1px solid #f1f3f4; }
    tr:hover td { background: #f8fff8; }
    .badge { padding: 4px 10px; border-radius: 12px; font-size: 0.8rem; font-weight: 600; }
    .diff-facile { background: #d4edda; color: #155724; }
    .diff-moyen { background: #fff3cd; color: #856404; }
    .diff-difficile { background: #f8d7da; color: #721c24; }
    .actions { display: flex; gap: 6px; }
    .btn-view, .btn-delete { border: none; background: none; cursor: pointer; font-size: 1.1rem; padding: 4px 8px; border-radius: 6px; transition: background 0.2s; }
    .btn-view:hover { background: #e3f2fd; }
    .btn-delete:hover { background: #fde8e8; }
    .empty { text-align: center; color: #999; padding: 32px; }
    .loading { text-align: center; color: #666; padding: 32px; }

    /* ✅ z-index élevé pour passer au-dessus du layout */
    .modal-overlay {
      position: fixed; inset: 0;
      background: rgba(0,0,0,0.5);
      display: flex; align-items: center; justify-content: center;
      z-index: 9999;
    }
    .modal {
      background: white; border-radius: 12px; padding: 28px;
      max-width: 400px; width: 90%;
      position: relative; z-index: 10000;
    }
    .modal h3 { margin: 0 0 12px; color: #d9534f; }
    .modal-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 20px; }
    .btn-cancel { padding: 8px 20px; border: 1px solid #ddd; border-radius: 8px; background: white; cursor: pointer; }
    .btn-confirm-delete { padding: 8px 20px; border: none; border-radius: 8px; background: #d9534f; color: white; cursor: pointer; font-weight: 600; }
  `]
})
export class AdminSortiesListComponent implements OnInit {
  sorties: SortieResponse[] = [];
  filteredSorties: SortieResponse[] = [];
  loading = false;
  searchTerm = '';
  filterDifficulte = '';
  filterStatut = '';
  sortieToDelete: SortieResponse | null = null;

  constructor(private sortieService: SortieService) {}

  ngOnInit(): void {
    this.loading = true;
    this.sortieService.getAllSorties().subscribe({
      next: data => { this.sorties = data; this.applyFilters(); this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  applyFilters(): void {
    const term = this.searchTerm.toLowerCase();
    const now = new Date();
    this.filteredSorties = this.sorties.filter(s => {
      const matchText = !term || s.titre?.toLowerCase().includes(term) || s.lieuDepart?.toLowerCase().includes(term);
      const matchDiff = !this.filterDifficulte || s.difficulte === this.filterDifficulte;
      const isFuture  = new Date(s.dateDebut) >= now;
      const matchStat = !this.filterStatut
        || (this.filterStatut === 'future' && isFuture)
        || (this.filterStatut === 'passee' && !isFuture);
      return matchText && matchDiff && matchStat;
    });
  }

  confirmDelete(s: SortieResponse): void { this.sortieToDelete = s; }
  cancelDelete(): void { this.sortieToDelete = null; }

  deleteSortie(): void {
    if (!this.sortieToDelete) return;
    this.sortieService.deleteSortie(String(this.sortieToDelete.id)).subscribe({
      next: () => {
        this.sorties = this.sorties.filter(s => s.id !== this.sortieToDelete!.id);
        this.applyFilters();
        this.sortieToDelete = null;
      }
    });
  }

  getNbParticipants(s: SortieResponse): number {
    return s.participantIds?.length ?? s.nombreParticipants ?? 0;
  }

  formatDate(d: Date | string): string {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}