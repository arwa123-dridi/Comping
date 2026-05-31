import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CommandeService } from '../services/CommandeService';
import { Commande } from '../models/commande.model';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-product-livreur',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-livreur.html',
  styleUrls: ['./product-livreur.css']
})
export class ProductLivreur implements OnInit {

  commandes: Commande[] = [];
  livreurId: string = '';

  constructor(
    private commandeService: CommandeService,
    private toastr: ToastrService
  ) { }

  getLivreurIdFromToken(): string {
    const token = localStorage.getItem('authToken');
    if (!token) return '';

    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.id; // 👈 THIS is your livreur ID
  }

  ngOnInit(): void {
    this.livreurId = this.getLivreurIdFromToken();

    console.log("👉 Livreur ID from JWT:", this.livreurId);

    this.loadCommandes();
  }

  loadCommandes() {
    if (!this.livreurId) {
      this.toastr.warning('Livreur ID introuvable ❌');
      return;
    }

    this.commandeService.getCommandesByLivreur(this.livreurId)
      .subscribe({
        next: (data) => {
          this.commandes = data;
        },
        error: () => {
          this.toastr.error('Erreur chargement commandes ❌');
        }
      });
  }

  markAsLivree(id: string) {
    this.commandeService.markLivree(id, this.livreurId)
      .subscribe({
        next: () => {
          this.toastr.success('Commande livrée ✔');
          this.loadCommandes();
        },
        error: () => {
          this.toastr.error('Erreur update ❌');
        }
      });
  }
}