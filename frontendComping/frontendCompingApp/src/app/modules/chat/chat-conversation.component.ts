import { Component } from '@angular/core';

@Component({
  selector: 'app-chat-conversation',
  templateUrl: './chat-conversation.component.html',
  styleUrls: ['./chat-conversation.component.css']
})
export class ChatConversationComponent {
  messages = [
    { type: 'user', text: 'Bonjour, quand commence ma réservation?', time: '14:25' },
    { type: 'admin', text: 'Bonjour Lucas! Votre réservation commence demain à 15h.', time: '14:27' },
    { type: 'user', text: 'Parfait merci beaucoup!', time: '14:30' }
  ];
  newMessage = '';

  sendMessage() {
    if (this.newMessage.trim()) {
      this.messages.push({
        type: 'admin',
        text: this.newMessage,
        time: new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})
      });
      this.newMessage = '';
    }
  }
}

