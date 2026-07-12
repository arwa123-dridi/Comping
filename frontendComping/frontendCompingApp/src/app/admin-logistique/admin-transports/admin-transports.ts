import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { DemandeTransportService } from '../../services/demande-transport.service';
import { CreneauLivraisonService } from '../../services/creneau-livraison.service';
import { DemandeTransportResponse, StatutDemande } from '../../models/demande-transport.model';
import { CreneauLivraison } from '../../models/creneau-livraison.model';

@Component({
  selector: 'app-admin-transports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-transports.html',
  styleUrls: ['./admin-transports.css']
})
export class AdminTransportsComponent implements OnInit {

  demandes: DemandeTransportResponse[] = [];
  filtered: DemandeTransportResponse[] = [];
  creneaux: CreneauLivraison[] = [];
  loading = false;

  searchText = '';
  selectedStatus = '';

  modalType: 'DETAIL' | 'TRAITEMENT' | null = null;
  selected: DemandeTransportResponse | null = null;

  traitementStatut: StatutDemande | '' = '';
  traitementCommentaire = '';
  traitementCreneauId = '';
  suggestion: { creneauLivraisonId: string; heureDebut: string; heureFin: string; raison: string } | null = null;

  readonly transitions: Record<StatutDemande, StatutDemande[]> = {
    EN_ATTENTE: ['PLANIFIEE', 'EN_COURS', 'ANNULEE'],
    PLANIFIEE: ['EN_COURS', 'ANNULEE'],
    EN_COURS: ['LIVREE', 'ANNULEE'],
    LIVREE: [],
    ANNULEE: []
  };

  constructor(
    private demandeTransportService: DemandeTransportService,
    private creneauService: CreneauLivraisonService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadDemandes();
    this.loadCreneaux();
  }

  loadDemandes(): void {
    this.loading = true;
    this.demandeTransportService.getAll().subscribe({
      next: (data) => {
        this.demandes = data;
        this.filtered = data;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Erreur chargement demandes');
        this.loading = false;
      }
    });
  }

  loadCreneaux(): void {
    this.creneauService.getAll().subscribe({
      next: (data) => this.creneaux = data,
      error: () => {}
    });
  }

  applyFilters(): void {
    this.filtered = this.demandes.filter(d => {
      const matchStatus = this.selectedStatus ? d.statut === this.selectedStatus : true;
      const matchSearch = this.searchText
        ? (d.typeService + ' ' + d.adresseDepart + ' ' + d.adresseArrivee).toLowerCase().includes(this.searchText.toLowerCase())
        : true;
      return matchStatus && matchSearch;
    });
  }

  voirDetail(d: DemandeTransportResponse): void {
    this.selected = d;
    this.modalType = 'DETAIL';
  }

  ouvrirTraitement(d: DemandeTransportResponse): void {
    this.selected = d;
    this.traitementStatut = '';
    this.traitementCommentaire = d.commentaireOrganisateur || '';
    this.traitementCreneauId = d.creneauLivraisonId || '';
    this.suggestion = null;
    this.modalType = 'TRAITEMENT';
  }

  closeModal(): void {
    this.selected = null;
    this.modalType = null;
  }

  statutsDisponibles(): StatutDemande[] {
    if (!this.selected) return [];
    return this.transitions[this.selected.statut] || [];
  }

  estTerminal(statut: StatutDemande): boolean {
    return (this.transitions[statut] || []).length === 0;
  }

  suggererCreneau(): void {
    if (!this.selected) return;
    this.demandeTransportService.suggestionCreneau(this.selected.idDemandeTransport).subscribe({
      next: (s) => {
        this.suggestion = s;
        this.traitementCreneauId = s.creneauLivraisonId;
      },
      error: (err) => this.toastr.error(err.error?.error || 'Aucune suggestion disponible')
    });
  }

  confirmerTraitement(): void {
    if (!this.selected || !this.traitementStatut) {
      this.toastr.warning('Choisissez un statut');
      return;
    }
    this.demandeTransportService.traiter(this.selected.idDemandeTransport, {
      statut: this.traitementStatut,
      commentaireOrganisateur: this.traitementCommentaire,
      creneauLivraisonId: this.traitementCreneauId || undefined
    }).subscribe({
      next: () => {
        this.toastr.success('Demande mise a jour');
        this.closeModal();
        this.loadDemandes();
        this.loadCreneaux();
      },
      error: (err) => this.toastr.error(err.error?.error || 'Erreur lors de la mise a jour')
    });
  }

  statutClass(statut: string): string {
    return {
      'EN_ATTENTE': 'st-pending',
      'PLANIFIEE': 'st-planned',
      'EN_COURS': 'st-progress',
      'LIVREE': 'st-delivered',
      'ANNULEE': 'st-cancelled'
    }[statut] || 'st-pending';
  }
}
