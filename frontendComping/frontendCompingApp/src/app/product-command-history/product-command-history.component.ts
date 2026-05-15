import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CommandeService } from '../services/CommandeService';
import { Commande } from '../models/commande.model';
import { StatutCommande } from '../models/statut-commande'; // ⭐ IMPORT ENUM

@Component({
  selector: 'app-product-command-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-command-history.component.html',
  styleUrl: './product-command-history.component.css',
})
export class ProductCommandHistoryComponent implements OnInit {

  commandes: Commande[] = [];
  loading = false;
  userId: string = '';
  selectedOrder: Commande | null = null;

  // ⭐⭐⭐ THIS LINE FIXES YOUR ERROR
  StatutCommande = StatutCommande;

  constructor(private commandeService: CommandeService) {}

  ngOnInit(): void {
    this.userId = this.getUserIdFromToken();

    if (this.userId) {
      this.loadHistory();
    } else {
      console.error('User not logged in or invalid token');
    }
  }

  // 🔐 JWT decode
  getUserIdFromToken(): string {
    const token =
      localStorage.getItem('authToken') ||
      localStorage.getItem('token');

    if (!token) return '';

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.id || payload.sub || '';
    } catch (error) {
      console.error('Invalid token format', error);
      return '';
    }
  }

  // 📦 Load orders
  loadHistory(): void {
    this.loading = true;

    this.commandeService.getCommandesByUser(this.userId)
      .subscribe({
        next: (data) => {
          this.commandes = data;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error loading commandes', err);
          this.loading = false;
        }
      });
  }

  // 🎯 Status badge colors
 getStatusClass(status?: string): string {
  if (!status) return 'pending';

  switch (status) {
    case 'LIVREE': return 'delivered';
    case 'ANNULEE': return 'cancelled';
    case 'EN_ATTENTE': return 'pending';
    case 'CONFIRMEE': return 'pending';
    case 'EN_PREPARATION': return 'pending';
    case 'EXPEDIEE': return 'pending';
    default: return 'pending';
  }
}



}