import { AccountBalance } from '../models';
import { NotFoundException } from '../errors';
import * as storage from '../utils/storage';

const DEFAULT_CURRENCY = 'USD';

export function getAccountBalance(accountId: string): AccountBalance {
  const balance = storage.getAccountBalance(accountId);

  if (balance === null) {
    throw new NotFoundException('Account', accountId);
  }

  return {
    accountId,
    balance,
    currency: DEFAULT_CURRENCY
  };
}
