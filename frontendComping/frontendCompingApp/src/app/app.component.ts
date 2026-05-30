import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { EditeEventComponent } from "./client/edite-event/edite-event.component";


import { HttpClientModule } from '@angular/common/http';
import { Header } from "./layouts/header/header"; 

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HttpClientModule, EditeEventComponent, Header],

  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontendCompingApp';
}
