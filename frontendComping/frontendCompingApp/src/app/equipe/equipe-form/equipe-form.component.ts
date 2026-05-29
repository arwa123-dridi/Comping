import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { EquipeService } from '../../services/equipe.service';
import { EquipeRequest } from '../../models/equipe.model';

@Component({
  selector: 'app-equipe-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './equipe-form.component.html',
  styleUrls: ['./equipe-form.component.css']
})
export class EquipeFormComponent implements OnInit {

  equipeForm: FormGroup;
  isEdit = false;
  equipeId: string | null = null;
  loading = false;
  error: string | null = null;

  niveaux = ['Tous niveaux', 'Débutant', 'Intermédiaire', 'Avancé'];

  constructor(
    private fb: FormBuilder,
    private equipeService: EquipeService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.equipeForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      nbMembresMax: [10, [Validators.required, Validators.min(2), Validators.max(30)]],
      niveau: ['Tous niveaux', Validators.required]
    });
  }

  ngOnInit(): void {
    // ✅ RESTRICTION: Seuls les organisateurs peuvent créer/modifier des équipes
    const userRole = localStorage.getItem('userRole');
    if (userRole !== 'ORGANISATEUR' && userRole !== 'ROLE_ORGANISATEUR' && userRole !== 'ADMIN') {
      console.warn('⛔ Accès refusé: organisateurs uniquement');
      this.router.navigate(['/equipes']);
      return;
    }

    this.equipeId = this.route.snapshot.paramMap.get('id');
    if (this.equipeId) {
      this.isEdit = true;
      this.loadEquipe();
    }
  }

  loadEquipe(): void {
    this.loading = true;
    this.equipeService.getEquipeById(this.equipeId!).subscribe({
      next: (data) => {
        this.equipeForm.patchValue({
          nom: data.nom,
          description: data.description || '',
          nbMembresMax: data.nbMembresMax || 10,
          niveau: data.niveau || 'Tous niveaux'
        });
        this.loading = false;
      },
      error: () => { 
        this.error = 'Erreur lors du chargement.'; 
        this.loading = false; 
      }
    });
  }

  onSubmit(): void {
  if (this.equipeForm.invalid) {
    this.error = 'Veuillez corriger les erreurs dans le formulaire.';
    return;
  }

  const userId = localStorage.getItem('userId');
  const userNom = localStorage.getItem('userNom');

  if (!userId) {
    this.error = "Vous devez être connecté pour créer une équipe";
    return;
  }

  const equipeData: EquipeRequest = {
    nom: this.equipeForm.value.nom,
    description: this.equipeForm.value.description,
    nbMembresMax: this.equipeForm.value.nbMembresMax,
    niveau: this.equipeForm.value.niveau,
    organisateurId: userId,
    organisateurNom: userNom || 'Utilisateur'
  };

  this.loading = true;

  const request$ = this.isEdit && this.equipeId
    ? this.equipeService.updateEquipe(this.equipeId, equipeData)
    : this.equipeService.createEquipe(equipeData);

  request$.subscribe({
    next: (result) => {
      this.loading = false;
      const id = (result as any).id || this.equipeId;
      this.router.navigate(['/admin/equipes', id]);
    },
    error: (err) => {
      console.log("BACKEND ERROR:", err);
      this.error = err.error?.message || 'Erreur création équipe';
      this.loading = false;
    }
  });
}
    
}
