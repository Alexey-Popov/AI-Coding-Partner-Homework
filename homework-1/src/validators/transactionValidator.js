const allowedTypes = ['deposit', 'withdrawal', 'transfer'];
const allowedStatus = ['pending', 'completed', 'failed'];

// Comprehensive set of ISO 4217 currency codes (common modern set)
const allowedCurrencies = new Set([
  'AED','AFN','ALL','AMD','ANG','AOA','ARS','AUD','AWG','AZN',
  'BAM','BBD','BDT','BGN','BHD','BIF','BMD','BND','BOB','BRL','BSD','BTN','BWP','BYN','BZD',
  'CAD','CDF','CHF','CLP','CNY','COP','CRC','CUC','CUP','CVE','CZK',
  'DJF','DKK','DOP','DZD',
  'EGP','ERN','ETB','EUR',
  'FJD','FKP',
  'GBP','GEL','GGP','GHS','GIP','GMD','GNF','GTQ','GYD',
  'HKD','HNL','HRK','HTG','HUF',
  'IDR','ILS','IMP','INR','IQD','IRR','ISK',
  'JEP','JMD','JOD','JPY',
  'KES','KGS','KHR','KMF','KPW','KRW','KWD','KYD','KZT',
  'LAK','LBP','LKR','LRD','LSL','LYD',
  'MAD','MDL','MGA','MKD','MMK','MNT','MOP','MRU','MUR','MVR','MWK','MXN','MYR','MZN',
  'NAD','NGN','NIO','NOK','NPR','NZD',
  'OMR',
  'PAB','PEN','PGK','PHP','PKR','PLN','PYG',
  'QAR',
  'RON','RSD','RUB','RWF',
  'SAR','SBD','SCR','SDG','SEK','SGD','SHP','SLL','SOS','SRD','SSP','STN','SVC','SYP','SZL',
  'THB','TJS','TMT','TND','TOP','TRY','TTD','TVD','TWD','TZS',
  'UAH','UGX','USD','UYU','UZS',
  'VES','VND','VUV',
  'WST',
  'XAF','XAG','XAU','XCD','XDR','XOF','XPD','XPF','XPT',
  'YER','ZAR','ZMW','ZWL'
]);

function isISODateString(s){
  if(!s) return false;
  const d = Date.parse(s);
  return !Number.isNaN(d);
}

function hasTwoOrFewerDecimals(value){
  // Accept numbers or numeric strings
  const str = String(value);
  return /^\d+(?:\.\d{1,2})?$/.test(str);
}

function isAccountFormat(s){
  return /^ACC-[A-Za-z0-9]{5}$/.test(String(s));
}

function validateTransaction(data){
  const errors = [];
  if(!data) {
    errors.push({ field: null, message: 'Missing transaction data' });
    return errors;
  }

  // amount
  if(typeof data.amount === 'undefined') {
    errors.push({ field: 'amount', message: 'amount is required' });
  } else if (Number.isNaN(Number(data.amount)) || Number(data.amount) <= 0) {
    errors.push({ field: 'amount', message: 'Amount must be a positive number' });
  } else if (!hasTwoOrFewerDecimals(data.amount)) {
    errors.push({ field: 'amount', message: 'Amount must have at most 2 decimal places' });
  }

  // currency
  if(!data.currency || typeof data.currency !== 'string' || !/^[A-Za-z]{3}$/.test(data.currency)) {
    errors.push({ field: 'currency', message: 'currency must be a 3-letter ISO code' });
  } else if (!allowedCurrencies.has(data.currency.toUpperCase())) {
    errors.push({ field: 'currency', message: 'Invalid currency code' });
  }

  // type
  if(!data.type || !allowedTypes.includes(data.type)) errors.push({ field: 'type', message: 'type must be one of: ' + allowedTypes.join(', ') });

  // status
  if(data.status && !allowedStatus.includes(data.status)) errors.push({ field: 'status', message: 'status must be one of: ' + allowedStatus.join(', ') });

  // Account checks depending on type
  if(data.type === 'deposit') {
    if(!data.toAccount) errors.push({ field: 'toAccount', message: 'toAccount is required for deposit' });
    else if(!isAccountFormat(data.toAccount)) errors.push({ field: 'toAccount', message: 'toAccount must follow ACC-XXXXX format' });
  }
  if(data.type === 'withdrawal') {
    if(!data.fromAccount) errors.push({ field: 'fromAccount', message: 'fromAccount is required for withdrawal' });
    else if(!isAccountFormat(data.fromAccount)) errors.push({ field: 'fromAccount', message: 'fromAccount must follow ACC-XXXXX format' });
  }
  if(data.type === 'transfer') {
    if(!data.fromAccount) errors.push({ field: 'fromAccount', message: 'fromAccount is required for transfer' });
    else if(!isAccountFormat(data.fromAccount)) errors.push({ field: 'fromAccount', message: 'fromAccount must follow ACC-XXXXX format' });
    if(!data.toAccount) errors.push({ field: 'toAccount', message: 'toAccount is required for transfer' });
    else if(!isAccountFormat(data.toAccount)) errors.push({ field: 'toAccount', message: 'toAccount must follow ACC-XXXXX format' });
  }

  return errors;
}

module.exports = { validateTransaction };
