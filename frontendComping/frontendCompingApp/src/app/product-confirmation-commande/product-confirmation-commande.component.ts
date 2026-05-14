import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CartService } from '../services/cart.service';
import { CommandeService, CommandeRequestDTO } from '../services/CommandeService';
import { StatutCommande } from '../models/statut-commande';
import { ProductPanierComponent } from "../product-panier/product-panier.component";

@Component({
  selector: 'app-product-confirmation-commande',
  standalone: true,
  imports: [CommonModule, ProductPanierComponent],
  templateUrl: './product-confirmation-commande.component.html',
  styleUrls: ['./product-confirmation-commande.component.css'],
})
export class ProductConfirmationCommandeComponent implements OnInit {

  panier: any;
  userId!: string;
  showCartSidebar = false;
  loading = true;
  isSubmitting = false;

  orderId!: string; // 👈 store order id

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
    modePaiement: 'CARTE',
    modeLivraison: 'HOME_DELIVERY'
  };

  constructor(
    private cartService: CartService,
    private commandeService: CommandeService,
    private router: Router
  ) { }

ngOnInit(): void {
  this.extractUserFromToken();

  this.cartService.cart$.subscribe(cart => {
    if (cart) {
      this.panier = cart;
      this.loading = false;
    }
  });
}

  extractUserFromToken() {
    const token = localStorage.getItem('authToken');
    if (!token) return;

    const payload = JSON.parse(atob(token.split('.')[1]));
    this.userId = payload.id;

    this.commande.userId = this.userId;

    this.loadCart();
  }

  loadCart() {
    this.loading = true;

    this.cartService.getCart(this.userId).subscribe({
      next: (res) => {
        this.panier = res;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  private getUserIdFromToken(): string | null {
    const token = localStorage.getItem('authToken');
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.id;
    } catch {
      return null;
    }
  }

  toggleCartSidebar() {
    this.showCartSidebar = !this.showCartSidebar;

    if (this.showCartSidebar) {
      const userId = this.getUserIdFromToken();
      if (userId) { // only call if not null
        this.cartService.refreshCartCount(userId);
      } else {
        console.error('User not logged in or token invalid');
      }
    }
  }

  // 💰 TOTAL
  getTotal(): number {
    return (this.panier?.totalPrice || 0) + 10;
  }

  // 🔙 BACK CART
  backToCart() {
    this.showCartSidebar = true;
  }

  

  // ❌ CANCEL → UPDATE STATUS TO ANNULEE
  // ❌ CANCEL ORDER (FRONT ONLY)
  cancel() {

    const confirmed = confirm('❌ Voulez-vous vraiment annuler la commande ?');
    if (!confirmed) return;

    this.isSubmitting = false;

    this.cartService.clearCart(this.userId).subscribe({
      next: () => {
        console.log("🧹 Cart cleared after cancel");

        alert('Commande annulée');
        this.router.navigate(['/marketplace']);
      },
      error: (err) => {
        console.error("❌ Error clearing cart:", err);
      }
    });
  }

  // ✅ CONFIRM ORDER (FRONT ONLY)
  confirmOrder() {

    const confirmed = confirm("✅ Voulez-vous confirmer votre commande ?");
    if (!confirmed) return;

    this.isSubmitting = true;

    // simulate backend delay for better UX (optional)
    setTimeout(() => {

      this.isSubmitting = false;

      // ⭐ DO NOT clear cart
      // ⭐ DO NOT call API
      // ⭐ DO NOT update status

      alert("🎉 Commande confirmée avec succès !");
      this.router.navigate(['/command']);

    }, 600);
  }

  // 🖼 IMAGE
  getImage(imageUrl: string) {
    return imageUrl
      ? `http://localhost:8087${imageUrl}`
      : 'assets/default-product.png';
  }
}