import { Component, OnInit } from '@angular/core';
import { EventService } from '../../services/event.service';
import { CommonModule } from '@angular/common';
import { Event as AppEvent } from '../../models/event.model';
import { Router, RouterLink } from '@angular/router';
import { EditeEventComponent } from '../edite-event/edite-event.component';


@Component({
  selector: 'app-list-event',
   standalone: true,
  imports: [CommonModule, EditeEventComponent,RouterLink],
  templateUrl: './list-event.component.html',
  styleUrl: './list-event.component.css'
})
export class ListEventComponent implements OnInit  {
   events: AppEvent[] = [];
   selectedEvent: any = null;
  StatutEvent = String;
  validCount = 12;
termineCount = 5;
annuleCount = 2;
 
  constructor(private eventService: EventService,
     private router: Router
  ) {}
 
  ngOnInit(): void {
    this.eventService.getAllEvents().subscribe({
      next: (data) => this.events = data,
      error: (err) => console.error('Erreur chargement événements', err)
    });
  }
 
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
}
