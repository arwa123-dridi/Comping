import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { CartService } from '../services/cart.service';
import { ProductPanierComponent } from "../product-panier/product-panier.component";
import { Header } from "../layouts/header/header";

@Component({
  selector: 'app-product-detail-page',
  standalone: true,
  imports: [CommonModule, ProductPanierComponent, Header],
  templateUrl: './product-detail-page.component.html',
  styleUrls: ['./product-detail-page.component.css'],
})
export class ProductDetailPageComponent implements OnInit {
  cartCount = 0;
  showCartSidebar = false;
  product: any;
  loading = true;
  defaultImage = 'assets/default-product.png';
  quantity: number = 1;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private cartService: CartService, private router: Router
  ) { }

  ngOnInit(): void {
    this.loadProduct();

    // 🔥 listen to real-time updates
    this.cartService.cartCount$.subscribe(count => {
      this.cartCount = count;
    });

    const userId = this.getUserIdFromToken();
    if (userId) {
      this.cartService.refreshCartCount(userId);
    }
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

  // 🔹 toggle cart sidebar
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

  // 🔥 LOAD PRODUCT FROM BACKEND
  loadProduct() {
    const id = this.route.snapshot.paramMap.get('id');

    if (!id) return;

    this.http.get(`http://localhost:8087/api/produits/${id}`)
      .subscribe({
        next: (res: any) => {
          this.product = res;
          this.loading = false;
        },
        error: (err) => {
          console.error(err);
          this.loading = false;
        }
      });
  }

  // 🖼 SAFE IMAGE
  get image(): string {
    if (!this.product?.imageUrl) return this.defaultImage;

    return this.product.imageUrl.startsWith('http')
      ? this.product.imageUrl
      : `http://localhost:8087${this.product.imageUrl}`;
  }

  // 🛒 ADD TO CART
  addToCart() {
    const token = localStorage.getItem('authToken');
    if (!token) return;

    const payload = JSON.parse(atob(token.split('.')[1]));
    const userId = payload.id;

    this.cartService.addToCart(userId, this.product.id, this.quantity)
      .subscribe({
        next: () => {
          alert('Produit ajouté 🛒');

          // 🔥 refresh badge instantly
          this.cartService.refreshCartCount(userId);

          // open sidebar
          this.showCartSidebar = true;
        }
      });
  }
  goToMarketplace() {
    this.router.navigate(['/marketplace']);
  }

  increaseQty() {
    this.quantity++;
  }

  decreaseQty() {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  isPromotionActive(): boolean {
    if (!this.product?.promoPrice || !this.product?.promoStart || !this.product?.promoEnd)
      return false;

    const now = Date.now();
    const start = new Date(this.product.promoStart).getTime();
    const end = new Date(this.product.promoEnd).getTime();

    return now >= start && now <= end;
  }

  // discount %
  get discountPercent(): number {
    if (!this.isPromotionActive()) return 0;

    return Math.round(
      (1 - this.product.promoPrice / this.product.prixProduit) * 100
    );
  }
  
}