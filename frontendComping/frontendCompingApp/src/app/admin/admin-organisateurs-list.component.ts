import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../services/user.service';
import { User } from '../models/user.model';

@Component({
  selector: 'app-admin-organisateurs-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="admin-list-page">
      <div class="page-header">
        <h2>🧑‍💼 Gestion des Organisateurs</h2>
      </div>

      <div class="filters-bar">
        <input type="text" [(ngModel)]="searchTerm" placeholder="Rechercher par nom ou email..."
               (input)="applyFilters()" class="search-input"/>
        <select [(ngModel)]="filterStatut" (change)="applyFilters()" class="filter-select">
          <option value="">Tous statuts</option>
          <option value="actif">Actifs</option>
          <option value="inactif">Inactifs</option>
        </select>
        <span class="results-count">{{ filteredOrga.length }} organisateur(s)</span>
      </div>

      <div *ngIf="loading" class="loading">Chargement...</div>

      <div class="table-container" *ngIf="!loading">
        <table>
          <thead>
            <tr>
              <th>Nom</th>
              <th>Email</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let u of filteredOrga">
              <td><strong>{{ u.firstName }} {{ u.lastName }}</strong></td>
              <td>{{ u.email }}</td>
              <td>
                <span [class]="u.actif ? 'badge badge-actif' : 'badge badge-inactif'">
                  {{ u.actif ? 'Actif' : 'Inactif' }}
                </span>
              </td>
              <td class="actions">
                <button (click)="toggleStatut(u)" class="btn-toggle">
                  {{ u.actif ? '⏸ Désactiver' : '▶ Activer' }}
                </button>
                <button (click)="confirmDelete(u)" class="btn-delete-sm">🗑 Supprimer</button>
              </td>
            </tr>
            <tr *ngIf="filteredOrga.length === 0">
              <td colspan="4" class="empty">Aucun organisateur trouvé</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="modal-overlay" *ngIf="userToDelete" (click)="cancelDelete()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>Confirmer la suppression</h3>
          <p>Supprimer le compte de <strong>{{ userToDelete.email }}</strong> ?</p>
          <div class="modal-actions">
            <button class="btn-cancel" (click)="cancelDelete()">Annuler</button>
            <button class="btn-confirm-delete" (click)="deleteUser()">Supprimer</button>
          </div>
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
    .badge-actif { background: #d4edda; color: #155724; }
    .badge-inactif { background: #f8d7da; color: #721c24; }
    .actions { display: flex; gap: 8px; }
    .btn-toggle { padding: 5px 12px; border: 1px solid #2d6a4f; color: #2d6a4f; background: white; border-radius: 6px; cursor: pointer; font-size: 0.85rem; }
    .btn-toggle:hover { background: #e8f5e9; }
    .btn-delete-sm { padding: 5px 12px; border: 1px solid #d9534f; color: #d9534f; background: white; border-radius: 6px; cursor: pointer; font-size: 0.85rem; }
    .btn-delete-sm:hover { background: #fde8e8; }
    .empty, .loading { text-align: center; color: #999; padding: 32px; }
    .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .modal { background: white; border-radius: 12px; padding: 28px; max-width: 400px; width: 90%; }
    .modal h3 { margin: 0 0 12px; color: #d9534f; }
    .modal-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 20px; }
    .btn-cancel { padding: 8px 20px; border: 1px solid #ddd; border-radius: 8px; background: white; cursor: pointer; }
    .btn-confirm-delete { padding: 8px 20px; border: none; border-radius: 8px; background: #d9534f; color: white; cursor: pointer; font-weight: 600; }
  `]
})
export class AdminOrganisateursListComponent implements OnInit {
  allOrga: User[] = [];
  filteredOrga: User[] = [];
  loading = false;
  searchTerm = '';
  filterStatut = '';
  userToDelete: User | null = null;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loading = true;
    this.userService.getAllUsers().subscribe({
      next: data => {
        // Filtrer uniquement les ORGANISATEURS
        this.allOrga = data.filter(u =>
          u.role === 'ORGANISATEUR' || u.role === 'ROLE_ORGANISATEUR'
        );
        this.applyFilters();
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  applyFilters(): void {
    const term = this.searchTerm.toLowerCase();
    this.filteredOrga = this.allOrga.filter(u => {
      const matchText = !term
        || u.email?.toLowerCase().includes(term)
        || u.firstName?.toLowerCase().includes(term)
        || u.lastName?.toLowerCase().includes(term);
      const matchStatut = !this.filterStatut
        || (this.filterStatut === 'actif' && u.actif)
        || (this.filterStatut === 'inactif' && !u.actif);
      return matchText && matchStatut;
    });
  }

  toggleStatut(u: User): void {
    this.userService.updateUserStatus(String(u.id), !u.actif).subscribe({
      next: () => { u.actif = !u.actif; this.applyFilters(); }
    });
  }

  confirmDelete(u: User): void { this.userToDelete = u; }
  cancelDelete(): void { this.userToDelete = null; }

  deleteUser(): void {
    if (!this.userToDelete) return;
    this.userService.deleteUser(String(this.userToDelete.id)).subscribe({
      next: () => {
        this.allOrga = this.allOrga.filter(u => u.id !== this.userToDelete!.id);
        this.applyFilters();
        this.userToDelete = null;
      }
    });
  }
}
