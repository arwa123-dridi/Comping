import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { SidebarComponent } from '../layouts/sidebar/sidebar.component';
import { AddProductComponent } from '../add-product/add-product.component';
import { EditProductComponent } from '../edit-product/edit-product.component';
import { ToastrService } from 'ngx-toastr';

interface Product {
  id: string;
  nomProduit: string;
  descriptionProduit: string;
  prixProduit: number;
  categorieProduit: string;
  typeProduit: string;
  statut: string;
  imageUrl?: string;
}

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    SidebarComponent,
    AddProductComponent,
    EditProductComponent
  ],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {
selectedCategory: string = '';
selectedStatus: string = '';

categories: string[] = [
  'TENTES',
  'SACS_DE_COUCHAGE',
  'MATELAS_ET_TAPIS_DE_SOL',
  'CUISINE_DE_CAMPING',
  'GLACIERES',
  'STOCKAGE_EAU',
  'ECLAIRAGE',
  'ENERGIE_PORTABLE',
  'SACS_A_DOS',
  'EQUIPEMENT_DE_RANDONNEE',
  'SURVIE_ET_SECOURS',
  'MOBILIER_DE_CAMPING',
  'ABRIS_ET_TARP',
  'VETEMENTS_DE_CAMPING',
  'CHAUSSURES_DE_RANDONNEE',
  'AUTRE'
];
  products: Product[] = [];
  filteredProducts: Product[] = [];
  searchTerm: string = '';

  showAddModal = false;
  showEditModal = false;

  // ✅ DELETE CONFIRMATION
  showDeleteModal = false;
  selectedDeleteId: string | null = null;

  selectedProductId: string | null = null;

  private baseUrl = 'http://localhost:8087/api/produits';
  private defaultImage = 'assets/default-product.png';

  constructor(
    private http: HttpClient,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  /** Load products */
  loadProducts() {
    this.http.get<any[]>(`${this.baseUrl}/allProduct`).subscribe({
      next: (data) => {
        this.products = data.map(p => ({
          id: p.id ?? p._id ?? '',
          nomProduit: p.nomProduit,
          descriptionProduit: p.descriptionProduit,
          prixProduit: p.prixProduit,
          categorieProduit: p.categorieProduit,
          typeProduit: p.typeProduit,
          statut: p.statut,
          imageUrl: p.imageUrl
            ? `http://localhost:8087${p.imageUrl}`
            : this.defaultImage
        }));

        this.filteredProducts = [...this.products];
      },
      error: () => this.toastr.error('Erreur chargement produits ❌')
    });
  }

  /** Search */
  onSearchChange() {
    if (!this.searchTerm.trim()) {
      this.loadProducts();
      return;
    }

    this.http.get<Product[]>(`${this.baseUrl}/search?nom=${this.searchTerm}`).subscribe({
      next: (data) => {
        this.filteredProducts = data.map(p => ({
          ...p,
          imageUrl: p.imageUrl
            ? `http://localhost:8087${p.imageUrl}`
            : this.defaultImage
        }));
      },
      error: () => this.toastr.error('Erreur recherche ❌')
    });
  }

  // =========================
  // ✅ DELETE FLOW WITH POPUP
  // =========================

  /** Open confirmation modal */
  confirmDelete(id: string) {
    this.selectedDeleteId = id;
    this.showDeleteModal = true;
  }

  /** Cancel delete */
  cancelDelete() {
    this.showDeleteModal = false;
    this.selectedDeleteId = null;
  }

  /** Confirm delete */
  deleteConfirmed() {
    if (!this.selectedDeleteId) return;

    this.http.delete(`${this.baseUrl}/deleteProduct/${this.selectedDeleteId}`, { responseType: 'text' })
      .subscribe({
        next: () => {
          this.toastr.success('Produit supprimé avec succès 🗑️');
          this.loadProducts();
          this.cancelDelete();
        },
        error: () => {
          this.toastr.error('Erreur lors de la suppression ❌');
        }
      });
  }

  /** Show Add modal */
  addProduct() {
    this.showAddModal = true;
  }

  /** Show Edit modal */
  editProduct(id: string) {
    this.selectedProductId = id;
    this.showEditModal = true;
  }

  applyFilters() {
  this.filteredProducts = this.products.filter(p => {

    const matchSearch =
      !this.searchTerm ||
      p.nomProduit.toLowerCase().includes(this.searchTerm.toLowerCase());

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