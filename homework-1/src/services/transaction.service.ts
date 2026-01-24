import { Transaction, CreateTransactionInput } from '../types';
import { NotFoundException } from '../errors';
import * as storage from '../utils/storage';

export function createTransaction(input: CreateTransactionInput): Transaction {
  return storage.createTransaction(input);
}

export function getAllTransactions(): Transaction[] {
  return storage.getAllTransactions();
}

export function getTransactionById(id: string): Transaction {
  const transaction = storage.getTransactionById(id);

  if (!transaction) {
    throw new NotFoundException('Transaction', id);
  }

  return transaction;
}
