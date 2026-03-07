import { Component } from '@angular/core';
import {RouterOutlet} from "@angular/router";
import {Card} from "primeng/card";
import {Menu} from "primeng/menu";
import {Navbar} from "../navbar/navbar";
import {Sidebar} from "../sidebar/sidebar";

@Component({
  selector: 'app-main-layout',
    imports: [
        RouterOutlet,
        Navbar,
        Sidebar
    ],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css',
})
export class MainLayout {

}
