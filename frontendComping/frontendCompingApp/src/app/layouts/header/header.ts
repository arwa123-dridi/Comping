import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { ProductPanierComponent } from '../../product-panier/product-panier.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, ProductPanierComponent],
  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class Header {

  private user: any = null;

  cartCount = 0;
  showCartSidebar = false;

  constructor(private cartService: CartService) {

    // LIVE CART COUNT
    this.cartService.cartCount$.subscribe(count => {
      this.cartCount = count;
    });

    // INITIAL LOAD
    const userId = this.getUserIdFromToken();
    if (userId) {
      this.cartService.refreshCartCount(userId);
    }
  }

  // =========================
  // OPEN CART
  // =========================
  toggleCart() {
    this.showCartSidebar = !this.showCartSidebar;

    const userId = this.getUserIdFromToken();
    if (this.showCartSidebar && userId) {
      this.cartService.refreshCartCount(userId);
    }
  }

  // IMPORTANT: this is called from child
  closeCart() {
    this.showCartSidebar = false;
  }

  // =========================
  // AUTH
  // =========================
  isConnected(): boolean {
    return this.user !== null;
  }

  isAdminOrOrg(): boolean {
    return this.user?.role === 'ADMIN' || this.user?.role === 'ORGANISATEUR';
  }

  // =========================
  // TOKEN
  // =========================
  private getUserIdFromToken(): string | null {
    const token = localStorage.getItem('authToken');
    if (!token) return null;

    try {
      return JSON.parse(atob(token.split('.')[1])).id;
    } catch {
      return null;
    }
  }
}