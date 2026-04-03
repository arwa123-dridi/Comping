// src/app/sortie/sortie-create/sortie-create.component.ts

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieRequest } from '../../models/sortie.model';

@Component({
  selector: 'app-sortie-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './sortie-create.component.html',
  styleUrls: ['./sortie-create.component.css']
})
export class SortieCreateComponent {
  
  sortie: SortieRequest = {
    titre: '',
    description: '',
    dateDebut: new Date(),
    dateFin: undefined,
    lieuDepart: '',
    region: '',
    difficulte: 'MOYEN',
    capaciteMax: 10,
    prixParPersonne: 0,
    equipementRequis: '',
    assistanceMedicale: false,
    organisateurId: localStorage.getItem('userId') || '',
    organisateurNom: localStorage.getItem('userNom') || '',
    equipeId: ''
  };

  loading = false;
  error: string | null = null;

  difficulteOptions = ['FACILE', 'MOYEN', 'DIFFICILE'];

  constructor(
    private sortieService: SortieService,
    private router: Router
  ) {}

  onSubmit(): void {
    if (!this.isValid()) return;

    this.loading = true;
    this.sortieService.createSortie(this.sortie).subscribe({
      next: (created) => {
        this.loading = false;
        this.router.navigate(['/sorties', created.id]);
      },
      error: (err: unknown) => {
        this.loading = false;
        this.error = 'Erreur lors de la création';
        console.error(err);
      }
    });
  }

  isValid(): boolean {
    if (!this.sortie.titre) return false;
    if (!this.sortie.description) return false;
    if (!this.sortie.dateDebut) return false;
    if (!this.sortie.lieuDepart) return false;
    if (!this.sortie.capaciteMax || this.sortie.capaciteMax < 1) return false;
    return true;
  }

  cancel(): void {
    this.router.navigate(['/sorties']);
  }
}