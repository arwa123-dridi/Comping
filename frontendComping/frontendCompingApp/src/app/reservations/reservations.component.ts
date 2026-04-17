import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReservationService } from '../services/reservation.service';
import { Reservation } from '../models/reservation.model';
import { FormsModule } from '@angular/forms';
import { User } from '../models/user.model';
import { SiteCamping } from '../models/site-camping.model';

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

  constructor(private reservationService: ReservationService) {}

  ngOnInit(): void {
    this.getAll();
    this.loadUsersAndSites();
  }

  loadUsersAndSites(): void {
  this.userService.getAll().subscribe({
    next: (data: User[]) => this.users = data,
    error: (err) => console.error('Erreur users:', err)
  });

  this.siteService.getAll().subscribe({
    next: (data: SiteCamping[]) => this.sites = data,
    error: (err) => console.error('Erreur sites:', err)
  });
}

  newReservation: Partial<Reservation> = {
  utilisateurId: '',
  siteCampingId: '',
  dateDebut: '',
  dateFin: '',
  montantTotal: 0,
  statut: 'EN_ATTENTE',
  modePaiement: 'CARTE'
};


openModal(): void {
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
      this.newReservation = {
        statut: 'EN_ATTENTE',
        modePaiement: 'CARTE'
      };
    },
    error: (err) => console.error(err)
  });
}
  
  getAll(): void {
    this.loading = true;
    this.reservationService.getAll().subscribe({
      next: (data: Reservation[]) => {
        this.reservations = data;  // ✅ direct, le model correspond exactement à l'API
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  setFilter(filter: string): void {
    this.activeFilter = filter;
    this.applyFilter();
  }

  applyFilter(): void {
    if (this.activeFilter === 'ALL') {
      this.filteredReservations = [...this.reservations];
    } else {
      this.filteredReservations = this.reservations.filter(r =>
        r.statut === this.activeFilter ||
        (this.activeFilter === 'CONFIRME' && r.statut === 'CONFIRMEE') ||
        (this.activeFilter === 'ANNULE'   && r.statut === 'ANNULEE')
      );
    }
  }

  countByStatut(statut: string): number {
    return this.reservations.filter(r =>
      r.statut === statut ||
      (statut === 'CONFIRME' && r.statut === 'CONFIRMEE')
    ).length;
  }

  // ✅ Méthode pour l'avatar — évite l'erreur toUpperCase sur unknown[]
  getAvatar(utilisateurId: string): string {
    return utilisateurId ? utilisateurId.charAt(0).toUpperCase() : '?';
  }

  updateStatut(id: string, statut: string): void {
    this.reservationService.updateStatut(id, statut).subscribe({
      next: () => this.getAll(),
      error: (err) => console.error(err)
    });
  }

  deleteReservation(id: string): void {
    if (!confirm('Supprimer cette réservation définitivement ?')) return;
    this.reservationService.delete(id).subscribe({
      next: () => this.getAll(),
      error: (err) => console.error(err)
    });
  }
}