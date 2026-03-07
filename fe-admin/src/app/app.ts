import {Component, inject, OnInit, signal} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {PrimeNG} from "primeng/config";

@Component({
    selector: 'app-root',
    imports: [
        RouterOutlet,
        // RouterOutlet
    ],
    templateUrl: './app.html',
    styleUrl: './app.css'
})
export class App implements OnInit {
    ngOnInit(): void {
        this.primeng.ripple.set(true);
    }
    
    private primeng = inject(PrimeNG);
}
