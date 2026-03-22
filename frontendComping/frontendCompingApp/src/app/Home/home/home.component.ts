import {Component, OnInit, OnDestroy, ViewEncapsulation} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
styleUrls: ['./home.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class HomeComponent implements OnInit, OnDestroy {

  currentSlide = 0;
  private timer: any;

  slides = [
    { url: 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=1200&q=80', label: '🏕️ Forêt · Ain Draham' },
    { url: 'https://images.unsplash.com/photo-1487730116645-74489c95b41b?w=1200&q=80', label: '⛺ Camping en montagne' },
    { url: 'https://images.unsplash.com/photo-1523987355523-c7b5b0dd90a7?w=1200&q=80', label: '🌲 Nature sauvage' },
    { url: 'https://images.unsplash.com/photo-1510312305653-8ed496efae75?w=1200&q=80', label: '🔥 Soirée autour du feu' },
    { url: 'https://images.unsplash.com/photo-1533873984035-25970ab07461?w=1200&q=80', label: '🌄 Lever de soleil' },
    { url: 'https://images.unsplash.com/photo-1478131143081-80f7f84ca84d?w=1200&q=80', label: '🌌 Nuit étoilée' },
  ];

  services = [
    { icon: '🏕️', title: 'Emplacements', desc: 'Trouvez l\'emplacement idéal parmi nos 240+ sites sélectionnés' },
    { icon: '🎒', title: 'Location matériel', desc: 'Tentes, sacs de couchage et équipements livrés sur place' },
    { icon: '🌄', title: 'Randonnées guidées', desc: 'Explorez la nature avec nos guides expérimentés certifiés' },
    { icon: '🔥', title: 'Expériences survie', desc: 'Apprenez les techniques de survie et de vie en plein air' },
  ];

  destinations = [
    { name: 'Ain Draham', sub: 'Forêt de chênes · Nord', tag: '32 emplacements', gradient: 'linear-gradient(160deg,#072010,#1a4d20)' },
    { name: 'Cap Serrat', sub: 'Bord de mer · Bizerte', tag: '18 emplacements', gradient: 'linear-gradient(135deg,#071a3d,#1f73a3)' },
    { name: 'Jebel Zaghouan', sub: 'Montagne · Centre', tag: '24 emplacements', gradient: 'linear-gradient(135deg,#3d1a00,#f29027)' },
    { name: 'Chott el-Jérid', sub: 'Désert · Sud · Expérience unique', tag: '12 emplacements', gradient: 'linear-gradient(135deg,#0d1f3d,#1b2a4a)' },
  ];

  ngOnInit(): void {
    this.startSlideshow();
  }

  ngOnDestroy(): void {
    clearInterval(this.timer);
  }

  startSlideshow(): void {
    this.timer = setInterval(() => this.nextSlide(), 5000);
  }

  goToSlide(index: number): void {
    this.currentSlide = index;
    clearInterval(this.timer);
    this.startSlideshow();
  }

  nextSlide(): void {
    this.currentSlide = (this.currentSlide + 1) % this.slides.length;
  }

  get currentLabel(): string {
    return this.slides[this.currentSlide].label;
  }
}
