import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { IncidentService } from '../../services/incident.service';
import { IncidentResponse } from '../../models/incident.model';

@Component({
  selector: 'app-mes-incidents',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './mes-incidents.html',
  styleUrls: ['./mes-incidents.css']
})
export class MesIncidentsComponent implements OnInit {

  incidents: IncidentResponse[] = [];
  loading = false;
  selected: IncidentResponse | null = null;

  constructor(
    private incidentService: IncidentService,
    private toastr: ToastrService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.incidentService.getMine().subscribe({
      next: (data) => {
        this.incidents = data;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Erreur lors du chargement de vos incidents');
        this.loading = false;
      }
    });
  }

  voirDetail(i: IncidentResponse): void {
    this.selected = i;
  }

  closeModal(): void {
    this.selected = null;
  }

  peutModifier(i: IncidentResponse): boolean {
    return i.statut === 'OUVERT';
  }

  modifier(i: IncidentResponse): void {
    this.router.navigate(['/dashboard/logistique/incidents/edit', i.idIncident]);
  }

  supprimer(i: IncidentResponse): void {
    if (!confirm('Supprimer cet incident ?')) return;
    this.incidentService.delete(i.idIncident).subscribe({
      next: () => {
        this.toastr.success('Incident supprime');
        this.load();
      },
      error: () => this.toastr.error('Erreur lors de la suppression')
    });
  }

  statutClass(statut: string): string {
    return {
      'OUVERT': 'st-open',
      'EN_COURS': 'st-progress',
      'RESOLU': 'st-resolved',
      'REJETE': 'st-rejected'
    }[statut] || 'st-open';
  }

  prioriteClass(p: string): string {
    return {
      'FAIBLE': 'pr-low',
      'MOYENNE': 'pr-medium',
      'HAUTE': 'pr-high',
      'CRITIQUE': 'pr-critical'
    }[p] || 'pr-low';
  }
}
