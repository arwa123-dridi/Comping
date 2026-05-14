import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartService } from '../services/cart.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-product-panier',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-panier.component.html',
  styleUrls: ['./product-panier.component.css'],
})
export class ProductPanierComponent implements OnInit {

    @Output() close = new EventEmitter<void>();
    
  panier: any;
  userId!: string;
  loading = true;

  constructor(private cartService: CartService , private router: Router) {}

  ngOnInit(): void {
    this.extractUserFromToken();
    this.loadCart();
  }

  // 🔐 Extract userId from JWT safely
  extractUserFromToken() {
    const token = localStorage.getItem('authToken');
    if (!token) return;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.userId = payload.id;
    } catch (err) {
      console.error('Invalid token', err);
    }
  }

  // 🛒 Load cart from backend
  loadCart() {
  if (!this.userId) return;

  this.loading = true;

  this.cartService.getCart(this.userId).subscribe({
    next: (res: any) => {

      // 🧠 SAFETY: ensure panier always has correct structure
      if (!res || !res.lignes) {
        this.panier = {
          lignes: [],
          totalPrice: 0
        };
      } else {
        this.panier = res;
      }

      this.loading = false;

      // 🔥 optional: force badge update
      this.cartService.forceRefreshCart(this.userId);
    },

    error: (err) => {
      console.error("LOAD CART ERROR:", err);

      // 🧠 fallback empty state
      this.panier = {
        lignes: [],
        totalPrice: 0
      };

      this.loading = false;

      // 🔥 reset badge on error too
      this.cartService.forceRefreshCart(this.userId);
    }
  });
}

  // ➕ Increase quantity
  increase(item: any) {
    this.cartService
      .updateQuantity(this.userId, item.produitId, item.quantite + 1)
      .subscribe(() => this.loadCart());
  }

  // ➖ Decrease quantity
  decrease(item: any) {
    if (item.quantite <= 1) return;

    this.cartService
      .updateQuantity(this.userId, item.produitId, item.quantite - 1)
      .subscribe(() => this.loadCart());
  }

  // 🗑 Remove product
  removeItem(produitId: string) {
    this.cartService.removeProduct(this.userId, produitId).subscribe(() => this.loadCart());
  }

  // ✅ Safe image
  getImage(imageUrl: string) {
    return imageUrl ? `http://localhost:8087${imageUrl}` : 'assets/default-product.png';
  }
  
  // 🛑 Close sidebar
closeCart() {
  // emit event to parent to hide the sidebar
  this.close.emit();
}
goToConfirmation() {
  this.close.emit(); // close sidebar
  this.router.navigate(['/confirm-order']);
}
}