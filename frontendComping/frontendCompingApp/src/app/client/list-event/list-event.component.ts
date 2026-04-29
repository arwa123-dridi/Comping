import { Component, OnInit } from '@angular/core';
import { EventService } from '../../services/event.service';
import { CommonModule } from '@angular/common';
import { Event as AppEvent } from '../../models/event.model';
import { Router, RouterLink } from '@angular/router';
import { EditeEventComponent } from '../edite-event/edite-event.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-list-event',
   standalone: true,
  imports: [CommonModule, EditeEventComponent,RouterLink, FormsModule],
  templateUrl: './list-event.component.html',
  styleUrl: './list-event.component.css'
})
export class ListEventComponent implements OnInit  {
  validCount = 0;
termineCount = 0;
annuleCount = 0;
  filters = {
  statut: '',
  lieu: '',
  prixMin: null as number | null,
  prixMax: null as number | null
};

allEvents: AppEvent[] = [];
   events: AppEvent[] = [];
   selectedEvent: any = null;
  StatutEvent = String;
 
  constructor(private eventService: EventService,
     private router: Router
  ) {}
 
  ngOnInit(): void {
    this.eventService.getAllEvents().subscribe({
      next: (data) => {
         this.allEvents = data;
        this.events = data;

      },
      error: (err) => console.error('Erreur chargement événements', err)
    });
     this.loadCounts();
  }

  loadCounts(): void {
  this.eventService.countValide().subscribe({
    next: (val) => this.validCount = val,
    error: (err) => console.error('Erreur countValide', err)
  });

  this.eventService.countTermine().subscribe({
    next: (val) => this.termineCount = val,
    error: (err) => console.error('Erreur countTermine', err)
  });

  this.eventService.countAnnule().subscribe({
    next: (val) => this.annuleCount = val,
    error: (err) => console.error('Erreur countAnnule', err)
  });}
 
 getStatutClass(statut: string): string {
  switch (statut) {
    case 'VALIDE': return 'badge-actif';
    case 'TERMINE': return 'badge-complet';
    case 'ANNULE':  return 'badge-inactif';
    default: return '';
  }
}
 
getTopBarClass(statut: string): string {
  switch (statut) {
    case 'VALIDE': return 'top-actif';
    case 'TERMINE': return 'top-complet';
    case 'ANNULE': return 'top-inactif';
    default: return '';
  }
}
editEvent(event: any): void {
  this.router.navigate(['events/edit', event.idEvent]);
}
deleteEvent(id: string | undefined): void {
  if (!id) return;

  this.eventService.deleteEvent(id).subscribe({
    next: () => {
      this.events = this.events.filter(e => e.idEvent !== id);
    }
  });
}
onCreateEvent() {
  this.router.navigate(['/events/add']);
    // Ici, tu peux ouvrir un modal ou rediriger vers une page de création
    // Exemple : this.router.navigate(['/creer-evenement']);
  }
  onCreateActivity() {
    this.router.navigate(['/activities/add']);
    // Ici, ton code pour créer une activité
  }


openDetails(event: any): void {
  this.selectedEvent = event;
}

closeDetails(): void {
  this.selectedEvent = null;
}
  applyFilters(): void {
  this.events = this.allEvents.filter(e => {

    const matchStatut =
      !this.filters.statut || e.statut === this.filters.statut;

    const matchLieu =
      !this.filters.lieu ||
      e.lieu?.toLowerCase().includes(this.filters.lieu.toLowerCase());

    const matchPrixMin =
      this.filters.prixMin == null || e.prix >= this.filters.prixMin;

    const matchPrixMax =
      this.filters.prixMax == null || e.prix <= this.filters.prixMax;

    return matchStatut && matchLieu && matchPrixMin && matchPrixMax;
  });
}
resetFilters(): void {
  this.filters = {
    statut: '',
    lieu: '',
    prixMin: null,
    prixMax: null
  };

  this.events = this.allEvents;
}

}
