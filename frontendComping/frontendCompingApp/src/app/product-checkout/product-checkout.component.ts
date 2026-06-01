import { Component, OnInit } from '@angular/core';
import { CommandeService, CommandeRequestDTO } from '../services/CommandeService';
import { CartService } from '../services/cart.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StatutCommande } from '../models/statut-commande';
import { ModePaiement } from '../models/mode-paiement';
import { ModeLivraison } from '../models/mode-livraison';
import { Header } from "../layouts/header/header";

@Component({
  selector: 'app-product-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, Header],
  templateUrl: './product-checkout.component.html',
  styleUrls: ['./product-checkout.component.css'],
})
export class ProductCheckoutComponent implements OnInit {
  // 📍 villes par région (simplifié mais réaliste)
  northCities = [
    'Tunis', 'Ariana', 'Ben Arous', 'Manouba',
    'Bizerte', 'Nabeul', 'Zaghouan', 'Beja', 'Jendouba'
  ];

  centerCities = [
    'Sousse', 'Monastir', 'Mahdia', 'Kairouan', 'Sfax'
  ];

  southWestCities = [
    'Gabes', 'Medenine', 'Tataouine', 'Gafsa',
    'Tozeur', 'Kebili', 'Kasserine', 'Sidi Bouzid'
  ];
  // ✅ expose enums to HTML
  ModePaiement = ModePaiement;
  ModeLivraison = ModeLivraison;

  panier: any;
  userId!: string;

  showErrorPopup = false;

  // form model
  commande: CommandeRequestDTO = {
    userId: '',
    adresseLivraison: {
      prenom: '',
      nom: '',
      telephone: '',
      adresse: '',
      ville: '',
      codePostal: ''
    },
    modePaiement: ModePaiement.CARTE,
    modeLivraison: ModeLivraison.HOME_DELIVERY
  };

  constructor(
    private commandeService: CommandeService,
    private cartService: CartService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.extractUserFromToken();
  }

  // 🔐 GET USER FROM TOKEN
  extractUserFromToken() {
    const token = localStorage.getItem('authToken');
    if (!token) return;

    const payload = JSON.parse(atob(token.split('.')[1]));
    this.userId = payload.id;

    this.commande.userId = this.userId;

    this.loadCart();
  }

  // 🛒 LOAD CART
  loadCart() {
    this.cartService.getCart(this.userId).subscribe(res => {
      this.panier = res;
    });
  }

  // 💳 CREATE ORDER
 payer() {

  if (!this.isFormValid()) {
    this.showErrorPopup = true;
    return;
  }

  const commandeToSend: CommandeRequestDTO = {
    ...this.commande
  };

  this.commandeService.createCommande(commandeToSend).subscribe({
    next: () => {
      alert('✅ Commande confirmée avec succès !');
      this.router.navigate(['/']);
    },
    error: err => {
      console.error(err);
      alert('❌ Une erreur est survenue.');
    }
  });
}

  // ❌ CANCEL ORDER
  annulerCommande() {

    const orderId = localStorage.getItem('orderId');

    if (!orderId) {
      if (confirm('Êtes-vous sûr de vouloir annuler la commande ?')) {
        this.router.navigate(['/marketplace']);
      }
      return;
    }

    if (!confirm('Êtes-vous sûr de vouloir annuler la commande ?')) return;

    this.commandeService.updateStatut(orderId, StatutCommande.ANNULEE)
      .subscribe({
        next: () => {

          // 🧹 reset form correctly
          this.commande = {
            userId: this.userId,
            adresseLivraison: {
              prenom: '',
              nom: '',
              telephone: '',
              adresse: '',
              ville: '',
              codePostal: ''
            },
            modePaiement: ModePaiement.CARTE,
            modeLivraison: ModeLivraison.HOME_DELIVERY
          };

          localStorage.removeItem('orderId');

          alert('❌ Commande annulée avec succès');

          this.router.navigate(['/marketplace']);
        },
        error: (err) => {
          console.error(err);
          alert('❌ Erreur lors de l’annulation');
        }
      });
  }

  // ✅ VALIDATION
  isFormValid(): boolean {
    const ad = this.commande.adresseLivraison;

    return !!(
      ad.prenom &&
      ad.nom &&
      ad.telephone &&
      ad.adresse &&
      ad.ville &&
      ad.codePostal &&
      this.commande.modePaiement &&
      this.commande.modeLivraison
    );
  }

  deliveryFee = 10; // valeur par défaut

  calculateDeliveryFee() {

  const ville = this.commande.adresseLivraison.ville;

  if (!ville) {
    this.deliveryFee = 10;
    return;
  }

  if (this.northCities.includes(ville)) {
    this.deliveryFee = 8;
  }
  else if (this.centerCities.includes(ville)) {
    this.deliveryFee = 10;
  }
  else {
    this.deliveryFee = 15;
  }
}

}