import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-avis-detail',
  templateUrl: './avis-detail.component.html',
  styleUrls: ['./avis-detail.component.css']
})
export class AvisDetailComponent implements OnInit {
  avis: any;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    // Mock data
    this.avis = {
      id: 1,
      user: 'Lucas Martin',
      avatar: 'LM',
      site: 'Forêt des Pins',
      rating: 5,
      comment: 'Super expérience en famille! Emplacements spacieux et calmes. Le personnel a été très accueillant et compétent.',
      date: '12 Avril 2024',
      response: '',
      status: 'approuvé'
    };
  }

  onApprove() { console.log('Approved'); }
  onReject() { console.log('Rejected'); }
  onRespond() { console.log('Responded:', this.avis.response); }
}

