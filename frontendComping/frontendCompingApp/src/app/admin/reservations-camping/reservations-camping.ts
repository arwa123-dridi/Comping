import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { ReservationService } from '../../services/reservation.service';
import { UserService } from '../../services/user.service';
import { SiteService } from '../../services/site';

@Component({
  selector: 'app-reservations-camping',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reservations-camping.html',
  styleUrl: './reservations-camping.css'
})
export class ReservationsCamping implements OnInit {

  reservations: any[] = [];
  filteredReservations: any[] = [];

  users: any[] = [];
  campings: any[] = [];

  filterStatut = '';
  filterClient = '';

  constructor(
    private reservationService: ReservationService,
    private userService: UserService,
    private siteService: SiteService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    forkJoin({
      reservations: this.reservationService.getAll(),
      users: this.userService.getAllUsers(),
      campings: this.siteService.getAll()
    }).subscribe({
      next: ({ reservations, users, campings }) => {

        this.users = users;
        this.campings = campings;

        this.reservations = reservations.map(r => {

          const user = users.find(u =>
            u.id === r.utilisateurId || u.id === r.utilisateurId
          );

          const camping = campings.find(c => c.id === r.siteCampingId);

          // 🔥 FIX IMPORTANT ICI
          const utilisateurNom =
          user?.firstName && user?.lastName? `${user.firstName} ${user.lastName}`: user?.email.split('@')[0]

          return {
            ...r,
            utilisateurNom: utilisateurNom || user?.email || 'Client inconnu',
            siteCampingNom: camping ? camping.nom : 'Inconnu'
          };
        });

        this.filteredReservations = [...this.reservations];
      },
      error: (err) => {
        console.error('Erreur chargement données', err);
      }
    });
  }

  // 🔎 FILTRES
  filterReservations(): void {
    this.filteredReservations = this.reservations.filter(r => {

      const matchStatut =
        !this.filterStatut || r.statut === this.filterStatut;

      const matchClient =
        !this.filterClient ||
        (r.utilisateurNom || '')
          .toLowerCase()
          .includes(this.filterClient.toLowerCase());

      return matchStatut && matchClient;
    });
  }

  resetFilters(): void {
    this.filterStatut = '';
    this.filterClient = '';
    this.filteredReservations = [...this.reservations];
  }

  // ❌ ANNULER RESERVATION
  cancelReservation(reservation: any): void {
    this.reservationService.delete(reservation.id).subscribe({
      next: () => {
        this.reservations = this.reservations.filter(
          r => r.id !== reservation.id
        );

        this.filterReservations();
      },
      error: (err) => {
        console.error('Erreur suppression réservation', err);
      }
    });
  }
}