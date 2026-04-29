import { Component } from '@angular/core';

@Component({
  selector: 'app-avis',
  templateUrl: './avis.component.html',
  styleUrls: ['./avis.component.css']
})
export class AvisComponent {
  avis = [
    { id: 1, user: 'Lucas M.', site: 'Forêt des Pins', rating: 5, comment: 'Super expérience en famille!', date: '12 Avr', status: 'approuvé' },
    { id: 2, user: 'Sophie C.', site: 'Plage du Sud', rating: 3, comment: 'Bonne localisation mais sanitaires à améliorer.', date: '14 Avr', status: 'en_attente' },
    { id: 3, user: 'Antoine R.', site: 'Camping des Lacs', rating: 4, comment: 'Personnel accueillant, emplacement calme.', date: '16 Avr', status: 'approuvé' }
  ];

  stats = {
    total: 127,
    avgRating: 4.3,
    positif: 89
  };
}

