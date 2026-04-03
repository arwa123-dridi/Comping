// src/app/sortie/sortie-edit/sortie-edit.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieRequest, SortieResponse } from '../../models/sortie.model';

@Component({
  selector: 'app-sortie-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './sortie-edit.component.html',
  styleUrls: ['./sortie-edit.component.css']
})
export class SortieEditComponent implements OnInit {
  
  sortieId: string = '';
  sortie: SortieRequest = {
    titre: '',
    description: '',
    dateDebut: '',
    dateFin: '',
    lieuDepart: '',
    region: '',
    difficulte: 'MOYEN',
    capaciteMax: 10,
    prixParPersonne: 0,
    equipementRequis: '',
    assistanceMedicale: false,
    organisateurId: '',
    organisateurNom: '',
    equipeId: ''
  };

  loading = false;
  error: string | null = null;

  difficulteOptions = ['FACILE', 'MOYEN', 'DIFFICILE'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sortieService: SortieService
  ) {}

  ngOnInit(): void {
    this.sortieId = this.route.snapshot.paramMap.get('id') || '';
    if (this.sortieId) {
      this.loadSortie();
    } else {
      this.error = 'ID de randonnée non trouvé';
      this.router.navigate(['/sorties']);
    }
  }

  loadSortie(): void {
    this.loading = true;
    this.sortieService.getSortieById(this.sortieId).subscribe({
      next: (data: SortieResponse) => {
        // Convertir les dates en format string pour l'input datetime-local
        const dateDebutStr = this.formatDateForInput(data.dateDebut);
        const dateFinStr = data.dateFin ? this.formatDateForInput(data.dateFin) : '';
        
        this.sortie = {
          titre: data.titre,
          description: data.description,
          dateDebut: dateDebutStr,
          dateFin: dateFinStr,
          lieuDepart: data.lieuDepart,
          region: data.region || '',
          difficulte: data.difficulte,
          capaciteMax: data.capaciteMax,
          prixParPersonne: data.prixParPersonne || 0,
          equipementRequis: data.equipementRequis || '',
          assistanceMedicale: data.assistanceMedicale || false,
          organisateurId: data.organisateurId,
          organisateurNom: data.organisateurNom,
          equipeId: data.equipeId || ''
        };
        this.loading = false;
      },
      error: (err: unknown) => {
        this.error = 'Erreur lors du chargement de la randonnée';
        this.loading = false;
        console.error(err);
      }
    });
  }

  //  Méthode pour formater la date en format YYYY-MM-DDThh:mm
  formatDateForInput(date: any): string {
    if (!date) return '';
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  onSubmit(): void {
    // Validation
    if (!this.sortie.titre?.trim()) {
      this.error = 'Le titre est obligatoire';
      return;
    }
    if (!this.sortie.description?.trim()) {
      this.error = 'La description est obligatoire';
      return;
    }
    if (!this.sortie.lieuDepart?.trim()) {
      this.error = 'Le lieu de départ est obligatoire';
      return;
    }
    if (!this.sortie.dateDebut) {
      this.error = 'La date de début est obligatoire';
      return;
    }
    if (!this.sortie.capaciteMax || this.sortie.capaciteMax < 1) {
      this.error = 'La capacité maximale doit être au moins 1';
      return;
    }

    this.loading = true;
    this.error = null;
    
    // Convertir les dates en objets Date avant l'envoi
    const sortieToSend = {
      ...this.sortie,
      dateDebut: new Date(this.sortie.dateDebut),
      dateFin: this.sortie.dateFin ? new Date(this.sortie.dateFin) : undefined
    };
    
    this.sortieService.updateSortie(this.sortieId, sortieToSend).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/sorties', this.sortieId]);
      },
      error: (err: unknown) => {
        this.loading = false;
        this.error = 'Erreur lors de la modification de la randonnée';
        console.error(err);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/sorties', this.sortieId]);
  }
}