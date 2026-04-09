// src/app/dashboard/dashboard-admin/dashboard-admin.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieResponse } from '../../models/sortie.model';

@Component({
    selector: 'app-dashboard-admin',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './dashboard-admin.component.html',
    styleUrls: ['./dashboard-admin.component.css']
})
export class DashboardAdminComponent implements OnInit {
    sorties: SortieResponse[] = [];
    equipes: any[] = [];
    stats = {
        totalSorties: 0,
        totalParticipants: 0,
        totalEquipes: 0,
        sortiesValidees: 0,
        sortiesEnAttente: 0
    };
    
    loading = false;

    constructor(
        private sortieService: SortieService,
        private equipeService: EquipeService
    ) {}

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        
        this.sortieService.getAllSorties().subscribe({
            next: (sorties) => {
                this.sorties = sorties;
                this.stats.totalSorties = sorties.length;
                this.stats.totalParticipants = sorties.reduce((sum, s) => sum + (s.nombreParticipants || 0), 0);
                this.stats.sortiesValidees = sorties.filter(s => s.statut === 'PLANIFIEE').length;
                this.loading = false;
            },
            error: (err) => {
                console.error(err);
                this.loading = false;
            }
        });
        
        this.equipeService.getAllEquipes().subscribe({
            next: (equipes) => {
                this.equipes = equipes;
                this.stats.totalEquipes = equipes.length;
            },
            error: (err) => console.error(err)
        });
    }

    getNombreParticipants(s: SortieResponse): number {
  return (s as any).participantIds?.length || (s as any).participants?.length || 0;
}

getSortieStatut(s: SortieResponse): string {
  const today = new Date();
  const debut = new Date(s.dateDebut);
  const fin = s.dateFin ? new Date(s.dateFin) : null;
  if (debut > today) return 'PLANIFIEE';
  if (fin && fin < today) return 'TERMINEE';
  if (debut <= today && (!fin || fin >= today)) return 'EN_COURS';
  return 'PLANIFIEE';
}


    deleteSortie(id: string): void {
        if (confirm('Confirmer la suppression de cette sortie?')) {
            this.sortieService.deleteSortie(id).subscribe({
                next: () => {
                    this.loadData(); // Reload
                    alert('Sortie supprimée');
                },
                error: (err) => alert('Erreur: ' + err.error.message)
            });
        }
    }

    // ✅ Méthode pour calculer le top organisateurs
    getTopOrganisateurs(): any[] {
        const organisateursMap = new Map<string, { nom: string; nbSorties: number; nbParticipants: number }>();
        
        this.sorties.forEach(sortie => {
            const orgId = sortie.organisateurId;
            if (organisateursMap.has(orgId)) {
                const org = organisateursMap.get(orgId)!;
                org.nbSorties++;
                org.nbParticipants += sortie.nombreParticipants || 0;
            } else {
                organisateursMap.set(orgId, {
                    nom: sortie.organisateurNom,
                    nbSorties: 1,
                    nbParticipants: sortie.nombreParticipants || 0
                });
            }
        });
        
        // Trier par nombre de sorties (décroissant) et prendre les 5 premiers
        return Array.from(organisateursMap.values())
            .sort((a, b) => b.nbSorties - a.nbSorties)
            .slice(0, 5);
    }

  refresh(): void {
    this.loadData();
  }
}
