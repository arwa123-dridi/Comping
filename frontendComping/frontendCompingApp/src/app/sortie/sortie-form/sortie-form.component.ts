// src/app/sortie/sortie-form/sortie-form.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieRequest } from '../../models/sortie.model';

@Component({
    selector: 'app-sortie-form',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './sortie-form.component.html',
    styleUrls: ['./sortie-form.component.css']
})
export class SortieFormComponent implements OnInit {
    sortie: SortieRequest = {
        titre: '',
        description: '',
        dateDebut: new Date(),
        dateFin: undefined,
        lieuDepart: '',
        lieuArrivee: '',
        region: '',
        difficulte: 'MOYEN',
        capaciteMax: 10,
        prixParPersonne: 0,
        equipementRequis: '',
        assistanceMedicale: false,
        organisateurId: localStorage.getItem('userId') || '',
        organisateurNom: localStorage.getItem('userNom') || 'Organisateur',
        equipeId: '',
        distanceKm: 0
    };
    
    isEdit = false;
    sortieId: string | null = null;
    loading = false;
    error: string | null = null;
    difficulteOptions = ['FACILE', 'MOYEN', 'DIFFICILE'];

    constructor(
        private sortieService: SortieService,
        private router: Router,
        private route: ActivatedRoute
    ) {}

    ngOnInit(): void {
        this.sortieId = this.route.snapshot.paramMap.get('id');
        if (this.sortieId) {
            this.isEdit = true;
            this.loadSortie();
        }
    }

    loadSortie(): void {
        this.loading = true;
        this.sortieService.getSortieById(this.sortieId!).subscribe({
            next: (data) => {
                this.sortie = {
                    titre: data.titre,
                    description: data.description,
                    dateDebut: new Date(data.dateDebut),
                    dateFin: data.dateFin ? new Date(data.dateFin) : undefined,
                    lieuDepart: data.lieuDepart,
                    lieuArrivee: data.lieuArrivee || '',
                    region: data.region || '',
                    difficulte: data.difficulte,
                    capaciteMax: data.capaciteMax,
                    prixParPersonne: data.prixParPersonne,
                    equipementRequis: data.equipementRequis || '',
                    assistanceMedicale: data.assistanceMedicale || false,
                    organisateurId: data.organisateurId,
                    organisateurNom: data.organisateurNom,
                    equipeId: data.equipeId || '',
                    distanceKm: data.distanceKm || 0
                };
                this.loading = false;
            },
            error: (err) => {
                this.error = 'Erreur chargement';
                this.loading = false;
            }
        });
    }

    onSubmit(): void {
        if (!this.isValid()) return;
        
        this.loading = true;
        
        if (this.isEdit && this.sortieId) {
            this.sortieService.updateSortie(this.sortieId, this.sortie).subscribe({
                next: () => {
                    this.router.navigate(['/sorties', this.sortieId]);
                },
                error: (err) => {
                    this.error = 'Erreur mise à jour';
                    this.loading = false;
                }
            });
        } else {
            this.sortieService.createSortie(this.sortie).subscribe({
                next: (created) => {
                    this.router.navigate(['/sorties', created.id]);
                },
                error: (err) => {
                    this.error = 'Erreur création';
                    this.loading = false;
                }
            });
        }
    }

    isValid(): boolean {
        if (!this.sortie.titre?.trim()) return false;
        if (!this.sortie.description?.trim()) return false;
        if (!this.sortie.lieuDepart?.trim()) return false;
        if (!this.sortie.dateDebut) return false;
        if (!this.sortie.capaciteMax || this.sortie.capaciteMax < 1) return false;
        return true;
    }
}