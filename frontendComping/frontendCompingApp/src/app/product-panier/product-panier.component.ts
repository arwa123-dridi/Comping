import { ChangeDetectorRef, Component, EventEmitter, OnInit, Output } from '@angular/core';
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
  promoTimers: { [key: string]: string } = {};
  interval: any;
  constructor(private cartService: CartService, private router: Router, private cdr: ChangeDetectorRef) { }

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

        if (!res || !res.lignes) {
          this.panier = { lignes: [], totalPrice: 0 };
        } else {
          this.panier = res;
        }

        this.loading = false;
        setTimeout(() => {
          this.startCountdown();
        }, 0); this.startCountdown();  // ⭐ ADD THIS LINE

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

 startCountdown() {
  if (this.interval) clearInterval(this.interval);

  this.interval = setInterval(() => {

    if (!this.panier?.lignes) return;

    this.panier.lignes.forEach((item: any) => {

      if (!item.promoActive || !item.promoEnd) return;

      const end = new Date(item.promoEnd).getTime();
      const now = Date.now();
      const diff = end - now;

      if (diff <= 0) {
        this.promoTimers[item.produitId] = "Expiré";
        return;
      }

      const hours = Math.floor(diff / 3600000);
      const minutes = Math.floor((diff % 3600000) / 60000);
      const seconds = Math.floor((diff % 60000) / 1000);

      this.promoTimers[item.produitId] =
        `${hours.toString().padStart(2,'0')}:` +
        `${minutes.toString().padStart(2,'0')}:` +
        `${seconds.toString().padStart(2,'0')}`;

    });

    // ⭐ IMPORTANT: force UI refresh
    this.cdr.detectChanges();

  }, 1000);
}

  ngOnDestroy() {
    if (this.interval) {
      clearInterval(this.interval);
    }
  }
}