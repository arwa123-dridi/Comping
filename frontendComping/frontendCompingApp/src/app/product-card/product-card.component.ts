import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartService } from '../services/cart.service';
import { Router } from '@angular/router';
import { ProductService } from '../services/Product.service';
import { RecommendationService } from '../services/Recommendation.service';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-card.component.html',
  styleUrls: ['./product-card.component.css']
})
export class ProductCardComponent {

  @Input() produit: any;
  defaultImage: string = 'assets/default-product.png';
  productDetails: any;
  loading = false;
recommendedProducts: any[] = [];

constructor(private cartService: CartService, private router: Router, private productService: ProductService, private recommendationService: RecommendationService) { }



  // 🧭 OPEN DETAILS PAGE
  openDetails() {
    this.router.navigate(['/product', this.produit.id]);
  }

  // ✅ Safe image
  get image(): string {
    if (!this.produit?.imageUrl) return this.defaultImage;

    return this.produit.imageUrl.startsWith('http')
      ? this.produit.imageUrl
      : `http://localhost:8087${this.produit.imageUrl}`;
  }

  // ✅ Status class
  getStatusClass(status: string) {
    return {
      'available': status === 'DISPONIBLE',
      'low-stock': status === 'STOCK_FAIBLE',
      'out-stock': status === 'RUPTURE_STOCK'
    };
  }

  // ✅ Short description
  getShortDescription(): string {
    if (!this.produit?.descriptionProduit) return '';

    return this.produit.descriptionProduit.length > 60
      ? this.produit.descriptionProduit.substring(0, 60) + '...'
      : this.produit.descriptionProduit;
  }

  // 🛒 ADD TO CART (FINAL VERSION)
  addToCart() {

  const token = localStorage.getItem('authToken');

  if (!token) {
    alert("Session expired. Please login again.");
    return;
  }

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const userId = payload.id;

    if (!userId) return;

    this.cartService.addToCart(userId, this.produit.id, 1)
      .subscribe({
        next: () => {

          alert("Produit ajouté au panier 🛒");

          // ⭐ CALL RECOMMENDATION AFTER ADD TO CART
          this.loadRecommendations(userId);

        },
        error: (err) => console.error(err)
      });

  } catch (e) {
    console.error(e);
  }
}

loadRecommendations(userId: string) {

  this.cartService.getCart(userId).subscribe({
    next: (cart: any) => {

      const productIds = cart?.lignes?.map((l: any) => l.produitId) || [];

      if (productIds.length === 0) return;

    this.recommendationService.getRecommendations(productIds)
  .subscribe(ids => {

    ids.forEach(id => {
      this.productService.getProductById(id)
        .subscribe(product => {
          this.recommendedProducts.push(product);
        });
    });

  });

    }
  });
}
  loadProductById() {

    if (!this.produit?.id) return;

    this.loading = true;

    this.productService.getProductById(this.produit.id)
      .subscribe({
        next: (data) => {
          this.productDetails = data;
          this.loading = false;
        },
        error: (err) => {
          console.error(err);
          this.loading = false;
        }
      });
  }

  // 🔥 PROMO ACTIVE ?
isPromotionActive(): boolean {
  if (!this.produit?.promoPrice || !this.produit?.promoStart || !this.produit?.promoEnd)
    return false;

  const now = new Date().getTime();
  const start = new Date(this.produit.promoStart).getTime();
  const end = new Date(this.produit.promoEnd).getTime();

  return now >= start && now <= end;
}

// 💰 FINAL PRICE (AUTO PROMO)
get finalPrice(): number {
  return this.isPromotionActive()
    ? this.produit.promoPrice
    : this.produit.prixProduit;
}

// 📉 DISCOUNT %
get discountPercent(): number {
  if (!this.isPromotionActive()) return 0;
  return Math.round(
    (1 - this.produit.promoPrice / this.produit.prixProduit) * 100
  );
}

}