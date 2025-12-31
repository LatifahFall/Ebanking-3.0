# 🔔 Intégration WebSocket pour Notifications Push (Angular)

## 📦 Installation

```bash
npm install sockjs-client @stomp/stompjs
npm install --save-dev @types/sockjs-client
```

## 🔧 Service Angular : notification-websocket.service.ts

```typescript
import { Injectable } from '@angular/core';
import * as SockJS from 'sockjs-client';
import { Client, Message } from '@stomp/stompjs';
import { BehaviorSubject, Observable } from 'rxjs';

export interface PushNotification {
  id: number;
  title: string;
  message: string;
  type: string;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationWebSocketService {
  private stompClient: Client | null = null;
  private notificationsSubject = new BehaviorSubject<PushNotification[]>([]);
  public notifications$: Observable<PushNotification[]> = this.notificationsSubject.asObservable();

  constructor() {}

  connect(userId: string): void {
    const socket = new SockJS('http://localhost:8084/ws');
    
    this.stompClient = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = () => {
      console.log('✅ WebSocket connected');
      
      // S'abonner aux notifications de l'utilisateur
      this.stompClient!.subscribe(`/user/queue/notifications`, (message: Message) => {
        const notification: PushNotification = JSON.parse(message.body);
        this.handleNewNotification(notification);
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('❌ WebSocket error:', frame);
    };

    this.stompClient.activate();
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      console.log('🔌 WebSocket disconnected');
    }
  }

  private handleNewNotification(notification: PushNotification): void {
    console.log('🔔 New notification received:', notification);
    
    // Ajouter à la liste
    const current = this.notificationsSubject.value;
    this.notificationsSubject.next([notification, ...current]);
    
    // Afficher une notification navigateur
    this.showBrowserNotification(notification);
  }

  private showBrowserNotification(notification: PushNotification): void {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(notification.title, {
        body: notification.message,
        icon: '/assets/logo.png'
      });
    }
  }

  requestNotificationPermission(): void {
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission();
    }
  }
}
```

## 🎯 Utilisation dans un Component

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { NotificationWebSocketService, PushNotification } from './services/notification-websocket.service';

@Component({
  selector: 'app-dashboard',
  template: `
    <div class="notifications-panel">
      <h3>🔔 Notifications</h3>
      <div *ngFor="let notif of notifications" class="notification-item">
        <strong>{{ notif.title }}</strong>
        <p>{{ notif.message }}</p>
        <small>{{ notif.timestamp | date:'short' }}</small>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit, OnDestroy {
  notifications: PushNotification[] = [];

  constructor(private wsService: NotificationWebSocketService) {}

  ngOnInit(): void {
    // Demander la permission pour les notifications navigateur
    this.wsService.requestNotificationPermission();
    
    // Se connecter au WebSocket
    const userId = this.getCurrentUserId(); // Récupérer depuis AuthService
    this.wsService.connect(userId);
    
    // Écouter les nouvelles notifications
    this.wsService.notifications$.subscribe(
      (notifications) => {
        this.notifications = notifications;
      }
    );
  }

  ngOnDestroy(): void {
    this.wsService.disconnect();
  }

  private getCurrentUserId(): string {
    // TODO: Récupérer l'ID utilisateur depuis votre service d'authentification
    return 'user123';
  }
}
```

## 🧪 Test de l'intégration

### 1. Démarrer le backend
```bash
mvn spring-boot:run
```

### 2. Démarrer Angular
```bash
ng serve
```

### 3. Tester via l'API
```bash
curl -X POST http://localhost:8084/api/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "type": "PUSH",
    "recipient": "user123",
    "subject": "Test Push",
    "message": "This is a test push notification"
  }'
```

Le frontend devrait recevoir la notification instantanément ! ✅

## 📱 Bonus : Notification navigateur

Pour afficher les notifications système (hors navigateur), ajoutez dans `app.component.ts` :

```typescript
ngOnInit(): void {
  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission();
  }
}
```

## 🎨 Style CSS pour les notifications

```css
.notifications-panel {
  position: fixed;
  top: 60px;
  right: 20px;
  width: 300px;
  max-height: 400px;
  overflow-y: auto;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  padding: 16px;
  z-index: 1000;
}

.notification-item {
  border-bottom: 1px solid #eee;
  padding: 12px 0;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
```