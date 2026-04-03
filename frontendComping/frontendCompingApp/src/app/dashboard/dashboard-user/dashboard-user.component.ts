// src/app/dashboard/dashboard-user/dashboard-user.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';
import { EquipeService } from '../../services/equipe.service';
import { EquipeResponse } from '../../models/equipe.model';

@Component({
    selector: 'app-dashboard-user',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './dashboard-user.component.html',
    styleUrls: ['./dashboard-user.component.css']
})
export class DashboardUserComponent implements OnInit {
    
    // Infos utilisateur
    userName: string = '';
    userEmail: string = '';
    userRole: string = '';
    
    // Statistiques
    totalSortiesInscrites: number = 0;
    totalEquipesMembre: number = 0;
    totalSortiesCreees: number = 0;
    
    // Données
    mesSortiesInscrites: SortieResponse[] = [];
    mesEquipes: EquipeResponse[] = [];
    mesSortiesCreees: SortieResponse[] = [];
    
    // Participants aux sorties que j'ai créées
    participantsParSortie: Map<string, number> = new Map();
    
    loading = {
        sorties: false,
        equipes: false,
        stats: false
    };
    
    error: string | null = null;

    constructor(
        private sortieService: SortieService,
        private equipeService: EquipeService,
        private router: Router
    ) {}

    ngOnInit(): void {
        this.loadUserInfo();
        this.loadDashboardData();
    }

    loadUserInfo(): void {
        this.userName = localStorage.getItem('userNom') || 'Campeur';
        this.userEmail = localStorage.getItem('userEmail') || '';
        this.userRole = localStorage.getItem('userRole') || 'USER';
    }

    loadDashboardData(): void {
        this.loadMesSortiesInscrites();
        this.loadMesEquipes();
        this.loadMesSortiesCreees();
    }

    loadMesSortiesInscrites(): void {
        this.loading.sorties = true;
        this.sortieService.getAllSorties().subscribe({
            next: (sorties) => {
                const userId = localStorage.getItem('userId');
                this.mesSortiesInscrites = sorties.filter(s => 
                    s.participantIds?.includes(userId || '')
                );
                this.totalSortiesInscrites = this.mesSortiesInscrites.length;
                this.loading.sorties = false;
            },
            error: (err) => {
                console.error('Erreur chargement sorties:', err);
                this.loading.sorties = false;
            }
        });
    }

    loadMesEquipes(): void {
        this.loading.equipes = true;
        this.equipeService.getAllEquipes().subscribe({
            next: (equipes) => {
                const userId = localStorage.getItem('userId');
                this.mesEquipes = equipes.filter(e => 
                    e.membres?.some(m => m.id === userId)
                );
                this.totalEquipesMembre = this.mesEquipes.length;
                this.loading.equipes = false;
            },
            error: (err) => {
                console.error('Erreur chargement equipes:', err);
                this.loading.equipes = false;
            }
        });
    }

    loadMesSortiesCreees(): void {
        this.loading.stats = true;
        this.sortieService.getAllSorties().subscribe({
            next: (sorties) => {
                const userId = localStorage.getItem('userId');
                this.mesSortiesCreees = sorties.filter(s => s.organisateurId === userId);
                this.totalSortiesCreees = this.mesSortiesCreees.length;
                
                // Calculer participants par sortie
                this.mesSortiesCreees.forEach(sortie => {
                    this.participantsParSortie.set(sortie.id, sortie.nombreParticipants || 0);
                });
                this.loading.stats = false;
            },
            error: (err) => {
                console.error('Erreur:', err);
                this.loading.stats = false;
            }
        });
    }

    voirSortie(id: string): void {
        this.router.navigate(['/sorties', id]);
    }

    voirEquipe(id: string): void {
        this.router.navigate(['/equipes', id]);
    }

    creerSortie(): void {
        this.router.navigate(['/sorties/create']);
    }

    getStatutClass(statut: string): string {
        switch(statut) {
            case 'PLANIFIEE': return 'bg-success';
            case 'EN_COURS': return 'bg-warning';
            case 'TERMINEE': return 'bg-secondary';
            default: return 'bg-info';
        }
    }

    getDifficulteClass(difficulte: string): string {
        switch(difficulte) {
            case 'FACILE': return 'text-success';
            case 'MOYEN': return 'text-warning';
            case 'DIFFICILE': return 'text-danger';
            default: return 'text-secondary';
        }
    }
}