import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SigninService } from '../../services/signin.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  @Input() set collapsed(v: boolean) { 
    this.isCollapsed = v; 
  }
  
  isCollapsed = false;
  role = 'USER';

  constructor(private signinService: SigninService, private router: Router) {}

  ngOnInit(): void {
    this.role = localStorage.getItem('userRole') ?? 'USER';
  }

  toggleSidebar(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  isAdmin(): boolean {
    return ['ADMIN', 'ROLE_ADMIN'].includes(this.role);
  }

  isOrga(): boolean {
    return ['ORGANISATEUR', 'ROLE_ORGANISATEUR'].includes(this.role) || this.isAdmin();
  }

  isUser(): boolean {
    return !this.isOrga();
  }

  logout(): void {
    this.signinService.logout();
    this.router.navigate(['/login']);
  }

  testClick(): void {
    console.log('Sidebar link clicked');
  }
}