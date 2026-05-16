import { Component } from '@angular/core';
import { UserProfileEvent } from '../../models/UserPrrofileEvent.model';
import { EventRecommandation } from '../../models/ai-recommendationEvent.model';
import { AiRecommendationEvent } from '../../services/ai-recommendationEvent';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { EventService } from '../../services/event.service';
import { Event as EventModel } from '../../models/event.model';
import { RouterLink } from "@angular/router";
@Component({
  selector: 'app-recommendation-event',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './recommendation-event.html',
  styleUrl: './recommendation-event.css',
})
export class RecommendationEvent {

   userProfile: UserProfileEvent = {
    userId: '',
    age: 0,
    interests: [],
    niveauExperience: 'debutant',
    localisation: '',
    budget: 0,
    meteo: 'ensoleille',
    saison: 'ete'
  };
   recommendations: EventRecommandation[] = [];
  isLoading = false;
  errorMessage = '';
  hasSearched = false;
  selectedEvent: EventModel | null = null;
  isLoadingDetails = false;
   constructor(private aiService: AiRecommendationEvent,
    private eventService: EventService
   ) {}
   
    getRecommendations(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.hasSearched = true;

    this.aiService.recommendEvents(this.userProfile).subscribe({
      next: (result) => {
        this.recommendations = result.recommendations;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors de la recommandation.';
        this.isLoading = false;
        console.error(err);
      }
    });
  }
   openDetails(idEvent: string): void {
    this.isLoadingDetails = true;
    document.body.style.overflow = 'hidden';
    this.eventService.getEventById(idEvent).subscribe({
      next: (event) => {
        this.selectedEvent = event;
        this.isLoadingDetails = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoadingDetails = false;
      }
    });
  }

  closeDetails(): void {
    this.selectedEvent = null;
    document.body.style.overflow = 'auto';
  }

  getStatutClass(statut: string): string {
    switch (statut) {
      case 'VALIDE'  : return 'badge-green';
      case 'TERMINE' : return 'badge-orange';
      case 'ANNULE'  : return 'badge-red';
      default        : return '';
    }
  }

  getTopBarClass(statut: string): string {
    switch (statut) {
      case 'VALIDE'  : return 'bar-green';
      case 'TERMINE' : return 'bar-orange';
      case 'ANNULE'  : return 'bar-red';
      default        : return '';
    }
  }
}
