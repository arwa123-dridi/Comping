import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ReservationService } from '../services/reservation.service';
import { Reservation } from '../models/reservation.model';
import { User } from '../models/user.model';
import { SiteCamping } from '../models/site-camping.model';
import { SiteService } from '../services/site';

// Interface pour les cellules du calendrier
export interface CalCell {
  date: Date | null;
  dateStr: string;
  isPast: boolean;
  isToday: boolean;
  status: 'libre' | 'reserve' | 'bloque' | '';
  tooltipText: string;
}

@Component({
  selector: 'app-reservations',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './reservations.component.html',
  styleUrls: ['./reservations.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ReservationsComponent implements OnInit {

  // ── Réservations & filtres ─────────────────────────────────────────────
  reservations: Reservation[] = [];
  filteredReservations: Reservation[] = [];
  activeFilter = 'ALL';
  loading = true;

  // ── Utilisateur & sites ────────────────────────────────────────────────
  users: User[] = [];
  sites: SiteCamping[] = [];
  currentUser: any = null;

  // ── Modals ─────────────────────────────────────────────────────────────
  showModal = false;
  showDeleteModal = false;
  pendingDeleteId: string | null = null;
  showEditModal = false;
  editReservation: Partial<Reservation> = {};
  editingId: string | null = null;

  // ── Nouvelle réservation ───────────────────────────────────────────────
  newReservation: Partial<Reservation> = {
    utilisateurId: '',
    siteCampingId: '',
    dateDebut: '',
    dateFin: '',
    montantTotal: 0,
    statut: 'EN_ATTENTE',
    modePaiement: 'CARTE'
  };

  // ── Calendrier ─────────────────────────────────────────────────────────
  calendarOpen     = true;
  calendarSiteId   = '';
  calendarYear     = new Date().getFullYear();
  calendarMonth    = new Date().getMonth(); // 0-indexed
  calendarCells: CalCell[] = [];
  calStats         = { libre: 0, reserve: 0, bloque: 0 };
  selectedCalDate  = '';
  selectedCellStatus: 'libre' | 'reserve' | 'bloque' | '' = '';
  selectedCalDateLabel = '';

  readonly dayNames = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

  constructor(
    private reservationService: ReservationService,
    private siteService: SiteService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.refreshData();
  }

  // ── Auth ───────────────────────────────────────────────────────────────
  loadCurrentUser(): void {
    const token = localStorage.getItem('authToken');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.currentUser = {
          id: payload.id || payload.sub,
          name: payload.sub?.split('@')[0] || 'Campeur'
        };
        this.newReservation.utilisateurId = this.currentUser.id;
      } catch (e) {
        console.error('Erreur décodage token', e);
      }
    }
  }

  // ── Chargement données ─────────────────────────────────────────────────
  refreshData(): void {
    this.loading = true;

    this.reservationService.getAll().subscribe({
      next: (data) => {
        this.reservations = data;
        this.applyFilter();
        this.buildCalendar();
        this.loading = false;
      },
      error: () => (this.loading = false)
    });

    this.siteService.getAll().subscribe({
      next: (data) => {
        this.sites = data;
        this.buildCalendar(); // reconstruire si les sites arrivent après
      },
      error: (err) => console.error('Erreur sites :', err)
    });
  }

  getAll(): void {
    this.loading = true;
    this.reservationService.getAll().subscribe({
      next: (data: Reservation[]) => {
        this.reservations = data;
        this.applyFilter();
        this.buildCalendar();
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  // ── Filtres ────────────────────────────────────────────────────────────
  applyFilter(): void {
    if (this.activeFilter === 'ALL') {
      this.filteredReservations = [...this.reservations];
    } else {
      this.filteredReservations = this.reservations.filter(r => {
        if (this.activeFilter === 'CONFIRME') {
          return r.statut === 'CONFIRME' || r.statut === 'CONFIRMEE';
        }
        if (this.activeFilter === 'ANNULE') {
          return r.statut === 'ANNULE' || r.statut === 'ANNULEE';
        }
        return r.statut === this.activeFilter;
      });
    }
  }

  setFilter(f: string): void {
    this.activeFilter = f;
    this.applyFilter();
  }

  // ── Helpers ────────────────────────────────────────────────────────────
  countByStatut(statut: string): number {
    return this.reservations.filter(r => {
      if (statut === 'CONFIRME') return r.statut === 'CONFIRME' || r.statut === 'CONFIRMEE';
      if (statut === 'ANNULE')   return r.statut === 'ANNULE'   || r.statut === 'ANNULEE';
      return r.statut === statut;
    }).length;
  }

  getSiteName(siteCampingId: string): string {
    const site = this.sites.find(s => s.id === siteCampingId);
    return site ? site.nom : siteCampingId || '—';
  }

  getNights(dateDebut: any, dateFin: any): number {
    if (!dateDebut || !dateFin) return 0;
    const from = new Date(dateDebut);
    const to   = new Date(dateFin);
    const diff = Math.round((to.getTime() - from.getTime()) / (1000 * 60 * 60 * 24));
    return diff > 0 ? diff : 0;
  }

  getAvatar(utilisateurId: string): string {
    return utilisateurId ? utilisateurId.charAt(0).toUpperCase() : '?';
  }

  // ── Modal nouvelle réservation ─────────────────────────────────────────
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
          utilisateurId: this.currentUser?.id || '',
          siteCampingId: '',
          dateDebut: '',
          dateFin: '',
          montantTotal: 0,
          statut: 'EN_ATTENTE',
          modePaiement: 'CARTE'
        };
      },
      error: (err) => console.error(err)
    });
  }

  // ── Modal suppression ──────────────────────────────────────────────────
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

  // ── Modal édition ──────────────────────────────────────────────────────
  openEditModal(r: Reservation): void {
    this.editingId = r.id;
    this.editReservation = {
      siteCampingId: r.siteCampingId,
      dateDebut:     r.dateDebut ? r.dateDebut.toString().substring(0, 10) : '',
      dateFin:       r.dateFin   ? r.dateFin.toString().substring(0, 10)   : '',
      montantTotal:  r.montantTotal,
      modePaiement:  r.modePaiement,
      statut:        r.statut
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

  updateStatut(id: string, statut: string): void {
    this.reservationService.updateStatut(id, statut).subscribe({
      next: () => this.getAll(),
      error: (err) => console.error(err)
    });
  }

  // ══════════════════════════════════════════════════════════════════════
  // CALENDRIER DE DISPONIBILITÉ
  // ══════════════════════════════════════════════════════════════════════

  /** Libellé affiché en haut du calendrier (ex: "mai 2026") */
  get calendarMonthLabel(): string {
    const d = new Date(this.calendarYear, this.calendarMonth, 1);
    return d.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
  }

  toggleCalendar(): void {
    this.calendarOpen = !this.calendarOpen;
  }

  onCalendarSiteChange(): void {
    this.selectedCalDate      = '';
    this.selectedCellStatus   = '';
    this.selectedCalDateLabel = '';
    this.buildCalendar();
  }

  prevMonth(): void {
    if (this.calendarMonth === 0) {
      this.calendarMonth = 11;
      this.calendarYear--;
    } else {
      this.calendarMonth--;
    }
    this.selectedCalDate = '';
    this.buildCalendar();
  }

  nextMonth(): void {
    if (this.calendarMonth === 11) {
      this.calendarMonth = 0;
      this.calendarYear++;
    } else {
      this.calendarMonth++;
    }
    this.selectedCalDate = '';
    this.buildCalendar();
  }

  goToday(): void {
    const now = new Date();
    this.calendarYear  = now.getFullYear();
    this.calendarMonth = now.getMonth();
    this.selectedCalDate = '';
    this.buildCalendar();
  }

  /**
   * Construit les cellules du calendrier pour le mois courant.
   * Détermine le statut de chaque jour en comparant les réservations :
   *   - 'bloque'  : réservation CONFIRMEE/CONFIRME
   *   - 'reserve' : réservation EN_ATTENTE
   *   - 'libre'   : aucune réservation active, jour futur
   */
  buildCalendar(): void {
    const cells: CalCell[] = [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const firstDay = new Date(this.calendarYear, this.calendarMonth, 1);
    const lastDay  = new Date(this.calendarYear, this.calendarMonth + 1, 0);

    // Décalage pour commencer la grille au lundi (ISO : lun=0 … dim=6)
    let startOffset = firstDay.getDay() - 1;
    if (startOffset < 0) startOffset = 6; // dimanche → offset 6

    // Cellules vides de début de grille
    for (let i = 0; i < startOffset; i++) {
      cells.push({
        date: null, dateStr: '',
        isPast: false, isToday: false,
        status: '', tooltipText: ''
      });
    }

    // Réservations concernées (filtre site si sélectionné, exclut annulées)
    const relevant = this.reservations.filter(r => {
      const statutOk = r.statut !== 'ANNULE' && r.statut !== 'ANNULEE';
      const siteOk   = !this.calendarSiteId || r.siteCampingId === this.calendarSiteId;
      return statutOk && siteOk;
    });

    let libre = 0, reserve = 0, bloque = 0;

    for (let d = 1; d <= lastDay.getDate(); d++) {
      const date    = new Date(this.calendarYear, this.calendarMonth, d);
      const dateStr = this.toDateStr(date);
      const isPast  = date < today;
      const isToday = date.getTime() === today.getTime();

      // Cherche si une réservation couvre ce jour (dateDebut ≤ day < dateFin)
      const hit = relevant.find(r => {
        const from = new Date(r.dateDebut); from.setHours(0, 0, 0, 0);
        const to   = new Date(r.dateFin);   to.setHours(0, 0, 0, 0);
        return date >= from && date < to;
      });

      let status: 'libre' | 'reserve' | 'bloque' | '' = '';
      let tooltipText = '';

      if (hit) {
        const confirmed = hit.statut === 'CONFIRME' || hit.statut === 'CONFIRMEE';
        if (confirmed) {
          status      = 'bloque';
          tooltipText = `Confirmé — ${this.getSiteName(hit.siteCampingId)}`;
          bloque++;
        } else {
          status      = 'reserve';
          tooltipText = `En attente — ${this.getSiteName(hit.siteCampingId)}`;
          reserve++;
        }
      } else if (!isPast && !isToday) {
        status      = 'libre';
        tooltipText = 'Disponible';
        libre++;
      } else if (isToday && !hit) {
        status      = 'libre';
        tooltipText = "Aujourd'hui — disponible";
        libre++;
      }

      cells.push({ date, dateStr, isPast, isToday, status, tooltipText });
    }

    this.calendarCells = cells;
    this.calStats = { libre, reserve, bloque };
  }

  /** Formate une date en 'YYYY-MM-DD' */
  private toDateStr(d: Date): string {
    const y  = d.getFullYear();
    const m  = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${dd}`;
  }

  /** Gère le clic sur une cellule du calendrier */
  onDayClick(cell: CalCell): void {
    if (!cell.date || cell.isPast) return;

    this.selectedCalDate      = cell.dateStr;
    this.selectedCellStatus   = cell.status as any;
    this.selectedCalDateLabel = cell.date.toLocaleDateString('fr-FR', {
      weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    });
  }

  /**
   * Pré-remplit le modal de réservation avec la date sélectionnée
   * et ouvre le modal.
   */
  bookFromCalendar(): void {
    if (this.calendarSiteId) {
      this.newReservation.siteCampingId = this.calendarSiteId;
    }
    this.newReservation.dateDebut = this.selectedCalDate;
    // Réinitialise la sélection puis ouvre le modal
    this.selectedCalDate      = '';
    this.selectedCellStatus   = '';
    this.selectedCalDateLabel = '';
    this.openModal();
  }

  /**
 * Vérifie si l'utilisateur a déjà une réservation
 * sur les mêmes dates dans un autre camping.
 */
private checkUserReservationConflict(
  debut: string,
  fin: string,
  excludeId: string | null = null
): string {

  if (!debut || !fin || !this.currentUser?.id) return '';

  const from = new Date(debut);
  const to = new Date(fin);

  from.setHours(0, 0, 0, 0);
  to.setHours(0, 0, 0, 0);

  const userReservations = this.reservations.filter(r => {

    // même utilisateur
    const sameUser = r.utilisateurId === this.currentUser.id;

    // ignorer annulées
    const activeReservation =
      r.statut !== 'ANNULE' &&
      r.statut !== 'ANNULEE' &&
      r.statut !== 'TERMINEE';

    // ignorer réservation en édition
    const notExcluded = excludeId ? r.id !== excludeId : true;

    return sameUser && activeReservation && notExcluded;
  });

  const conflict = userReservations.find(r => {

    const rFrom = new Date(r.dateDebut);
    const rTo = new Date(r.dateFin);

    rFrom.setHours(0, 0, 0, 0);
    rTo.setHours(0, 0, 0, 0);

    // chevauchement
    return from < rTo && to > rFrom;
  });

  if (conflict) {

    const siteName = this.getSiteName(conflict.siteCampingId);

    return `Vous avez déjà une réservation active au camping "${siteName}" pendant cette période. Impossible de réserver deux campings aux mêmes dates.`;
  }

  return '';
}

}