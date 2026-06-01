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
                <span class="sortie-link" [routerLink]="['/admin/sorties', p.sortieId]">
                  {{ p.sortieTitle }}
                </span>
            </td>
            <td>
                <span [class]="'badge diff-' + p.sortieDifficulte.toLowerCase()">
                  {{ p.sortieDifficulte || '???' }}
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
              <button class="btn-delete" title="Désinscrire ce participant"
                      (click)="confirmDesinscription(p)">🗑️</button>
            </td>
          </tr>
          <tr *ngIf="filteredRows.length === 0">
            <td colspan="10" class="empty">
              <div class="empty-state">
                <span>👥</span>
                <p *ngIf="allRows.length === 0 && !loading">
                  Aucune inscription trouvée.
                  <br><small>Vérifiez qu’un utilisateur s’est bien inscrit à une sortie.</small>
                </p>
                <p *ngIf="allRows.length > 0">Aucun résultat pour ces filtres.</p>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Modal confirmation désinscription -->
      <div class="modal-overlay" *ngIf="rowToDelete" (click)="cancelDelete()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>Confirmer la désinscription</h3>
          <p>
            Désinscrire <strong>{{ rowToDelete.utilisateurPrenom }} {{ rowToDelete.utilisateurNom }}</strong>
            de la sortie <strong>{{ rowToDelete.sortieTitle }}</strong> ?
          </p>
          <div class="modal-actions">
            <button class="btn-cancel" (click)="cancelDelete()">Annuler</button>
            <button class="btn-confirm-delete" (click)="executeDesinscription()">Désinscrire</button>
          </div>
        </div>
      </div>

    </div>
  `,
  styles: [`/* (styles inchangés – gardez ceux que vous avez déjà) */`]
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

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.errorMsg = '';

    this.sortieService.getAllSorties().subscribe({
      next: (sorties) => {
        console.log('📋 Sorties chargées :', sorties.length);
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
          console.log(`🔍 Chargement participants pour sortie "${sortie.titre}" (${sortie.id})`);

          this.http.get<ParticipationDTO[]>(url, {
            headers: new HttpHeaders({ Authorization: `Bearer ${localStorage.getItem('authToken') ?? ''}` })
          }).subscribe({
            next: (participants) => {
              console.log(`✅ ${participants.length} participant(s) pour "${sortie.titre}"`);
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
                console.log('🏁 Total lignes chargées :', this.allRows.length);
                if (this.allRows.length === 0) {
                  this.errorMsg = 'Aucune inscription trouvée. Connectez un utilisateur et inscrivez-le à une sortie.';
                }
                this.applyFilters();
                this.loading = false;
              }
            },
            error: (err) => {
              console.error(`❌ Erreur pour sortie ${sortie.id} :`, err);
              completed++;
              if (completed === sorties.length) {
                this.allRows = rows;
                this.applyFilters();
                this.loading = false;
                if (this.allRows.length === 0 && !this.errorMsg) {
                  this.errorMsg = 'Impossible de charger certaines participations. Vérifiez les droits admin.';
                }
              }
            }
          });
        });
      },
      error: (err) => {
        console.error('❌ Erreur chargement sorties :', err);
        this.loading = false;
        this.errorMsg = 'Impossible de charger les sorties. Vérifiez que le backend est démarré et que vous êtes admin.';
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
        || (p.sortieTitle ?? '').toLowerCase().includes(term)
        || (p.sortieRegion ?? '').toLowerCase().includes(term);

      const matchSortie = !this.filterSortie || p.sortieId === this.filterSortie;
      const matchDiff = !this.filterDifficulte || p.sortieDifficulte === this.filterDifficulte;
      const matchStatut = !this.filterStatut || (p.statutPresence ?? 'EN_ATTENTE') === this.filterStatut;

      return matchText && matchSortie && matchDiff && matchStatut;
    });
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.filterSortie = '';
    this.filterDifficulte = '';
    this.filterStatut = '';
    this.applyFilters();
  }

  updateStatut(p: ParticipantRow, newStatut: string): void {
    p.statutPresence = newStatut;
    const url = `http://localhost:8087/api/sorties/${p.sortieId}/participants/${p.utilisateurId}/statut`;
    this.http.patch(url, { statutPresence: newStatut }, {
      headers: new HttpHeaders({ Authorization: `Bearer ${localStorage.getItem('authToken') ?? ''}` })
    }).subscribe({
      next: () => console.log('Statut mis à jour'),
      error: (err) => console.warn('Impossible de mettre à jour le statut', err)
    });
  }

  confirmDesinscription(p: ParticipantRow): void {
    this.rowToDelete = p;
  }

  cancelDelete(): void {
    this.rowToDelete = null;
  }

  executeDesinscription(): void {
    if (!this.rowToDelete) return;
    const { sortieId, utilisateurId } = this.rowToDelete;

    this.sortieService.desinscrire(sortieId!).subscribe({
      next: () => {
        this.allRows = this.allRows.filter(r => !(r.sortieId === sortieId && r.utilisateurId === utilisateurId));
        this.applyFilters();
        this.rowToDelete = null;
      },
      error: () => {
        this.rowToDelete = null;
      }
    });
  }

  getUniquesCount(): number {
    return new Set(this.allRows.map(r => r.utilisateurId)).size;
  }

  getSortiesAvecPart(): number {
    return new Set(this.allRows.map(r => r.sortieId)).size;
  }

  getInitiales(nom?: string): string {
    return (nom || '?').split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }

  getAvatarColor(nom?: string): string {
    const colors = ['#16a34a','#1d4ed8','#d97706','#dc2626','#7c3aed','#0891b2'];
    const idx = (nom?.charCodeAt(0) ?? 0) % colors.length;
    return colors[idx];
  }

  formatDate(d: any): string {
    if (!d) return '???';
    return new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
