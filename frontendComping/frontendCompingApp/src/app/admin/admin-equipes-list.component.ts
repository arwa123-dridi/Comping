import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { EquipeService } from '../services/equipe.service';
import { EquipeResponse } from '../models/equipe.model';

@Component({
  selector: 'app-admin-equipes-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="admin-list-page">
      <div class="page-header">
        <h2>🛡️ Gestion des Équipes</h2>
        <button class="btn-create" routerLink="/admin/equipes/create">+ Nouvelle équipe</button>
      </div>

      <div class="filters-bar">
        <input type="text" [(ngModel)]="searchTerm" placeholder="Rechercher par nom..."
               (input)="applyFilters()" class="search-input"/>
        <select [(ngModel)]="filterPlace" (change)="applyFilters()" class="filter-select">
          <option value="">Toutes</option>
          <option value="disponible">Places disponibles</option>
          <option value="pleine">Complètes</option>
        </select>
        <span class="results-count">{{ filteredEquipes.length }} résultat(s)</span>
      </div>

      <div *ngIf="loading" class="loading">Chargement...</div>

      <div class="table-container" *ngIf="!loading">
        <table>
          <thead>
            <tr>
              <th>Nom</th>
              <th>Membres</th>
              <th>Capacité</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let e of filteredEquipes">
              <td><strong>{{ e.nom }}</strong></td>
              <td>{{ getNbMembres(e) }}</td>
              <td>{{ e.capaciteMax }}</td>
              <td>
                <span [class]="isPleine(e) ? 'badge badge-full' : 'badge badge-open'">
                  {{ isPleine(e) ? 'Complète' : 'Disponible' }}
                </span>
              </td>
              <td class="actions">
                <button class="btn-view"   [routerLink]="['/admin/equipes', e.id]">👁</button>
                <button class="btn-edit"   [routerLink]="['/admin/equipes/edit', e.id]">✏️</button>
                <button class="btn-delete" (click)="confirmDelete(e)">🗑</button>
              </td>
            </tr>
            <tr *ngIf="filteredEquipes.length === 0">
              <td colspan="5" class="empty">Aucune équipe trouvée</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="modal-overlay" *ngIf="equipeToDelete" (click)="cancelDelete()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>Confirmer la suppression</h3>
          <p>Supprimer l'équipe <strong>{{ equipeToDelete.nom }}</strong> ?</p>
          <div class="modal-actions">
            <button class="btn-cancel" (click)="cancelDelete()">Annuler</button>
            <button class="btn-confirm-delete" (click)="deleteEquipe()">Supprimer</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-list-page { padding: 24px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .page-header h2 { color: #2d6a4f; margin: 0; }
    .btn-create { background: #2d6a4f; color: white; border: none; padding: 10px 20px; border-radius: 8px; cursor: pointer; font-weight: 600; }
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
    .badge-open { background: #d4edda; color: #155724; }
    .badge-full { background: #f8d7da; color: #721c24; }
    .actions { display: flex; gap: 6px; }
    .btn-view, .btn-edit, .btn-delete { border: none; background: none; cursor: pointer; font-size: 1.1rem; padding: 4px 8px; border-radius: 6px; transition: background 0.2s; }
    .btn-delete:hover { background: #fde8e8; }
    .btn-edit:hover { background: #fff3cd; }
    .empty, .loading { text-align: center; color: #999; padding: 32px; }
    .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal { background: white; border-radius: 12px; padding: 28px; max-width: 400px; width: 90%; }
    .modal h3 { margin: 0 0 12px; color: #d9534f; }
    .modal-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 20px; }
    .btn-cancel { padding: 8px 20px; border: 1px solid #ddd; border-radius: 8px; background: white; cursor: pointer; }
    .btn-confirm-delete { padding: 8px 20px; border: none; border-radius: 8px; background: #d9534f; color: white; cursor: pointer; font-weight: 600; }
  `]
})
export class AdminEquipesListComponent implements OnInit {
  equipes: EquipeResponse[] = [];
  filteredEquipes: EquipeResponse[] = [];
  loading = false;
  searchTerm = '';
  filterPlace = '';
  equipeToDelete: EquipeResponse | null = null;

  constructor(private equipeService: EquipeService) {}

  ngOnInit(): void {
    this.loading = true;
    this.equipeService.getAllEquipes().subscribe({
      next: data => { this.equipes = data; this.applyFilters(); this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  applyFilters(): void {
    const term = this.searchTerm.toLowerCase();
    this.filteredEquipes = this.equipes.filter(e => {
      const matchText = !term || e.nom?.toLowerCase().includes(term);
      const pleine = this.isPleine(e);
      const matchPlace = !this.filterPlace
        || (this.filterPlace === 'disponible' && !pleine)
        || (this.filterPlace === 'pleine' && pleine);
      return matchText && matchPlace;
    });
  }

  isPleine(e: EquipeResponse): boolean {
    return this.getNbMembres(e) >= (e.capaciteMax ?? 0);
  }

  getNbMembres(e: EquipeResponse): number {
    return e.membres?.length ?? e.nbMembresActuels ?? 0;
  }

  confirmDelete(e: EquipeResponse): void { this.equipeToDelete = e; }
  cancelDelete(): void { this.equipeToDelete = null; }

  deleteEquipe(): void {
    if (!this.equipeToDelete) return;
    this.equipeService.deleteEquipe(String(this.equipeToDelete.id)).subscribe({
      next: () => {
        this.equipes = this.equipes.filter(e => e.id !== this.equipeToDelete!.id);
        this.applyFilters();
        this.equipeToDelete = null;
      }
    });
  }
}
