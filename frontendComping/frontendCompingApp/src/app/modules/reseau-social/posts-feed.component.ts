import { Component } from '@angular/core';

@Component({
  selector: 'app-posts-feed',
  templateUrl: './posts-feed.component.html',
  styleUrls: ['./posts-feed.component.css']
})
export class PostsFeedComponent {
  posts = [
    { id: 1, user: 'Lucas M.', content: 'Magnifique soirée au Camping des Pins! 🌟 #camping #nature', likes: 23, comments: 5, status: 'approuvé' },
    { id: 2, user: 'Sophie C.', content: 'Trop déçu par la propreté...', likes: 2, comments: 12, status: 'rejete' },
    { id: 3, user: 'Karim D.', content: 'Rando incroyable avec CAMPINO!', likes: 45, comments: 8, status: 'approuvé' }
  ];

  stats = {
    totalPosts: 156,
    activeUsers: 89,
    reports: 4
  };
}

