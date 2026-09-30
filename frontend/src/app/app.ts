import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth-service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  loading = true;
  token: string | null = null;

  constructor(private authService:AuthService) {}

  ngOnInit() {
    if (typeof window !== 'undefined' && window.localStorage){
      this.token = localStorage.getItem('authToken');

      if (this.token) 
        this.authService.inicializarUsuario().subscribe();

      setTimeout(() => {
        this.loading = false;
      }, 200);
    }
  }
}
