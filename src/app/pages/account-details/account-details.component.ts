import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { AccountService } from '../../core/services/account.service';
import { Account } from '../../models';

@Component({
  selector: 'app-account-details',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <h2>Account Details</h2>
      <div *ngIf="account">
        <p><strong>Account:</strong> {{ account.accountNumber }}</p>
        <p><strong>Type:</strong> {{ account.accountType }}</p>
        <p><strong>Balance:</strong> {{ account.balance | number:'1.2-2' }} {{ account.currency }}</p>
        <p><strong>Status:</strong> {{ account.status }}</p>
      </div>
      <div *ngIf="!account">Loading...</div>
    </div>
  `
})
export class AccountDetailsComponent implements OnInit {
  account: Account | null = null;

  constructor(private route: ActivatedRoute, private accountService: AccountService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.accountService.getAccountById(id).subscribe(a => (this.account = a));
    }
  }
}