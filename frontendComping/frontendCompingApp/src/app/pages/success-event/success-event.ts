import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-success-event',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './success-event.html',
  styleUrl: './success-event.css',
})
export class SuccessEvent implements OnInit {
  sessionId: string | null = null;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.sessionId = this.route.snapshot.queryParamMap.get('session_id');
    console.log("Session ID:", this.sessionId);
  }
  
}