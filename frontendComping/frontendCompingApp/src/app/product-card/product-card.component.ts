import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartService } from '../services/cart.service';

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

  constructor(private cartService: CartService) {}

  // ✅ Safe image
  get image(): string {
    if (!this.produit?.imageUrl) return this.defaultImage;

    return this.produit.imageUrl.startsWith('http')
      ? this.produit.imageUrl
      : `http://localhost:8087${this.produit.imageUrl}`;
  }

  // ✅ Status check
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

  // 🛒 ADD TO CART FUNCTION
  addToCart() {

  const userId = "USER_TEST_ID"; // later we replace with logged user

  this.cartService.addToCart(userId, this.produit.id, 1)
    .subscribe({
      next: () => {
        alert("Produit ajouté au panier 🛒");
        this.cartService.increaseCartCount(); // 🔥 update navbar counter
      },
      error: err => console.error(err)
    });
}

}