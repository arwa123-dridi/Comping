// src/app/equipe/equipe-list/equipe-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { EquipeService } from '../../services/equipe.service';
import { EquipeResponse } from '../../models/equipe.model';

@Component({
    selector: 'app-equipe-list',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './equipe-list.component.html',
    styleUrls: ['./equipe-list.component.css']
})
export class EquipeListComponent implements OnInit {
    equipes: EquipeResponse[] = [];
    loading = false;
    error: string | null = null;
    userId: string | null = null;

    constructor(
        private equipeService: EquipeService,
        private router: Router
    ) {}

    ngOnInit(): void {
        this.userId = localStorage.getItem('userId');
        this.loadEquipes();
    }

    loadEquipes(): void {
        this.loading = true;
        this.equipeService.getAllEquipes().subscribe({
            next: (data) => {
                this.equipes = data;
                this.loading = false;
            },
            error: (err) => {
                this.error = 'Erreur chargement';
                this.loading = false;
            }
        });
    }

    isMembre(equipe: EquipeResponse): boolean {
        return equipe.membres?.some(m => m.id === this.userId) ?? false;
    }

    isOrganisateur(equipe: EquipeResponse): boolean {
        return equipe.organisateurId === this.userId;
    }

    rejoindre(equipeId: string): void {
        if (!this.userId) {
            alert('Connectez-vous');
            this.router.navigate(['/signup']);
            return;
        }
        
        this.equipeService.ajouterMembre(equipeId, this.userId, localStorage.getItem('userNom') || '').subscribe({
            next: () => this.loadEquipes(),
            error: (err) => alert(err.error?.message || 'Erreur')
        });
    }

    voirDetail(id: string): void {
        this.router.navigate(['/equipes', id]);
    }
}