import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieRequest, SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';

@Component({
  selector: 'app-sortie-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './sortie-form.component.html',
  styleUrls: ['./sortie-form.component.css']
})
export class SortieFormComponent implements OnInit {
  sortieForm: FormGroup;
  isEdit = false;
  sortieId: string | null = null;
  isLoading = false;
  error: string | null = null;

  equipes: EquipeResponse[] = [];
  equipesLoading = false;

  constructor(
    private fb: FormBuilder,
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.sortieForm = this.fb.group({
      titre:             ['', [Validators.required, Validators.minLength(3)]],
      description:       ['', [Validators.required, Validators.minLength(10)]],
      dateDebut:         ['', Validators.required],
      dateFin:           [''],
      lieuDepart:        ['', Validators.required],
      lieuArrivee:       [''],
      region:            [''],
      difficulte:        ['MOYEN', Validators.required],
      capaciteMax:       [10,  [Validators.required, Validators.min(1), Validators.max(50)]],
      prixParPersonne:   [0,   Validators.min(0)],
      equipementRequis:  [''],
      assistanceMedicale:[false],
      equipeId:          ['', Validators.required],
      distanceKm:        [0,   Validators.min(0)],
      organisateurId:    [localStorage.getItem('userId') || ''],
      organisateurNom:   [localStorage.getItem('userNom') || 'Organisateur']
    });
  }

  ngOnInit(): void {
    this.loadEquipes();
    this.sortieId = this.route.snapshot.paramMap.get('id');
    if (this.sortieId) {
      this.isEdit = true;
      this.loadSortie();
    }
  }

  get f() { return this.sortieForm.controls; }

  // ── Sélection équipe (depuis la grille visuelle) ──────────
  selectEquipe(equipeId: string): void {
    this.sortieForm.patchValue({ equipeId });
    this.sortieForm.get('equipeId')?.markAsTouched();
  }

  // ── Chargement des équipes ────────────────────────────────
  loadEquipes(): void {
    this.equipesLoading = true;
    this.equipeService.getAllEquipes().subscribe({
      next: (data) => { this.equipes = data || []; this.equipesLoading = false; },
      error: () => { this.equipesLoading = false; }
    });
  }

  // ── Chargement sortie en mode édition ────────────────────
  loadSortie(): void {
    this.isLoading = true;
    this.sortieService.getSortieById(this.sortieId!).subscribe({
      next: (data: SortieResponse) => {
        const fmt = (d: any): string => {
          if (!d) return '';
          return new Date(d).toISOString().slice(0, 16);
        };
        this.sortieForm.patchValue({
          titre:             data.titre,
          description:       data.description,
          dateDebut:         fmt(data.dateDebut),
          dateFin:           fmt(data.dateFin),
          lieuDepart:        data.lieuDepart,
          lieuArrivee:       data.lieuArrivee || '',
          region:            data.region || '',
          difficulte:        data.difficulte,
          capaciteMax:       data.capaciteMax,
          prixParPersonne:   data.prixParPersonne || 0,
          equipementRequis:  data.equipementRequis || '',
          assistanceMedicale:data.assistanceMedicale || false,
          equipeId:          data.equipeId || '',
          distanceKm:        data.distanceKm || 0,
          organisateurId:    data.organisateurId,
          organisateurNom:   data.organisateurNom
        });
        this.isLoading = false;
      },
      error: () => { this.error = 'Impossible de charger la randonnée.'; this.isLoading = false; }
    });
  }

  // ── Soumission ────────────────────────────────────────────
  onSubmit(): void {
    if (this.sortieForm.invalid) {
      this.sortieForm.markAllAsTouched();
      this.error = 'Veuillez corriger les erreurs avant de continuer.';
      return;
    }

    this.isLoading = true;
    this.error = null;

    const v = this.sortieForm.value;
    const sortieData: SortieRequest = {
      titre:             v.titre,
      description:       v.description,
      dateDebut:         new Date(v.dateDebut),
      dateFin:           v.dateFin ? new Date(v.dateFin) : undefined,
      lieuDepart:        v.lieuDepart,
      lieuArrivee:       v.lieuArrivee,
      region:            v.region,
      difficulte:        v.difficulte,
      capaciteMax:       Number(v.capaciteMax),
      prixParPersonne:   Number(v.prixParPersonne),
      equipementRequis:  v.equipementRequis,
      assistanceMedicale:v.assistanceMedicale,
      equipeId:          v.equipeId,
      distanceKm:        Number(v.distanceKm),
      organisateurId:    localStorage.getItem('userId') || '',
      organisateurNom:   localStorage.getItem('userNom') || 'Organisateur'
    };

    const request$ = this.isEdit && this.sortieId
      ? this.sortieService.updateSortie(this.sortieId, sortieData)
      : this.sortieService.createSortie(sortieData);

    request$.subscribe({
      next: (result) => {
        this.isLoading = false;
        const id = result.id || this.sortieId;
        this.router.navigate(['/sorties', id]);
      },
      error: (err) => {
        this.error = err.error?.message || (this.isEdit ? 'Erreur mise à jour.' : 'Erreur création.');
        this.isLoading = false;
      }
    });
  }
}