import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Component } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, HttpClientModule,RouterModule],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent {

  product: any;
  private baseUrl = 'http://localhost:8087/api/produits';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router 
  ) {}

ngOnInit(): void {
  const id = this.route.snapshot.paramMap.get('id');

  if (!id) return;

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
goBack() {
  this.router.navigate(['/productTable']);
}
}