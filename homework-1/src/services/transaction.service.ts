import { Transaction, CreateTransactionInput, TransactionFilter } from '../models';
import { NotFoundException } from '../errors';
import * as storage from '../utils/storage';

export function createTransaction(input: CreateTransactionInput): Transaction {
  return storage.createTransaction(input);
}

export function getAllTransactions(filter?: TransactionFilter): Transaction[] {
  return storage.getAllTransactions(filter);
}

export function getTransactionById(id: string): Transaction {
  const transaction = storage.getTransactionById(id);

  if (!transaction) {
    throw new NotFoundException('Transaction', id);
  }

  return transaction;
}
