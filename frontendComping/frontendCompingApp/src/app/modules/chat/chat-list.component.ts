import { Component } from '@angular/core';

@Component({
  selector: 'app-chat-list',
  templateUrl: './chat-list.component.html',
  styleUrls: ['./chat-list.component.css']
})
export class ChatListComponent {
  conversations = [
    { id: 1, user: 'Lucas Martin', avatar: 'LM', lastMsg: 'Quand commence la réservation ?', unread: 3, time: '14:32', status: 'en_ligne' },
    { id: 2, user: 'Sophie Carré', avatar: 'SC', lastMsg: 'Problème avec le paiement', unread: 0, time: '10:15', status: 'absent' },
{ id: 3, user: 'Karim Dali', avatar: 'KD', lastMsg: 'Merci pour l accueil !', unread: 1, time: '9:47', status: 'en_ligne' }
  ];

  stats = {
    total: 47,
    active: 12,
    urgent: 2
  };
}

