import { Component, OnInit } from '@angular/core';
import { EventService } from '../../services/event.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Event as EventModel } from '../../models/event.model';

@Component({
  selector: 'app-event',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './event.component.html',
  styleUrl: './event.component.css'
})
export class EventComponent implements OnInit {

  events: EventModel[] = [];
  filteredEvents: EventModel[] = [];

  filterTitre = '';
  filterStatut = '';
  showDeletePopup = false;
  selectedEventId: string | null = null;

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.eventService.getAllEvents().subscribe({
      next: (data) => {
        this.events = data;
        this.applyFilters();
      },
      error: (err) => console.error('Erreur chargement events', err)
    });
  }

  applyFilters(): void {
    this.filteredEvents = this.events.filter(e => {
      const matchTitre = e.titre.toLowerCase().includes(this.filterTitre.toLowerCase());
      const matchStatut = this.filterStatut ? e.statut === this.filterStatut : true;
      return matchTitre && matchStatut;
    });
  }

  toggleStatut(event: EventModel): void {
    const newStatut = event.statut === 'ACTIF' ? 'INACTIF' : 'ACTIF';

    this.eventService.toggleStatut(event.idEvent!, newStatut).subscribe({
      next: () => {
        event.statut = newStatut;
        this.applyFilters();
      },
      error: (err) => console.error('Erreur toggle statut', err)
    });
  }

  openDeletePopup(id: string): void {
    this.selectedEventId = id;
    this.showDeletePopup = true;
  }

  confirmDelete(): void {
    if (!this.selectedEventId) return;

    this.eventService.deleteEvent(this.selectedEventId).subscribe({
      next: () => {
        this.events = this.events.filter(e => e.idEvent !== this.selectedEventId);
        this.applyFilters();
        this.closePopup();
      },
      error: (err) => console.error('Erreur suppression', err)
    });
  }

  closePopup(): void {
    this.showDeletePopup = false;
    this.selectedEventId = null;
  }

  getStatutClass(statut: string): string {
    switch (statut) {
      case 'ACTIF':    return 'badge-actif';
      case 'INACTIF':  return 'badge-inactif';
      case 'COMPLET':  return 'badge-complet';
      default:         return '';
    }
  }
}