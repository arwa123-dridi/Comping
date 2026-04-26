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

  newReservation: Partial<Reservation> = {
  utilisateurId: '',
  siteCampingId: '',
  dateDebut: '',
  dateFin: '',
  montantTotal: 0,
  statut: 'EN_ATTENTE',
  modePaiement: 'CARTE'
};


  constructor(private reservationService: ReservationService,  private siteService: SiteService) {}


ngOnInit(): void {
    this.loadCurrentUser();
    this.getAll();
    this.refreshData();
  }

  // RÉCUPÉRATION DE L'UTILISATEUR VIA LE TOKEN DIRECTEMENT
  loadCurrentUser(): void {
    const token = localStorage.getItem('authToken');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.currentUser = {
          id: payload.id || payload.sub, // Ajustez selon votre backend
          name: payload.sub.split('@')[0] // Utilise le début de l'email comme nom
        };
        this.newReservation.utilisateurId = this.currentUser.id;
      } catch (e) {
        console.error("Erreur décodage token", e);
      }
    }
  }

  refreshData(): void {
    this.loading = true;
    // Charger les réservations
    this.reservationService.getAll().subscribe({
      next: (data) => {
        this.reservations = data;
        this.applyFilter();
        this.loading = false;
      },
      error: () => this.loading = false
    });

    // Charger les sites (avec headers pour éviter 403)
    this.siteService.getAll().subscribe({
      next: (data) => this.sites = data,
      error: (err) => console.error("Erreur sites (403?) :", err)
    });
  }

  applyFilter(): void {
    if (this.activeFilter === 'ALL') {
      this.filteredReservations = [...this.reservations];
    } else {
      this.filteredReservations = this.reservations.filter(r => r.statut === this.activeFilter);
    }
  }

  setFilter(f: string): void { this.activeFilter = f; this.applyFilter(); }



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
    return this.reservations.filter(r =>
      r.statut === statut ||
      (statut === 'CONFIRME' && r.statut === 'CONFIRMEE')
    ).length;
  }

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