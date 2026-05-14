import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { EditeEventComponent } from "./client/edite-event/edite-event.component";


import { HttpClientModule } from '@angular/common/http'; 

@Component({
  selector: 'app-root',
  imports: [RouterOutlet ,HttpClientModule,EditeEventComponent],

  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontendCompingApp';
}
