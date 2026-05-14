import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ProductCardComponent } from '../product-card/product-card.component';
import { ProductPanierComponent } from '../product-panier/product-panier.component';
import { CartService } from '../services/cart.service';

@Component({
  selector: 'app-product-front',
  standalone: true,
  imports: [
    CommonModule,
    HttpClientModule,
    FormsModule,
    ProductCardComponent,
    ProductPanierComponent
  ],
  templateUrl: './product-front.component.html',
  styleUrls: ['./product-front.component.css']
})
export class ProductFrontComponent implements OnInit {

  produits: any[] = [];
  filteredProduits: any[] = [];
  searchTerm = '';
  selectedCategory = '';
  selectedStatus = '';
  cartCount = 0;

  // 🔹 control sidebar visibility
  showCartSidebar = false;

  categories: string[] = [
    'TENTES', 'SACS_DE_COUCHAGE', 'MOBILIER_DE_CAMPING',
    'ECLAIRAGE', 'CUISINE_DE_CAMPING', 'AUTRE'
  ];

  private baseUrl = 'http://localhost:8087/api/produits';
  private defaultImage = 'assets/default-product.png';

  constructor(private http: HttpClient, private cartService: CartService) { }

  ngOnInit(): void {
    this.loadProducts();

    // 🔹 subscribe to cart count
    this.cartService.cartCount$.subscribe(count => {
      this.cartCount = count;
    });
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

  loadProducts() {
    this.http.get<any[]>(`${this.baseUrl}/allProduct`).subscribe({
      next: data => {
        this.produits = data.map(p => ({
          ...p,
          imageUrl: p.imageUrl ? `http://localhost:8087${p.imageUrl}` : this.defaultImage,
          statut: p.statut?.toUpperCase()
        }));
        this.filteredProduits = this.produits;
      },
      error: err => console.error(err)
    });
  }

  applyFilters() {
    const search = this.searchTerm.toLowerCase();
    this.filteredProduits = this.produits.filter(p => {
      const matchSearch = !this.searchTerm || p.nomProduit?.toLowerCase().includes(search);
      const matchCategory = !this.selectedCategory || p.categorieProduit === this.selectedCategory;
      const matchStatus = !this.selectedStatus || p.statut === this.selectedStatus;
      return matchSearch && matchCategory && matchStatus;
    });
  }
}