import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Component } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EditProductComponent } from '../edit-product/edit-product.component';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, HttpClientModule, RouterModule, EditProductComponent ],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent {

  product: any;

  // ✅ DELETE MODAL
  showDeleteModal = false;
  productIdToDelete: number | null = null;

  // ✅ EDIT MODAL (MISSING IN YOUR CODE)
  showEditModal = false;

  private baseUrl = 'http://localhost:8087/api/produits';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (!id) return;

    this.loadProduct(id);
  }

  // ✅ LOAD PRODUCT
  loadProduct(id: string) {
    this.http.get<any>(`${this.baseUrl}/${id}`).subscribe({
      next: (data) => {
        this.product = {
          ...data,
          imageUrl: data.imageUrl
            ? `http://localhost:8087${data.imageUrl}`
            : 'assets/default-product.png'
        };
      },
      error: (err) => console.error('Erreur chargement produit', err)
    });
  }

  // =========================
  // NAVIGATION
  // =========================
  goBack() {
    this.router.navigate(['/productTable']);
  }

  // =========================
  // EDIT (MODAL)
  // =========================
  openEdit() {
    this.showEditModal = true;
  }

  cancelEdit() {
    this.showEditModal = false;
  }

  onProductUpdated() {
    this.showEditModal = false;

    // reload product after update
    if (this.product?.id) {
      this.loadProduct(this.product.id);
    }
  }

  // =========================
  // DELETE
  // =========================
  confirmDelete(id: number) {
    this.productIdToDelete = id;
    this.showDeleteModal = true;
  }

  cancelDelete() {
    this.showDeleteModal = false;
    this.productIdToDelete = null;
  }

  deleteProduct() {
    if (!this.productIdToDelete) return;

    this.http.delete(`${this.baseUrl}/${this.productIdToDelete}`).subscribe({
      next: () => {
        this.showDeleteModal = false;
        this.router.navigate(['/productTable']);
      },
      error: (err) => console.error('Erreur suppression produit', err)
    });
  }

}