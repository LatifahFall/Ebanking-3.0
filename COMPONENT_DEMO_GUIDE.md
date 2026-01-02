# Guide de Visualisation des Composants - E-Banking 3.0

## 🎯 Comment Tester les Composants Sans Microservices

### Option 1: Créer une Page de Démonstration (Recommandé)

Créez une page dédiée pour visualiser tous les composants avec des données mockées:

```bash
cd c:\Users\Hp\Desktop\front\ebanking-app
ng generate component pages/component-demo --skip-tests
```

Ensuite, modifiez `src/app/pages/component-demo/component-demo.component.ts`:

```typescript
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { InfoCardComponent } from '../../shared/components/info-card/info-card.component';
import { TransactionItemComponent } from '../../shared/components/transaction-item/transaction-item.component';
import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';
import { ChartWidgetComponent } from '../../shared/components/chart-widget/chart-widget.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { Transaction, TransactionType, TransactionCategory, TransactionStatus } from '../../models';

@Component({
  selector: 'app-component-demo',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    InfoCardComponent,
    TransactionItemComponent,
    CustomButtonComponent,
    ChartWidgetComponent,
    PageHeaderComponent,
    LoaderComponent
  ],
  template: `
    <div class="demo-container">
      <app-page-header
        title="Composants UI - Démo"
        [breadcrumbs]="['Accueil', 'Démo']"
        subtitle="Visualisation de tous les composants réutilisables">
      </app-page-header>

      <section class="demo-section">
        <h2>🎴 Info Cards</h2>
        <div class="cards-grid">
          <app-info-card
            title="Solde Total"
            value="$125,450.00"
            subtitle="Tous les comptes"
            icon="account_balance"
            [trend]="5.2"
            trendLabel="vs mois dernier"
            color="#4F46E5">
          </app-info-card>

          <app-info-card
            title="Revenus"
            value="$8,240.00"
            subtitle="Ce mois"
            icon="trending_up"
            [trend]="12.5"
            trendLabel="vs mois dernier"
            color="#16A34A">
          </app-info-card>

          <app-info-card
            title="Dépenses"
            value="$3,890.50"
            subtitle="Ce mois"
            icon="trending_down"
            [trend]="-3.8"
            trendLabel="vs mois dernier"
            color="#EF4444">
          </app-info-card>

          <app-info-card
            title="Crypto"
            value="$15,200.00"
            subtitle="Portfolio"
            icon="currency_bitcoin"
            [trend]="8.3"
            trendLabel="24h"
            color="#F59E0B">
          </app-info-card>
        </div>
      </section>

      <section class="demo-section">
        <h2>💸 Transactions</h2>
        <div class="transactions-list">
          @for (transaction of mockTransactions; track transaction.id) {
            <app-transaction-item [transaction]="transaction"></app-transaction-item>
          }
        </div>
      </section>

      <section class="demo-section">
        <h2>🔘 Boutons</h2>
        <div class="buttons-demo">
          <app-custom-button
            label="Primary Button"
            variant="primary"
            icon="send">
          </app-custom-button>

          <app-custom-button
            label="Secondary Button"
            variant="secondary"
            icon="save">
          </app-custom-button>

          <app-custom-button
            label="Danger Button"
            variant="danger"
            icon="delete">
          </app-custom-button>

          <app-custom-button
            label="Ghost Button"
            variant="ghost"
            icon="refresh">
          </app-custom-button>
        </div>
      </section>

      <section class="demo-section">
        <h2>📊 Chart Widget</h2>
        <app-chart-widget
          title="Dépenses Mensuelles"
          subtitle="Placeholder pour graphiques">
        </app-chart-widget>
      </section>

      <section class="demo-section">
        <h2>⏳ Loader</h2>
        <app-loader message="Chargement des données..."></app-loader>
      </section>
    </div>
  `,
  styles: [`
    .demo-container {
      padding: 2rem;
      max-width: 1400px;
      margin: 0 auto;
    }

    .demo-section {
      margin-bottom: 3rem;
      padding: 2rem;
      background: var(--card-bg);
      border-radius: 16px;
      box-shadow: var(--shadow-sm);

      h2 {
        margin: 0 0 1.5rem;
        color: var(--text-primary);
        font-size: 1.5rem;
        font-weight: 600;
      }
    }

    .cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1.5rem;
    }

    .transactions-list {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .buttons-demo {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
    }
  `]
})
export class ComponentDemoComponent {
  mockTransactions: Transaction[] = [
    {
      id: '1',
      accountId: 'acc-1',
      type: TransactionType.CREDIT,
      category: TransactionCategory.SALARY,
      amount: 5000,
      currency: 'USD',
      description: 'Salaire Mensuel',
      timestamp: new Date('2026-01-01'),
      status: TransactionStatus.COMPLETED,
      balance: 125450
    },
    {
      id: '2',
      accountId: 'acc-1',
      type: TransactionType.DEBIT,
      category: TransactionCategory.SHOPPING,
      amount: 89.50,
      currency: 'USD',
      description: 'Amazon.com',
      timestamp: new Date('2025-12-31'),
      status: TransactionStatus.COMPLETED,
      balance: 120450
    },
    {
      id: '3',
      accountId: 'acc-2',
      type: TransactionType.DEBIT,
      category: TransactionCategory.UTILITIES,
      amount: 150.00,
      currency: 'USD',
      description: 'Électricité',
      timestamp: new Date('2025-12-30'),
      status: TransactionStatus.PENDING,
      balance: 120360.50
    }
  ];
}
```

Ajoutez la route dans `src/app/app.routes.ts`:

```typescript
{
  path: '',
  component: MainLayoutComponent,
  canActivate: [authGuard],
  children: [
    { path: 'dashboard', component: DashboardComponent },
    { path: 'accounts', component: AccountsComponent },
    { path: 'demo', loadComponent: () => import('./pages/component-demo/component-demo.component').then(m => m.ComponentDemoComponent) },
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
  ]
}
```

### Option 2: Utiliser Storybook (Pour Développement Avancé)

Installation:
```bash
npx storybook@latest init
```

Créez des stories pour chaque composant dans `src/app/shared/components/[nom-composant]/[nom-composant].stories.ts`

### Option 3: Créer un Mode "Mock" Complet

Tous les services utilisent déjà des données mockées! L'application fonctionne SANS backend:

1. **AuthService** → Retourne des utilisateurs fictifs
2. **AccountService** → Données de comptes simulées
3. **TransactionService** → Historique de transactions mock
4. **NotificationService** → Notifications en mémoire

### 🚀 Lancer l'Application

```bash
cd c:\Users\Hp\Desktop\front\ebanking-app
ng serve
```

Ouvrez `http://localhost:4200` et connectez-vous avec:
- **Email**: n'importe quel email
- **Password**: n'importe quel mot de passe
- **Code MFA**: 123456 (ou n'importe quel 6 chiffres)

### 📍 Navigation

Après connexion, vous pouvez accéder à:
- `/dashboard` - Tableau de bord avec tous les composants
- `/accounts` - Vue des comptes
- `/demo` - Page de démo des composants (après ajout de la route)

### 🎨 Composants Disponibles

#### Shared Components (Réutilisables):
1. **InfoCard** - Carte statistique avec tendance
2. **TransactionItem** - Élément de transaction
3. **CustomButton** - Bouton stylisé avec variantes
4. **ChartWidget** - Placeholder pour graphiques
5. **PageHeader** - En-tête de page avec breadcrumbs
6. **Loader** - Indicateur de chargement
7. **NotificationBell** - Cloche de notifications
8. **Sidebar** - Navigation latérale
9. **Navbar** - Barre de navigation supérieure

#### Layouts:
1. **MainLayout** - Layout principal (sidebar + navbar)
2. **AuthLayout** - Layout d'authentification

#### Pages:
1. **Login** - Connexion
2. **MFA** - Authentification à deux facteurs
3. **Dashboard** - Tableau de bord complet
4. **Accounts** - Vue des comptes

### 🔍 Test du Thème Dark/Light

Cliquez sur l'icône ☀️/🌙 dans la navbar pour basculer entre les modes.

### 📝 Notes Importantes

- ✅ **Pas de microservices requis** - Tout est mocké
- ✅ **Données réalistes** - Les services retournent des données cohérentes
- ✅ **Délais simulés** - `delay(500)` pour simuler les appels API
- ✅ **Authentification fictive** - Token stocké dans localStorage
- ✅ **Responsive** - Testez sur mobile/tablet/desktop

### 🛠️ Ajouter des Composants au Sidebar

Modifiez `src/app/shared/components/sidebar/sidebar.component.ts`:

```typescript
menuItems: MenuItem[] = [
  { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
  { label: 'Accounts', icon: 'account_balance', route: '/accounts' },
  { label: 'Demo Composants', icon: 'widgets', route: '/demo' }, // ← Ajoutez ceci
  // ... autres items
];
```

### 🎯 Prochaines Étapes

Pour développer de nouvelles pages sans backend:
1. Créez le composant avec `ng g c pages/nouvelle-page`
2. Ajoutez des données mock dans le composant
3. Utilisez les composants shared existants
4. Ajoutez la route dans `app.routes.ts`
5. Ajoutez l'entrée dans le sidebar

---

**L'application est 100% fonctionnelle en mode frontend-only!** 🚀
