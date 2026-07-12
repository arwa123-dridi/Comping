import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { IncidentService } from '../../services/incident.service';
import { IncidentResponse, StatutIncident } from '../../models/incident.model';

@Component({
  selector: 'app-admin-incidents',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-incidents.html',
  styleUrls: ['./admin-incidents.css']
})
export class AdminIncidentsComponent implements OnInit {

  incidents: IncidentResponse[] = [];
  filtered: IncidentResponse[] = [];
  loading = false;

  searchText = '';
  selectedStatus = '';
  selectedPriorite = '';

  modalType: 'DETAIL' | 'TRAITEMENT' | null = null;
  selected: IncidentResponse | null = null;

  traitementStatut: StatutIncident | '' = '';
  traitementCommentaire = '';

  readonly transitions: Record<StatutIncident, StatutIncident[]> = {
    OUVERT: ['EN_COURS', 'RESOLU', 'REJETE'],
    EN_COURS: ['RESOLU', 'REJETE'],
    RESOLU: [],
    REJETE: []
  };

  constructor(private incidentService: IncidentService, private toastr: ToastrService) {}

  ngOnInit(): void {
    this.loadIncidents();
  }

  loadIncidents(): void {
    this.loading = true;
    this.incidentService.getAll().subscribe({
      next: (data) => {
        this.incidents = data;
        this.filtered = data;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Erreur chargement incidents');
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filtered = this.incidents.filter(i => {
      const matchStatus = this.selectedStatus ? i.statut === this.selectedStatus : true;
      const matchPriorite = this.selectedPriorite ? i.priorite === this.selectedPriorite : true;
      const matchSearch = this.searchText
        ? (i.type + ' ' + i.description).toLowerCase().includes(this.searchText.toLowerCase())
        : true;
      return matchStatus && matchPriorite && matchSearch;
    });
  }

  voirDetail(i: IncidentResponse): void {
    this.selected = i;
    this.modalType = 'DETAIL';
  }

  ouvrirTraitement(i: IncidentResponse): void {
    this.selected = i;
    this.traitementStatut = '';
    this.traitementCommentaire = i.commentaireOrganisateur || '';
    this.modalType = 'TRAITEMENT';
  }

  closeModal(): void {
    this.selected = null;
    this.modalType = null;
  }

  statutsDisponibles(): StatutIncident[] {
    if (!this.selected) return [];
    return this.transitions[this.selected.statut] || [];
  }

  confirmerTraitement(): void {
    if (!this.selected || !this.traitementStatut) {
      this.toastr.warning('Choisissez un statut');
      return;
    }
    this.incidentService.traiter(this.selected.idIncident, {
      statut: this.traitementStatut,
      commentaireOrganisateur: this.traitementCommentaire
    }).subscribe({
      next: () => {
        this.toastr.success('Incident mis a jour');
        this.closeModal();
        this.loadIncidents();
      },
      error: (err) => this.toastr.error(err.error?.error || 'Erreur lors de la mise a jour')
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
