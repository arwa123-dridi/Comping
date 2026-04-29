import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReservationService } from '../services/reservation.service';
import { Reservation } from '../models/reservation.model';
import { FormsModule } from '@angular/forms';
import { User } from '../models/user.model';
import { SiteCamping } from '../models/site-camping.model';
import { SiteService } from '../services/site';

@Component({
  selector: 'app-reservations',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './reservations.component.html',
  styleUrls: ['./reservations.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ReservationsComponent implements OnInit {

  reservations: Reservation[] = [];
  users: User[] = [];
  sites: SiteCamping[] = [];
  filteredReservations: Reservation[] = [];
  activeFilter = 'ALL';
  loading = true;
  showModal = false;
  currentUser: any = null;
  showDeleteModal = false;
  pendingDeleteId: string | null = null;
  showEditModal = false;
  editReservation: Partial<Reservation> = {};
  editingId: string | null = null;

  newReservation: Partial<Reservation> = {
    utilisateurId: '',
    siteCampingId: '',
    dateDebut: '',
    dateFin: '',
    montantTotal: 0,
    statut: 'EN_ATTENTE',
    modePaiement: 'CARTE'
  };

  constructor(private reservationService: ReservationService, private siteService: SiteService) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.getAll();
    this.refreshData();
  }

  loadCurrentUser(): void {
    const token = localStorage.getItem('authToken');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.currentUser = {
          id: payload.id || payload.sub,
          name: payload.sub.split('@')[0]
        };
        this.newReservation.utilisateurId = this.currentUser.id;
      } catch (e) {
        console.error('Erreur décodage token', e);
      }
    }
  }

  refreshData(): void {
    this.loading = true;
    this.reservationService.getAll().subscribe({
      next: (data) => {
        this.reservations = data;
        this.applyFilter();
        this.loading = false;
      },
      error: () => (this.loading = false)
    });

    this.siteService.getAll().subscribe({
      next: (data) => (this.sites = data),
      error: (err) => console.error('Erreur sites (403?) :', err)
    });
  }

  applyFilter(): void {
    if (this.activeFilter === 'ALL') {
      this.filteredReservations = [...this.reservations];
    } else {
      this.filteredReservations = this.reservations.filter(r => r.statut === this.activeFilter);
    }
  }

  setFilter(f: string): void {
    this.activeFilter = f;
    this.applyFilter();
  }

  openModal(): void {
    if (this.currentUser) {
      this.newReservation.utilisateurId = this.currentUser.id;
    }
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  addReservation(): void {
    this.reservationService.create(this.newReservation).subscribe({
      next: () => {
        this.getAll();
        this.closeModal();
        this.newReservation = { statut: 'EN_ATTENTE', modePaiement: 'CARTE' };
      },
      error: (err) => console.error(err)
    });
  }

  getAll(): void {
    this.loading = true;
    this.reservationService.getAll().subscribe({
      next: (data: Reservation[]) => {
        this.reservations = data;
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  countByStatut(statut: string): number {
    return this.reservations.filter(
      r => r.statut === statut || (statut === 'CONFIRME' && r.statut === 'CONFIRMEE')
    ).length;
  }

  getAvatar(utilisateurId: string): string {
    return utilisateurId ? utilisateurId.charAt(0).toUpperCase() : '?';
  }

  /**
   * Returns the site name from the cached sites list.
   * Falls back to the raw ID if the site hasn't loaded yet.
   */
  getSiteName(siteCampingId: string): string {
    const site = this.sites.find(s => s.id === siteCampingId);
    return site ? site.nom : siteCampingId || '—';
  }

  /**
   * Calculates the number of nights between two dates.
   */
  getNights(dateDebut: any, dateFin: any): number {
    if (!dateDebut || !dateFin) return 0;
    const from = new Date(dateDebut);
    const to   = new Date(dateFin);
    const diff = Math.round((to.getTime() - from.getTime()) / (1000 * 60 * 60 * 24));
    return diff > 0 ? diff : 0;
  }

  updateStatut(id: string, statut: string): void {
    this.reservationService.updateStatut(id, statut).subscribe({
      next: () => this.getAll(),
      error: (err) => console.error(err)
    });
  }

  deleteReservation(id: string): void {
    this.pendingDeleteId = id;
    this.showDeleteModal = true;
  }

  confirmDelete(): void {
    if (!this.pendingDeleteId) return;
    this.reservationService.delete(this.pendingDeleteId).subscribe({
      next: () => {
        this.getAll();
        this.cancelDelete();
      },
      error: (err) => console.error(err)
    });
  }

  cancelDelete(): void {
    this.showDeleteModal = false;
    this.pendingDeleteId = null;
  }

  openEditModal(r: Reservation): void {
    this.editingId = r.id;
    this.editReservation = {
      siteCampingId: r.siteCampingId,
      dateDebut: r.dateDebut ? r.dateDebut.toString().substring(0, 10) : '',
      dateFin:   r.dateFin   ? r.dateFin.toString().substring(0, 10)   : '',
      montantTotal: r.montantTotal,
      modePaiement: r.modePaiement,
      statut: r.statut
    };
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingId = null;
    this.editReservation = {};
  }

  saveEdit(): void {
    if (!this.editingId) return;
    this.reservationService.update(this.editingId, this.editReservation).subscribe({
      next: () => {
        this.getAll();
        this.closeEditModal();
      },
      error: (err) => console.error(err)
    });
  }
}