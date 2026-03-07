import {Component, inject} from '@angular/core';
import {Router} from "@angular/router";
import {NgOptimizedImage} from "@angular/common";

@Component({
  selector: 'app-page-not-found',
    imports: [
        NgOptimizedImage
    ],
  templateUrl: './page-not-found.html',
  styleUrl: './page-not-found.css',
})
export class PageNotFound {
    private router = inject(Router);
    
    goHome() {
        void this.router.navigate(['/']);
    }
}
