import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../services/users/user.service';
import { CartService } from '../../services/auth/cart.service';
import { UseStateService } from '../../services/auth/use-state.service';
import { PopupService } from '../../services/popup.service';
import { environment } from '../../../environments/environment.prod';

@Component({
  selector: 'app-public-profile',
  standalone: false,
  templateUrl: './public-profile.component.html',
  styleUrl: './public-profile.component.scss',
})
export class PublicProfileComponent implements OnInit {
  user: any = null;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private cartService: CartService,
    private popupService: PopupService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const username = params.get('username');
      if (username) {
        this.userService.getPublicProfile(username).subscribe({
          next: (res) => {
            this.user = res;

            // 🔧 Combinar todas las reseñas de todos los servicios
            this.user.reviews =
              this.user.professionalServices?.flatMap(
                (s: any) => s.reviews || []
              ) || [];
          },
          error: (err) => console.error('Error cargando perfil público:', err),
        });
      }
    });
  }
  getStarCount(calification: string): number {
    switch (calification) {
      case 'EXCELENTE':
        return 5;
      case 'BUENO':
        return 4;
      case 'REGULAR':
        return 3;
      case 'MALO':
        return 2;
      case 'PESIMO':
        return 1;
      default:
        return 0;
    }
  }
  goToServicio(serviceId: number) {
    this.router.navigate(['/servicio', serviceId]);
  }
  getAverageRating(): number {
    if (!this.user?.reviews?.length) return 0;

    const total = this.user.reviews
      .map((r: any) => this.getStarCount(r.calification))
      .reduce((acc: number, val: number) => acc + val, 0);

    return total / this.user.reviews.length;
  }
  getAvatarUrl(avatarPath: string): string {
    if (!avatarPath || avatarPath.trim() === '') {
      return '/ken.gif'; // Imagen por defecto
    }

  const cleanPath = avatarPath.trim(); // 🔑 recorta espacios

  if (cleanPath.startsWith('http://') || cleanPath.startsWith('https://')) {
    return cleanPath; // usar Cloudinary directamente
  }

  // Si es relativa, concatena con tu API
  return `${environment.apiUrl}${cleanPath}`;
  }
  getImageUrl(imagePath: string): string {
    if (!imagePath || typeof imagePath !== 'string')
      return '/assets/image-not-found.jpg';
    const trimmed = imagePath.trim();
    if (trimmed.startsWith('http')) return trimmed;
    return `https://warmhelp.onrender.com/api/uploads/images/${encodeURIComponent(
      trimmed
    )}`;
  }

  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = '/assets/image-not-found.jpg';
  }

  addToCart(service: any): void {
    this.cartService.addToCart(service, 1);
    this.popupService.showMessage(
      'Servicio añadido',
      'El servicio se ha añadido correctamente al carrito.',
      'success'
    );
  }
}
