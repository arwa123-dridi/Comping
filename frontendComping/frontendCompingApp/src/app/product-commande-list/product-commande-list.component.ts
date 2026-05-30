import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../layouts/sidebar/sidebar.component';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';

import { Commande } from '../models/commande.model';
import { CommandeService } from '../services/CommandeService';
import { FormsModule } from '@angular/forms';
import { User } from '../models/user.model';
import { AuthUserService } from '../services/signup.service';


@Component({
  selector: 'app-product-commande-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, FormsModule],
  templateUrl: './product-commande-list.component.html',
  styleUrls: ['./product-commande-list.component.css']
})
export class ProductCommandeListComponent implements OnInit {

  commandes: Commande[] = [];
  loading = false;
  selectedCommande: Commande | null = null;
  showModal = false;
  searchText: string = '';
  selectedStatus: string = '';
  selectedDate: string = '';

  filteredCommandes: Commande[] = [];

  livreurs: User[] = [];
  selectedCommandeForAssign: Commande | null = null;
  selectedLivreurId: string = '';
  modalType: 'DETAIL' | 'ASSIGN' | null = null;
  constructor(
    private commandeService: CommandeService,
    private toastr: ToastrService,
    private router: Router,
    private userService: AuthUserService
  ) { }

  ngOnInit(): void {
    this.loadCommandes();
    this.loadLivreurs();
  }

  loadLivreurs() {
    this.userService.getLivreurs().subscribe({
      next: (data) => this.livreurs = data,
      error: () => this.toastr.error("Erreur chargement livreurs ❌")
    });
  }

  // 📥 LOAD ORDERS
  loadCommandes() {
    this.loading = true;

    this.commandeService.getAllCommandes().subscribe({
      next: (data) => {

        console.log("COMMANDES => ", data);

        this.commandes = data;
        this.filteredCommandes = data;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Erreur chargement commandes ❌');
        this.loading = false;
      }
    });
  }

  applyFilters() {
    this.filteredCommandes = this.commandes.filter(c => {

      const matchStatus =
        this.selectedStatus ? c.statutCommande === this.selectedStatus : true;

      const matchSearch =
        this.searchText
          ? c.userId.toLowerCase().includes(this.searchText.toLowerCase())
          : true;

      const matchDate =
        this.selectedDate
          ? new Date(c.dateCommande).toDateString() === new Date(this.selectedDate).toDateString()
          : true;

      return matchStatus && matchSearch && matchDate;
    });
  }

  // 👁 VIEW DETAILS
  viewCommande(id: string) {
    this.commandeService.getCommandeById(id).subscribe({
      next: (cmd) => {
        this.selectedCommande = cmd;
        this.modalType = 'DETAIL';
      },
      error: () => this.toastr.error('Erreur chargement commande ❌')
    });
  }

  // 🔄 CHANGE STATUS FROM TABLE
  changeStatut(id: string, event: Event) {
    const statut = (event.target as HTMLSelectElement).value;

    this.commandeService.updateStatut(id, statut).subscribe({
      next: () => {
        this.toastr.success('Statut mis à jour ✔');
        this.loadCommandes();
      },
      error: () => this.toastr.error('Erreur mise à jour ❌')
    });
  }

  // ❌ CLOSE POPUP
  closeModal() {
    this.selectedCommande = null;
    this.modalType = null;
  }

  // 🚚 OPEN ASSIGN LIVREUR POPUP
  openAssignLivreur(commande: Commande) {
    this.selectedCommandeForAssign = commande;
    this.selectedLivreurId = '';
    this.modalType = 'ASSIGN';
  }
  // ❌ CLOSE ASSIGN POPUP
  closeAssignPopup() {
    this.selectedCommandeForAssign = null;
    this.selectedLivreurId = '';
    this.modalType = null;
  }

  assignLivreur() {
    if (!this.selectedCommandeForAssign || !this.selectedLivreurId) {
      this.toastr.warning("Choisissez un livreur !");
      return;
    }

    this.commandeService
      .assignLivreur(this.selectedCommandeForAssign.id, this.selectedLivreurId)
      .subscribe({
        next: () => {
          this.toastr.success("Livreur assigné 🚚");
          this.closeAssignPopup();
          this.loadCommandes();
        },
        error: () => this.toastr.error("Erreur assignation ❌")
      });
  }

}