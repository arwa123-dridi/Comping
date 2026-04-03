import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EquipeService } from '../../services/equipe.service';
import { EquipeResponse } from '../../models/equipe.model';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';

@Component({
    selector: 'app-equipe-detail',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './equipe-detail.component.html',
    styleUrls: ['./equipe-detail.component.css']
})
export class EquipeDetailComponent implements OnInit {
    equipe: EquipeResponse | null = null;
    sorties: SortieResponse[] = [];
    loading = false;
    error: string | null = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private equipeService: EquipeService,
        private sortieService: SortieService
    ) {}

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) this.loadEquipe(id);
        else this.router.navigate(['/equipes']);
    }

    loadEquipe(id: string): void {
        this.loading = true;
        this.equipeService.getEquipeById(id).subscribe({
            next: (data) => {
                this.equipe = data;
                this.loadSorties(id);
                this.loading = false;
            },
            error: (err) => {
                console.error(err);
                this.error = 'Impossible de charger l\'équipe';
                this.loading = false;
            }
        });
    }

    loadSorties(equipeId: string): void {
        this.sortieService.getAllSorties().subscribe({
            next: (data) => {
                this.sorties = data.filter(s => s.equipeId === equipeId);
            },
            error: (err) => console.error(err)
        });
    }

    rejoindre(): void {
        if (!this.equipe) return;
        const userId = localStorage.getItem('userId') || '';
        const userNom = localStorage.getItem('userNom') || 'Utilisateur';
        if (!userId) { this.router.navigate(['/signup']); return; }
        this.equipeService.ajouterMembre(this.equipe.id, userId, userNom).subscribe({
            next: () => this.loadEquipe(this.equipe!.id),
            error: (err) => alert(err.error?.message || 'Erreur')
        });
    }

    isOrganisateur(): boolean {
        const userId = localStorage.getItem('userId');
        return this.equipe?.organisateurId === userId;
    }

    isMembre(): boolean {
        const userId = localStorage.getItem('userId');
        return !!userId && !!this.equipe?.membres?.some(m => m.id === userId);
    }
}
