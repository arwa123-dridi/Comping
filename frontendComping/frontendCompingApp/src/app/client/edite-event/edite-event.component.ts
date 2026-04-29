import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivityService } from '../../services/activity.service';
import { EventService } from '../../services/event.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-edite-event',
  standalone: true, // Si tu es en Angular 17+
  imports: [CommonModule, FormsModule],
  templateUrl: './edite-event.component.html',
  styleUrl: './edite-event.component.css'
})
export class EditeEventComponent implements OnInit {
  // On reçoit l'événement à modifier du parent
  @Input() event: any = {
    titre: '',
    description: '',
    prix: 0,
    capacite: 0,
    statut: 'VALIDE',
    activities: []
  };

  // Liste de toutes les activités pour les checkboxes
  @Input() allActivities: any[] = [];

  // Événements pour communiquer avec le parent
  @Output() close = new EventEmitter<void>();
  @Output() saved = new EventEmitter<any>();

  constructor(
    private eventService: EventService,
    private activityService: ActivityService,
     private route: ActivatedRoute,
       private router: Router
  ) {}

  ngOnInit(): void {
  const id = this.route.snapshot.paramMap.get('id');

  if (id) {
    this.eventService.getEventById(id).subscribe({
      next: (data) => {
        this.event = data;
      },
      error: (err) => console.error(err)
    });
  }
  this.activityService.getAllActivities().subscribe({
    next: (data) => this.allActivities = data,
    error: (err) => console.error(err)
  });
}

  // Vérifie si une activité est déjà dans l'événement
  isActivitySelected(activityId: number): boolean {
    return this.event.activities?.some((a: any) => a.idActivity === activityId);
  }

  // Gère le cochage/décochage des activités
  onActivityChange(event: any, activity: any): void {
    if (!this.event.activities) this.event.activities = [];

    if (event.target.checked) {
      // Ajouter l'objet activité complet
      this.event.activities.push(activity);
    } else {
      // Retirer l'activité
      this.event.activities = this.event.activities.filter(
        (a: any) => a.idActivity !== activity.idActivity
      );
    }
  }

  // Fermer la modale
  closeModal(): void {
    this.close.emit();
  }

  // Sauvegarder les modifs
  onUpdate(): void {
    // On appelle le service de mise à jour
    this.eventService.updateEvent(this.event.idEvent, this.event).subscribe({
      next: (response) => {
        console.log('Mise à jour réussie', response);
        alert('✅ Événement modifié avec succès !');
          this.router.navigate(['/events/list']);
        this.saved.emit(response); // Notifie le parent du succès
        this.closeModal();
      },
      error: (err) => {
        console.error('Erreur lors de la mise à jour', err);
      }
    });
  }
}