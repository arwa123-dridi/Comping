import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../layouts/sidebar/sidebar.component';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';

import { Commande } from '../models/commande.model';
import { CommandeService } from '../services/CommandeService';
import { FormsModule } from '@angular/forms';

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
  constructor(
    private commandeService: CommandeService,
    private toastr: ToastrService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadCommandes();
  }

  // 📥 LOAD ORDERS
  loadCommandes() {
    this.loading = true;

    this.commandeService.getAllCommandes().subscribe({
      next: (data) => {
        this.commandes = data;
        this.filteredCommandes = data; // 👈 important
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
        this.selectedCommande = cmd; // ⭐ opens popup
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
  }

}