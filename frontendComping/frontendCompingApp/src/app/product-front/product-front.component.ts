import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ProductCardComponent } from '../product-card/product-card.component';

@Component({
  selector: 'app-product-front',
  standalone: true,
  imports: [
    CommonModule,
    HttpClientModule,
    FormsModule,
    ProductCardComponent
  ],
  templateUrl: './product-front.component.html',
  styleUrls: ['./product-front.component.css']
})
export class ProductFrontComponent implements OnInit {

  produits: any[] = [];
  filteredProduits: any[] = [];

  searchTerm: string = '';
  selectedCategory: string = '';
  selectedStatus: string = '';

  categories: string[] = [
    'TENTES',
    'SACS_DE_COUCHAGE',
    'MOBILIER_DE_CAMPING',
    'ECLAIRAGE',
    'CUISINE_DE_CAMPING',
    'AUTRE'
  ];

  private baseUrl = 'http://localhost:8087/api/produits';
  private defaultImage = 'assets/default-product.png';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  // ✅ LOAD ALL PRODUCTS
  loadProducts() {
    this.http.get<any[]>(`${this.baseUrl}/allProduct`).subscribe({
      next: (data) => {

        this.produits = data.map(p => ({
          ...p,
          imageUrl: p.imageUrl
            ? `http://localhost:8087${p.imageUrl}`
            : this.defaultImage,
          statut: p.statut?.toUpperCase()
        }));

        // ✅ IMPORTANT: show all products initially
        this.filteredProduits = this.produits;

        console.log("ALL PRODUCTS LOADED:", this.produits);
      },
      error: (err) => console.error(err)
    });
  }

  // 🔍 FILTER LOGIC
  applyFilters() {

    const search = this.searchTerm.toLowerCase();

    this.filteredProduits = this.produits.filter(p => {

      const matchSearch =
        !this.searchTerm ||
        p.nomProduit?.toLowerCase().includes(search);

      const matchCategory =
        !this.selectedCategory ||
        p.categorieProduit === this.selectedCategory;

      const matchStatus =
        !this.selectedStatus ||
        p.statut === this.selectedStatus;

      return matchSearch && matchCategory && matchStatus;
    });
  }

}