import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DemandeTransportService } from '../../services/demande-transport.service';
import { CreneauLivraisonService } from '../../services/creneau-livraison.service';
import { DemandeTransportResponse } from '../../models/demande-transport.model';
import { CreneauLivraison } from '../../models/creneau-livraison.model';

interface LivraisonRow {
  demande: DemandeTransportResponse;
  creneau?: CreneauLivraison;
}

@Component({
  selector: 'app-mes-livraisons',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './mes-livraisons.html',
  styleUrls: ['./mes-livraisons.css']
})
export class MesLivraisonsComponent implements OnInit {

  livraisons: LivraisonRow[] = [];
  loading = false;

  constructor(
    private demandeTransportService: DemandeTransportService,
    private creneauService: CreneauLivraisonService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.demandeTransportService.getMine().subscribe({
      next: (demandes) => {
        const avecCreneau = demandes.filter(d => !!d.creneauLivraisonId);
        if (avecCreneau.length === 0) {
          this.livraisons = [];
          this.loading = false;
          return;
        }
        this.creneauService.getAll().subscribe({
          next: (creneaux) => {
            this.livraisons = avecCreneau.map(d => ({
              demande: d,
              creneau: creneaux.find(c => c.idCreneauLivraison === d.creneauLivraisonId)
            }));
            this.loading = false;
          },
          error: () => {
            this.livraisons = avecCreneau.map(d => ({ demande: d }));
            this.loading = false;
          }
        });
      },
      error: () => this.loading = false
    });
  }
}
