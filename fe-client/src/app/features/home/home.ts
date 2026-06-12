import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Card } from 'primeng/card';

interface FeatureCard {
  title: string;
  description: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-home',
  imports: [RouterLink, Card],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  readonly features: FeatureCard[] = [
    {
      title: 'Đặt lịch khám',
      description: 'Chọn bác sĩ, ngày khám và khung giờ còn trống.',
      icon: 'pi pi-calendar',
      route: '/booking',
    },
    {
      title: 'Quản lý lịch khám',
      description: 'Theo dõi trạng thái các lịch hẹn đã đặt.',
      icon: 'pi pi-list',
      route: '/appointments',
    },
    {
      title: 'Hồ sơ sức khỏe',
      description: 'Xem kết quả khám, điều trị và đơn thuốc cá nhân.',
      icon: 'pi pi-heart',
      route: '/health-records',
    },
  ];
}
