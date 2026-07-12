import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { DemandeTransportService } from '../../services/demande-transport.service';
import { AvisService } from '../../services/avis.service';
import { DemandeTransportResponse } from '../../models/demande-transport.model';
import { StarRatingComponent } from '../../shared/star-rating/star-rating.component';

@Component({
  selector: 'app-mes-transports',
  standalone: true,
  imports: [CommonModule, RouterModule, StarRatingComponent],
  templateUrl: './mes-transports.html',
  styleUrls: ['./mes-transports.css']
})
export class MesTransportsComponent implements OnInit {

  demandes: DemandeTransportResponse[] = [];
  loading = false;
  selected: DemandeTransportResponse | null = null;
  ratingInProgress = false;

  constructor(
    private demandeTransportService: DemandeTransportService,
    private avisService: AvisService,
    private toastr: ToastrService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.demandeTransportService.getMine().subscribe({
      next: (data) => {
        this.demandes = data;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Erreur lors du chargement de vos demandes');
        this.loading = false;
      }
    });
  }

  voirDetail(d: DemandeTransportResponse): void {
    this.selected = d;
  }

  closeModal(): void {
    this.selected = null;
  }

  peutModifier(d: DemandeTransportResponse): boolean {
    return d.statut === 'EN_ATTENTE';
  }

  peutNoter(d: DemandeTransportResponse): boolean {
    return d.statut === 'LIVREE' && !d.noteAttribuee;
  }

  modifier(d: DemandeTransportResponse): void {
    this.router.navigate(['/dashboard/logistique/transports/edit', d.idDemandeTransport]);
  }

  annuler(d: DemandeTransportResponse): void {
    if (!confirm('Annuler cette demande de transport ?')) return;
    this.demandeTransportService.delete(d.idDemandeTransport).subscribe({
      next: () => {
        this.toastr.success('Demande annulee');
        this.load();
      },
      error: () => this.toastr.error('Erreur lors de l\'annulation')
    });
  }

  noter(d: DemandeTransportResponse, note: number): void {
    this.ratingInProgress = true;
    this.avisService.creerAvis({
      note,
      commentaire: '',
      cibleId: d.idDemandeTransport,
      typeCible: 'TRANSPORT'
    }).subscribe({
      next: () => {
        this.toastr.success('Merci pour votre note !');
        this.ratingInProgress = false;
        this.load();
      },
      error: (err) => {
        this.toastr.error(err.error?.message || 'Erreur lors de la notation');
        this.ratingInProgress = false;
      }
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
