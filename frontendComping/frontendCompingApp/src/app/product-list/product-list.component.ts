import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../layouts/sidebar/sidebar.component'; // ⭐ add this

interface Product {
  id: string;
  nomProduit: string;
  descriptionProduit: string;
  prixProduit: number;
  categorieProduit: number;
  typeProduit: string;
  statut: string;
}

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    CommonModule,
    SidebarComponent   // ⭐ NOW you can use <app-sidebar>
  ],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {

  products: Product[] = [
    { id: '1', nomProduit: 'Tente', descriptionProduit: 'Tente 2 places', prixProduit: 120, categorieProduit: 1, typeProduit: 'Location', statut: 'Disponible' },
    { id: '2', nomProduit: 'Sac de couchage', descriptionProduit: 'Confortable', prixProduit: 45, categorieProduit: 2, typeProduit: 'Vente', statut: 'En rupture' },
    { id: '3', nomProduit: 'Lampe', descriptionProduit: 'LED rechargeable', prixProduit: 30, categorieProduit: 3, typeProduit: 'Vente', statut: 'Disponible' }
  ];

  constructor() { }

  ngOnInit(): void {}

  deleteProduct(id: string) {
    this.products = this.products.filter(p => p.id !== id);
  }

  editProduct(id: string) {
    console.log('Edit product', id);
  }

  addProduct() {
    console.log('Add new product');
  }

}