// src/app/sortie/sortie-detail/sortie-detail.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';

@Component({
    selector: 'app-sortie-detail',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './sortie-detail.component.html',
    styleUrls: ['./sortie-detail.component.css']
})
export class SortieDetailComponent implements OnInit {
    sortie: SortieResponse | null = null;
    loading: boolean = true;
    error: string | null = null;
    inscriptionEnCours: boolean = false;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private sortieService: SortieService
    ) {}

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.loadSortie(id);
        } else {
            this.error = 'ID non trouvé';
            this.loading = false;
        }
    }

    loadSortie(id: string): void {
        this.loading = true;
        this.sortieService.getSortieById(id).subscribe({
            next: (data) => {
                this.sortie = data;
                this.loading = false;
            },
            error: (err) => {
                this.error = 'Impossible de charger';
                this.loading = false;
                console.error(err);
            }
        });
    }

    get isParticipant(): boolean {
        const userId = localStorage.getItem('userId');
        return this.sortie?.participantIds?.includes(userId ?? '') ?? false;
    }

    get isOrganisateur(): boolean {
        const userId = localStorage.getItem('userId');
        return this.sortie?.organisateurId === userId;
    }

    // ✅ AJOUT : Méthode pour calculer la durée
    getDuree(): string {
        if (!this.sortie?.dateDebut || !this.sortie?.dateFin) return 'Non spécifiée';
        const debut = new Date(this.sortie.dateDebut);
        const fin = new Date(this.sortie.dateFin);
        const diffHeures = Math.round((fin.getTime() - debut.getTime()) / (1000 * 60 * 60));
        return `${diffHeures} heures`;
    }

    inscrire(): void {
        if (!this.sortie) return;
        this.inscriptionEnCours = true;
        this.sortieService.inscrire(this.sortie.id).subscribe({
            next: () => {
                this.loadSortie(this.sortie!.id);
                this.inscriptionEnCours = false;
            },
            error: (err) => {
                alert(err.error?.message || 'Erreur');
                this.inscriptionEnCours = false;
            }
        });
    }

    desinscrire(): void {
        if (!this.sortie) return;
        this.sortieService.desinscrire(this.sortie.id).subscribe({
            next: () => this.loadSortie(this.sortie!.id),
            error: (err) => alert('Erreur désinscription')
        });
    }

    modifier(): void {
        this.router.navigate(['/sorties/edit', this.sortie?.id]);
    }

    supprimer(): void {
        if (!this.sortie || !confirm('Supprimer cette randonnée ?')) return;
        this.sortieService.deleteSortie(this.sortie.id).subscribe({
            next: () => this.router.navigate(['/sorties']),
            error: (err) => alert('Erreur suppression')
        });
    }
}