import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UpperCasePipe } from '@angular/common';
import { LLMService, ChatMessage } from '../../services/llm.service';

interface DisplayMessage extends ChatMessage {
  isUserMessage: boolean;
  displayTime?: string;
}

@Component({
  selector: 'app-emergency-chatbot',
  templateUrl: './emergency-chatbot.component.html',
  styleUrls: ['./emergency-chatbot.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, UpperCasePipe]
})
export class EmergencyChatbotComponent implements OnInit, AfterViewChecked {

  @ViewChild('chatContainer', { static: false }) chatContainer!: ElementRef;
  @ViewChild('messageInput', { static: false }) messageInput!: ElementRef;

  messages: DisplayMessage[] = [];
  inputMessage: string = '';
  isLoading: boolean = false;
  currentContext: string = 'general';
  contexts = [
    { value: 'general', label: 'General Questions' },
    { value: 'incident', label: 'Incident Report' },
    { value: 'alert', label: 'Active Alert' },
    { value: 'emergency', label: 'Emergency' }
  ];

  isLLMAvailable: boolean = false;
  selectedFile: string = '';

  constructor(private llmService: LLMService) { }

  ngOnInit(): void {
    this.checkLLMHealth();
    this.addSystemMessage('Welcome to Emergency & Incident Support Chat. How can I help you today?');
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  checkLLMHealth(): void {
    this.llmService.checkHealth().subscribe(
      (health) => {
        this.isLLMAvailable = health.available;
        if (health.available) {
          this.addSystemMessage(`✓ AI Assistant connected (${health.status}). ${health.modelName} ready.`);
        } else {
          this.addSystemMessage(`✗ AI Assistant unavailable. Please ensure Ollama is running on port 11434.`);
        }
      },
      (error) => {
        this.isLLMAvailable = false;
        this.addSystemMessage('✗ Cannot connect to AI Assistant. Make sure Ollama is running locally.');
        console.error('Health check failed:', error);
      }
    );
  }

  sendMessage(): void {
    if (!this.inputMessage.trim()) {
      return;
    }

    const userMessage = this.inputMessage.trim();
    this.inputMessage = '';

    // Add user message to display
    this.messages.push({
      message: userMessage,
      response: userMessage,
      isUserMessage: true,
      success: true,
      displayTime: this.getCurrentTime()
    });

    this.isLoading = true;

    // Send to backend based on context
    this.llmService.chat(userMessage, this.currentContext).subscribe(
      (response: ChatMessage) => {
        this.isLoading = false;
        this.messages.push({
          ...response,
          isUserMessage: false,
          displayTime: this.getCurrentTime()
        });
      },
      (error) => {
        this.isLoading = false;
        this.messages.push({
          message: userMessage,
          response: '❌ Error: Unable to get response. Please try again.',
          isUserMessage: false,
          success: false,
          error: error.message,
          displayTime: this.getCurrentTime()
        });
        console.error('Chat error:', error);
      }
    );

    // Focus back to input
    setTimeout(() => {
      this.messageInput?.nativeElement?.focus();
    }, 100);
  }

  clearChat(): void {
    this.messages = [];
    this.addSystemMessage('Chat cleared. How can I help you?');
  }

  private addSystemMessage(content: string): void {
    this.messages.push({
      message: 'System',
      response: content,
      isUserMessage: false,
      success: true,
      displayTime: this.getCurrentTime()
    });
  }

  private scrollToBottom(): void {
    try {
      setTimeout(() => {
        this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
      }, 0);
    } catch (err) {
      console.error('Scroll error:', err);
    }
  }

  private getCurrentTime(): string {
    const now = new Date();
    return now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  }

  onContextChange(newContext: string): void {
    this.currentContext = newContext;
    const contextLabel = this.contexts.find(c => c.value === newContext)?.label || 'Context';
    this.addSystemMessage(`Context switched to: ${contextLabel}`);
  }

  getContextColor(): string {
    switch (this.currentContext) {
      case 'emergency':
        return '#ff4444';
      case 'alert':
        return '#ff9800';
      case 'incident':
        return '#f29027';
      default:
        return '#3da859';
    }
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }
}
