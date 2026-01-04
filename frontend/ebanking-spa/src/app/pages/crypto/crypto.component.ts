import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { InfoCardComponent } from '../../shared/components/info-card/info-card.component';
import { ChartWidgetComponent, ChartData } from '../../shared/components/chart-widget/chart-widget.component';
import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { CryptoService } from '../../core/services/crypto.service';
import {
  CryptoPortfolio,
  CryptoHoldingWithPrice,
  CryptoTransaction,
  CryptoCoin,
  BuyCryptoRequest,
  SellCryptoRequest,
  TransactionType,
  TransactionStatus
} from '../../models/crypto.model';

/**
 * Crypto Component
 * Complete crypto wallet management with portfolio, trading, and analytics
 */
@Component({
  selector: 'app-crypto',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatTableModule,
    MatSnackBarModule,
    MatDialogModule,
    MatSelectModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    PageHeaderComponent,
    InfoCardComponent,
    ChartWidgetComponent,
    CustomButtonComponent,
    LoaderComponent
  ],
  templateUrl: './crypto.component.html',
  styleUrl: './crypto.component.scss'
})
export class CryptoComponent implements OnInit {
  // State
  loading = true;
  errorMessage: string | null = null;
  portfolio: CryptoPortfolio | null = null;
  availableCoins: CryptoCoin[] = [];
  selectedTab = 0;

  // Forms
  buyForm = {
    symbol: '',
    eurAmount: 0
  };

  sellForm = {
    symbol: '',
    cryptoAmount: 0
  };

  // Table columns
  holdingsColumns: string[] = ['symbol', 'amount', 'price', 'value', 'change24h', 'actions'];
  transactionsColumns: string[] = ['date', 'type', 'symbol', 'amount', 'price', 'total', 'status'];

  // Charts
  portfolioChartData: ChartData | null = null;
  performanceChartData: ChartData | null = null;

  constructor(
    private cryptoService: CryptoService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  /**
   * Load all crypto data
   */
  loadData(): void {
    this.loading = true;
    this.errorMessage = null;

    const userId = 1; // TODO: Get from auth service

    this.cryptoService.getPortfolio(userId).subscribe({
      next: (portfolio) => {
        this.portfolio = portfolio;
        this.generateCharts();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading portfolio:', error);
        this.errorMessage = 'Failed to load crypto portfolio. Please try again.';
        this.loading = false;
        this.showError('Failed to load crypto portfolio');
      }
    });

    this.cryptoService.getCoinsDetails().subscribe({
      next: (coins) => {
        this.availableCoins = coins;
      },
      error: (error) => {
        console.error('Error loading coins:', error);
      }
    });
  }

  /**
   * Generate chart data from portfolio
   */
  private generateCharts(): void {
    if (!this.portfolio) return;

    // Portfolio distribution chart (pie-like data for bar chart)
    const holdings = this.portfolio.holdings;
    const labels = holdings.map(h => h.cryptoSymbol);
    const values = holdings.map(h => h.valueInEUR);

    this.portfolioChartData = {
      labels,
      datasets: [{
        label: 'Portfolio Value (EUR)',
        data: values,
        color: '#1E6AE1'
      }]
    };

    // Performance chart (last 7 days mock data)
    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const portfolioValues = [8500, 8700, 9200, 9100, 9500, 9800, this.portfolio.totalValueEUR];

    this.performanceChartData = {
      labels: days,
      datasets: [{
        label: 'Portfolio Value',
        data: portfolioValues,
        color: '#10B981'
      }]
    };
  }

  /**
   * Buy crypto
   */
  onBuyCrypto(): void {
    if (!this.portfolio || !this.buyForm.symbol || this.buyForm.eurAmount <= 0) {
      this.showError('Please fill in all fields with valid values');
      return;
    }

    const request: BuyCryptoRequest = {
      symbol: this.buyForm.symbol,
      eurAmount: this.buyForm.eurAmount
    };

    this.cryptoService.buyCrypto(this.portfolio.wallet.id, request).subscribe({
      next: (transaction) => {
        this.showSuccess(`Successfully bought ${transaction.cryptoAmount.toFixed(8)} ${transaction.cryptoSymbol}`);
        this.buyForm = { symbol: '', eurAmount: 0 };
        this.loadData(); // Reload to update portfolio
      },
      error: (error) => {
        console.error('Error buying crypto:', error);
        this.showError('Failed to buy crypto. Please try again.');
      }
    });
  }

  /**
   * Sell crypto
   */
  onSellCrypto(): void {
    if (!this.portfolio || !this.sellForm.symbol || this.sellForm.cryptoAmount <= 0) {
      this.showError('Please fill in all fields with valid values');
      return;
    }

    // Check if user has enough crypto
    const holding = this.portfolio.holdings.find(h => h.cryptoSymbol === this.sellForm.symbol.toUpperCase());
    if (!holding || holding.amount < this.sellForm.cryptoAmount) {
      this.showError(`Insufficient ${this.sellForm.symbol} balance`);
      return;
    }

    const request: SellCryptoRequest = {
      symbol: this.sellForm.symbol,
      cryptoAmount: this.sellForm.cryptoAmount
    };

    this.cryptoService.sellCrypto(this.portfolio.wallet.id, request).subscribe({
      next: (transaction) => {
        this.showSuccess(`Successfully sold ${transaction.cryptoAmount.toFixed(8)} ${transaction.cryptoSymbol} for €${transaction.eurAmount.toFixed(2)}`);
        this.sellForm = { symbol: '', cryptoAmount: 0 };
        this.loadData(); // Reload to update portfolio
      },
      error: (error) => {
        console.error('Error selling crypto:', error);
        this.showError('Failed to sell crypto. Please try again.');
      }
    });
  }

  /**
   * Get holding by symbol
   */
  getHolding(symbol: string): CryptoHoldingWithPrice | undefined {
    return this.portfolio?.holdings.find(h => h.cryptoSymbol === symbol.toUpperCase());
  }

  /**
   * Get coin by symbol
   */
  getCoin(symbol: string): CryptoCoin | undefined {
    return this.availableCoins.find(c => c.symbol.toUpperCase() === symbol.toUpperCase());
  }

  /**
   * Get coin price for buy preview
   */
  getBuyCoinPrice(symbol: string): number {
    const coin = this.getCoin(symbol);
    return coin?.current_price || 0;
  }

  /**
   * Get holding for sell preview
   */
  getSellHolding(symbol: string): CryptoHoldingWithPrice | null {
    return this.getHolding(symbol) || null;
  }

  /**
   * Format currency
   */
  formatCurrency(amount: number): string {
    return `€${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  /**
   * Format crypto amount
   */
  formatCrypto(amount: number, decimals: number = 8): string {
    return amount.toFixed(decimals);
  }

  /**
   * Format percentage
   */
  formatPercent(value: number): string {
    const sign = value >= 0 ? '+' : '';
    return `${sign}${value.toFixed(2)}%`;
  }

  /**
   * Get transaction type icon
   */
  getTransactionTypeIcon(type: TransactionType): string {
    return type === TransactionType.BUY ? 'trending_up' : 'trending_down';
  }

  /**
   * Get transaction type color
   */
  getTransactionTypeColor(type: TransactionType): string {
    return type === TransactionType.BUY ? '#10B981' : '#EF4444';
  }

  /**
   * Get status color
   */
  getStatusColor(status: TransactionStatus): string {
    switch (status) {
      case TransactionStatus.COMPLETED:
        return '#10B981';
      case TransactionStatus.PENDING:
        return '#F59E0B';
      case TransactionStatus.FAILED:
        return '#EF4444';
      default:
        return '#64748B';
    }
  }

  /**
   * Show success message
   */
  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }

  /**
   * Show error message
   */
  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }
}
