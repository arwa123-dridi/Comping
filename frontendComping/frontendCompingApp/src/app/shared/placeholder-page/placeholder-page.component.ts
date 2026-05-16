import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-placeholder-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="placeholder-wrap">
      <div class="card">
        <h2>{{ title }}</h2>
        <p>Cette page est en cours de construction.</p>
        <a routerLink="/Campino">← Retour à l'accueil</a>
      </div>
    </section>
  `,
  styles: [
    `
      .placeholder-wrap {
        min-height: calc(100vh - 80px);
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f1f5f9;
        padding: 24px;
      }

      .card {
        background: #ffffff;
        border: 1px solid #dbe5ef;
        border-radius: 16px;
        padding: 24px;
        width: min(580px, 100%);
      }

      h2 {
        margin: 0 0 8px;
        color: #1b2a4a;
      }

      p {
        margin: 0 0 16px;
        color: #475569;
      }

      a {
        color: #1f73a3;
        text-decoration: none;
        font-weight: 700;
      }
    `
  ]
})
export class PlaceholderPageComponent {
  title = 'Page';

  constructor(private route: ActivatedRoute) {
    this.route.data.subscribe((data) => {
      this.title = data['title'] ?? 'Page';
    });
  }
}
