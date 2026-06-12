import {Component, inject, OnInit, signal} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {PrimeNG} from "primeng/config";
import {Toast} from "primeng/toast";

@Component({
    selector: 'app-root',
    imports: [
        RouterOutlet,
        Toast,
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
