// src/app/equipe/equipe-form/equipe-form.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { EquipeService } from '../../services/equipe.service';
import { EquipeRequest } from '../../models/equipe.model';

@Component({
    selector: 'app-equipe-form',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './equipe-form.component.html'
})
export class EquipeFormComponent implements OnInit {
    equipe: EquipeRequest = {
        nom: '',
        description: '',
        nbMembresMax: 10,
        niveau: 'Tous niveaux',
        organisateurId: localStorage.getItem('userId') || '',
        organisateurNom: localStorage.getItem('userNom') || ''
    };
    
    isEdit = false;
    equipeId: string | null = null;
    loading = false;
    error: string | null = null;

    constructor(
        private equipeService: EquipeService,
        private router: Router,
        private route: ActivatedRoute
    ) {}

    ngOnInit(): void {
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
                this.equipe = {
                    nom: data.nom,
                    description: data.description || '',
                    nbMembresMax: data.nbMembresMax,
                    niveau: data.niveau || 'Tous niveaux',
                    organisateurId: data.organisateurId,
                    organisateurNom: data.organisateurNom
                };
                this.loading = false;
            },
            error: () => {
                this.error = 'Erreur chargement';
                this.loading = false;
            }
        });
    }

    onSubmit(): void {
        if (!this.equipe.nom?.trim()) return;
        
        this.loading = true;
        
        if (this.isEdit && this.equipeId) {
            this.equipeService.updateEquipe(this.equipeId, this.equipe).subscribe({
                next: () => this.router.navigate(['/equipes', this.equipeId]),
                error: () => {
                    this.error = 'Erreur mise à jour';
                    this.loading = false;
                }
            });
        } else {
            this.equipeService.createEquipe(this.equipe).subscribe({
                next: (created) => this.router.navigate(['/equipes', created.id]),
                error: () => {
                    this.error = 'Erreur création';
                    this.loading = false;
                }
            });
        }
    }
}