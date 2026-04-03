// src/app/sortie/sortie-list/sortie-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';

@Component({
    selector: 'app-sortie-list',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './sortie-list.component.html',
    styleUrls: ['./sortie-list.component.css']
})
export class SortieListComponent implements OnInit {
    sorties: SortieResponse[] = [];
    loading: boolean = false;
    error: string | null = null;
    userId: string | null = null;

    constructor(
        private sortieService: SortieService,
        private router: Router
    ) {}

    ngOnInit(): void {
        this.userId = localStorage.getItem('userId');
        this.loadSorties();
    }

    loadSorties(): void {
        this.loading = true;
        this.sortieService.getAllSorties().subscribe({
            next: (data: SortieResponse[]) => {
                this.sorties = data;
                this.loading = false;
            },
            error: (err) => {
                this.error = 'Erreur lors du chargement';
                this.loading = false;
                console.error(err);
            }
        });
    }

    viewDetail(id: string): void {
        this.router.navigate(['/sorties', id]);
    }
 
    
    inscrire(sortieId: string): void {
        if (!this.userId) {
            alert('Veuillez vous connecter');
            this.router.navigate(['/signup']);
            return;
        }
        
        this.sortieService.inscrire(sortieId).subscribe({
            next: () => {
                alert('Inscription réussie !');
                this.loadSorties();
            },
            error: (err) => {
                alert(err.error?.message || 'Erreur lors de l\'inscription');
            }
        });
    }

    isOrganisateur(organisateurId: string): boolean {
        return this.userId === organisateurId;
    }
    
    getDifficultyClass(difficulte: string): string {
        switch(difficulte) {
            case 'FACILE': return 'bg-success';
            case 'MOYEN': return 'bg-warning';
            case 'DIFFICILE': return 'bg-danger';
            default: return 'bg-secondary';
        }
    }
    sortiesByDifficulte(difficulte: string): SortieResponse[] {
  return this.sorties.filter(s => s.difficulte === difficulte);
} 

}