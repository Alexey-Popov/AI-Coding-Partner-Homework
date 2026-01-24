export function parseFromDate(dateStr: string | undefined): Date | null {
  if (!dateStr || dateStr.trim() === '') {
    return null;
  }

  const date = new Date(dateStr);
  
  if (!dateStr.includes('T') && dateStr.match(/^\d{4}-\d{2}-\d{2}$/)) {
    date.setHours(0, 0, 0, 0);
  }
  
  return date;
}

export function parseToDate(dateStr: string | undefined): Date | null {
  if (!dateStr || dateStr.trim() === '') {
    return null;
  }

  const date = new Date(dateStr);
  
  if (!dateStr.includes('T') && dateStr.match(/^\d{4}-\d{2}-\d{2}$/)) {
    date.setHours(23, 59, 59, 999);
  }
  
  return date;
}
