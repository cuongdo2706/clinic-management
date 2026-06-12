import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterOutlet } from '@angular/router';
import { Footer } from '../footer/footer';
import { Login } from '../login/login';
import { Navbar } from '../navbar/navbar';
import { Register } from '../register/register';

@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, Navbar, Login, Register, Footer],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css',
})
export class MainLayout {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly showLoginModal = signal(false);
  readonly showRegisterModal = signal(false);
  readonly loginReturnUrl = signal<string | null>(null);

  constructor() {
    this.route.queryParamMap.subscribe((params) => {
      this.showLoginModal.set(params.get('login') === 'true');
      this.showRegisterModal.set(params.get('register') === 'true');
      this.loginReturnUrl.set(params.get('returnUrl'));
    });
  }

  closeLoginModal(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { login: null, returnUrl: null },
      queryParamsHandling: 'merge',
    });
  }

  closeRegisterModal(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { register: null },
      queryParamsHandling: 'merge',
    });
  }
}
